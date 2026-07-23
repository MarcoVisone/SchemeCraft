package com.xyra.schemecraft.service;

import com.xyra.schemecraft.connection.ConnectionPool;
import com.xyra.schemecraft.dao.*;
import com.xyra.schemecraft.dto.OrderAdminView;
import com.xyra.schemecraft.dto.OrderSearchCriteria;
import com.xyra.schemecraft.exception.*;
import com.xyra.schemecraft.model.*;
import com.xyra.schemecraft.service.gateway.ChargeResult;
import com.xyra.schemecraft.service.gateway.FakePaymentGateway;
import com.xyra.schemecraft.constant.ServiceConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private static final ConcurrentHashMap<String, Boolean> purchaseLocks = new ConcurrentHashMap<>();

    private final ProductDAO productDAO;
    private final CartDAO cartDAO;
    private final OrderDAO orderDAO;
    private final OrderItemDAO orderItemDAO;
    private final AccountDAO accountDAO;
    private final AccountProductDAO accountProductDAO;
    private final FakePaymentGateway paymentGateway;
    private final EntityValidator entityValidator;

    public OrderService() {
        this.productDAO = new ProductDAO();
        this.cartDAO = new CartDAO();
        this.orderDAO = new OrderDAO();
        this.orderItemDAO = new OrderItemDAO();
        this.accountDAO = new AccountDAO();
        this.accountProductDAO = new AccountProductDAO();
        this.paymentGateway = new FakePaymentGateway();
        this.entityValidator = new EntityValidator();
    }

    public enum OrderStatus {
        PENDING(1),
        PAID(2),
        SHIPPED(3),
        CANCELLED(4),
        PENDING_VERIFICATION(5);

        private final int id;

        OrderStatus(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public record OrderItemSnapshot(
            String productId,
            BigDecimal price,
            BigDecimal discount,
            BigDecimal tax
    ) {}

    private record OrderCreationContext(
            String orderId,
            AccountBean account,
            PaymentMethodBean paymentMethod,
            CurrencyBean currency,
            List<OrderItemSnapshot> items,
            BigDecimal totalAmount,
            boolean isFromCart
    ) {}

    public String placeOrderDirect(String accountId, String productId) {
        OrderCreationContext context = validateAndCreatePendingOrder(accountId, productId, false);
        return executeOrderProcessing(context);
    }

    public String placeOrderFromCart(String accountId) {
        OrderCreationContext context = validateAndCreatePendingOrder(accountId, null, true);
        return executeOrderProcessing(context);
    }

    public OrderBean getOrderById(String orderId) throws EntityNotFoundException, ServiceException {
        try (Connection connection = ConnectionPool.getConnection()) {
            return orderDAO.findById(connection, orderId)
                    .orElseThrow(() -> {
                        logger.warn("Order not found for ID: {}", orderId);
                        return new EntityNotFoundException("Order not found for ID: " + orderId);
                    });
        } catch (SQLException e) {
            logger.error("Database connection error while fetching order ID: {}", orderId, e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    public List<OrderBean> listAccountOrders(String accountId, int pageNumber) throws ServiceException {
        try (Connection connection = ConnectionPool.getConnection()) {
            return orderDAO.findAllByAccountId(connection, accountId, pageNumber, ServiceConstants.ORDERS_PAGE_SIZE);
        } catch (DAOException | SQLException e) {
            logger.error("Database error while listing orders for Account: {}", accountId, e);
            throw new ServiceException("Unable to list orders due to an internal error", e);
        }
    }

    public void updateOrderStatus(String orderId, int statusId) throws EntityNotFoundException, ServiceException {
        Connection connection = null;
        boolean success = false;

        try {
            connection = ConnectionPool.getConnection();
            connection.setAutoCommit(false);

            OrderBean order = orderDAO.findById(connection, orderId)
                    .orElseThrow(() -> new EntityNotFoundException("Order not found for ID: " + orderId));

            int oldStatusId = order.getStatus();

            if (oldStatusId == statusId) {
                connection.commit();
                return;
            }

            orderDAO.updateStatus(connection, orderId, statusId);

            if (statusId == OrderStatus.CANCELLED.getId()) {
                List<OrderItemBean> items = orderItemDAO.findAllByOrderId(connection, orderId);
                for (OrderItemBean item : items) {
                    productDAO.incrementStock(connection, item.getProductId());
                }
                logger.info("Order {} cancelled. Stock units restored for associated products.", orderId);
            }

            connection.commit();
            success = true;
            logger.info("Order {} status updated from {} to {}", orderId, oldStatusId, statusId);

        } catch (EntityNotFoundException e) {
            throw e;
        } catch (SQLException | DAOException e) {
            logger.error("Database error while updating status for Order: {}", orderId, e);
            throw new ServiceException("Unable to update order status due to an internal error", e);
        } finally {
            if (connection != null) {
                if (!success) {
                    rollback(connection);
                }
                closeConnection(connection);
            }
        }
    }

    public List<OrderAdminView> searchOrders(OrderSearchCriteria criteria) throws ServiceException {
        try (Connection connection = ConnectionPool.getConnection()) {
            return orderDAO.searchOrdersForAdmin(connection, criteria);
        } catch (DAOException | SQLException e) {
            logger.error("Database error while searching orders with criteria: {}", criteria, e);
            throw new ServiceException("Unable to search orders due to an internal error", e);
        }
    }

    private OrderCreationContext validateAndCreatePendingOrder(String accountId, String directProductId,
                                                               boolean isFromCart) {
        Connection connection = null;
        boolean success = false;
        try {
            connection = ConnectionPool.getConnection();
            connection.setAutoCommit(false);

            AccountBean account = validateAccount(connection, accountId);

            String countryId = account.getCountryId();
            if (countryId == null) {
                logger.error("Country ID is null for Account: {}", accountId);
                throw new EntityNotFoundException("Country ID is null for Account: " + accountId);
            }

            CountryBean country = entityValidator.validateActiveCountry(connection, countryId);
            PaymentMethodBean paymentMethod = entityValidator.validateActiveDefaultPaymentMethod(connection, accountId);
            CurrencyBean currency = entityValidator.validateActiveCurrency(connection, account.getCurrencyId());
            AddressBean address = entityValidator.validateActiveDefaultAddress(connection, accountId);

            List<String> productIdsToValidate = new ArrayList<>();
            if (isFromCart) {
                List<CartBean> cartItems = cartDAO.findAllByAccountId(connection, accountId);
                if (cartItems.isEmpty()) {
                    throw new ServiceException("Cannot place an order with an empty cart");
                }
                for (CartBean item : cartItems) {
                    productIdsToValidate.add(item.getProductId());
                }
            } else {
                if (directProductId == null) {
                    throw new ServiceException("Product ID cannot be null for direct orders");
                }
                productIdsToValidate.add(directProductId);
            }

            Collections.sort(productIdsToValidate);
            List<String> acquiredLocks = new ArrayList<>();

            try {
                for (String productId : productIdsToValidate) {
                    String lockKey = accountId + "|" + productId;
                    if (purchaseLocks.putIfAbsent(lockKey, Boolean.TRUE) != null) {
                        throw new DuplicateEntityException("An order for this product is already being processed.");
                    }
                    acquiredLocks.add(lockKey);
                }

                List<OrderItemSnapshot> items = new ArrayList<>();
                BigDecimal tax = country.getTax();

                for (String productId : productIdsToValidate) {
                    boolean alreadyProcessing = orderDAO.existsActiveOrPending(connection, accountId, productId, 15);
                    if (alreadyProcessing) {
                        throw new DuplicateEntityException("An order for this product is already being processed.");
                    }

                    ProductBean product = entityValidator.validateProduct(connection, productId);
                    validateProductNotAlreadyOwned(connection, accountId, productId);

                    boolean stockOk = productDAO.decrementStock(connection, productId);
                    if (!stockOk) {
                        throw new InsufficientStockException("Product with id " + productId + " is out of stock.");
                    }

                    items.add(new OrderItemSnapshot(
                            product.getProductId(),
                            product.getPrice(),
                            product.getDiscount(),
                            tax
                    ));
                }

                BigDecimal totalAmount = calculateTotal(items);
                String orderId = UUID.randomUUID().toString();

                OrderBean order = new OrderBean(
                        orderId,
                        accountId,
                        address.getAddressId(),
                        currency.getCurrencyId(),
                        paymentMethod.getMethodType(),
                        OrderStatus.PENDING.getId(),
                        LocalDateTime.now(),
                        totalAmount,
                        ""
                );
                orderDAO.insert(connection, order);

                for (OrderItemSnapshot item : items) {
                    OrderItemBean orderItem = new OrderItemBean(
                            orderId, item.productId(), item.discount(), item.price(), item.tax()
                    );
                    orderItemDAO.insert(connection, orderItem);
                }

                connection.commit();
                success = true;

                return new OrderCreationContext(orderId, account, paymentMethod, currency, items, totalAmount,
                        isFromCart);

            } finally {
                for (String lockKey : acquiredLocks) {
                    purchaseLocks.remove(lockKey);
                }
            }

        } catch (SQLException e) {
            logger.error("Database error during order validation/creation for Account: {}", accountId, e);
            throw new ServiceException("Unable to process the order due to an internal error", e);
        } finally {
            if (connection != null) {
                if (!success) {
                    rollback(connection);
                }
                closeConnection(connection);
            }
        }
    }

    private String executeOrderProcessing(OrderCreationContext context) {
        String orderId = context.orderId();
        String accountId = context.account().getAccountId();

        String paymentToken = context.paymentMethod().getPaymentToken();
        BigDecimal amount = context.totalAmount();
        String currencyId = context.currency().getCurrencyId();

        ChargeResult chargeResult;
        try {
            chargeResult = paymentGateway.charge(paymentToken, amount, currencyId);
        } catch (Exception e) {
            logger.error("Network communication failure with Gateway for Order: {}. Payment outcome unknown, " +
                    "order marked for verification.", orderId, e);
            markOrderForReconciliation(orderId);
            throw new ServiceException(
                    "Unable to confirm the payment outcome due to a communication error. " +
                            "The order is under verification: please contact support with order ID " + orderId, e);
        }

        if (!chargeResult.success()) {
            logger.error("Payment declined for Account: {}. Error Code: {}", accountId, chargeResult.errorCode());
            safeCancelOrder(context, orderId);
            throw new PaymentDeclinedException("Payment failed with error code: " + chargeResult.errorCode());
        }

        String transactionId = chargeResult.transactionId();
        try {
            finalizeSuccessfulOrder(context, orderId, transactionId);
            logger.info("Order {} successfully processed and PAID for Account: {}", orderId, accountId);
        } catch (Exception e) {
            logger.error("CRITICAL ERROR: Charge completed (Tx: {}) " +
                    "but failed to persist PAID status in DB for Order: {}", transactionId, orderId, e);
            throw new ServiceException("Payment processed successfully but an error occurred during fulfillment. " +
                    "Please contact support with Transaction ID: " + transactionId, e);
        }

        return transactionId;
    }

    private void finalizeSuccessfulOrder(OrderCreationContext context, String orderId, String transactionId)
            throws SQLException {
        Connection connection = null;
        try {
            connection = ConnectionPool.getConnection();
            connection.setAutoCommit(false);

            String accountId = context.account().getAccountId();

            orderDAO.updateStatus(connection, orderId, OrderStatus.PAID.getId(), transactionId);

            for (OrderItemSnapshot item : context.items()) {
                AccountProductBean accountProduct = new AccountProductBean(
                        accountId, item.productId(), LocalDateTime.now()
                );
                accountProductDAO.insert(connection, accountProduct);
            }

            if (context.isFromCart()) {
                cartDAO.deleteAllByAccountId(connection, accountId);
            }

            connection.commit();
        } catch (Exception e) {
            rollback(connection);
            throw e;
        } finally {
            closeConnection(connection);
        }
    }

    private void safeCancelOrder(OrderCreationContext context, String orderId) {
        Connection connection = null;
        try {
            connection = ConnectionPool.getConnection();
            connection.setAutoCommit(false);

            orderDAO.updateStatus(connection, orderId, OrderStatus.CANCELLED.getId());

            for (OrderItemSnapshot item : context.items()) {
                productDAO.incrementStock(connection, item.productId());
            }

            connection.commit();
        } catch (Exception e) {
            rollback(connection);
            logger.error("FATAL ERROR: Failed to cancel order {} and restore stock units!", orderId, e);
        } finally {
            closeConnection(connection);
        }
    }

    private void markOrderForReconciliation(String orderId) {
        Connection connection = null;
        try {
            connection = ConnectionPool.getConnection();
            connection.setAutoCommit(false);

            orderDAO.updateStatus(connection, orderId, OrderStatus.PENDING_VERIFICATION.getId());

            connection.commit();
        } catch (Exception e) {
            rollback(connection);
            logger.error("FATAL ERROR: Failed to mark order {} as PENDING_VERIFICATION after gateway timeout!",
                    orderId, e);
        } finally {
            closeConnection(connection);
        }
    }

    private BigDecimal calculateTotal(List<OrderItemSnapshot> items) {
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemSnapshot item : items) {
            BigDecimal discountFactor = BigDecimal.ONE.subtract(item.discount().divide(HUNDRED, 4,
                    RoundingMode.HALF_UP));
            BigDecimal priceAfterDiscount = item.price().multiply(discountFactor);

            BigDecimal taxFactor = BigDecimal.ONE.add(item.tax().divide(HUNDRED, 4, RoundingMode.HALF_UP));
            BigDecimal finalItemPrice = priceAfterDiscount.multiply(taxFactor).setScale(2,
                    RoundingMode.HALF_UP);

            total = total.add(finalItemPrice);
        }

        return total;
    }

    private AccountBean validateAccount(Connection connection, String accountId) throws SQLException {
        return accountDAO.findById(connection, accountId).orElseThrow(() ->
                new EntityNotFoundException("Account not found for ID: " + accountId, EntityNotFoundException.EntityType.ACCOUNT));
    }

    private void validateProductNotAlreadyOwned(Connection connection, String accountId, String productId)
            throws SQLException {
        boolean isOwned = accountProductDAO.findById(connection, accountId, productId).isPresent();
        if (isOwned) {
            logger.error("Account: {} already owns Product: {}", accountId, productId);
            throw new DuplicateEntityException("Account already owns this product");
        }
    }

    private void rollback(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                logger.error("Rollback failed", e);
            }
        }
    }

    private void closeConnection(Connection connection) {
        if (connection == null) return;

        try (connection) {
            try {
                if (!connection.isClosed()) {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                logger.warn("Failed to reset autoCommit to true before releasing connection", e);
            }
        } catch (SQLException e) {
            logger.error("Failed to close database connection", e);
        }
    }
}