package com.xyra.schemecraft.service;

import com.xyra.schemecraft.connection.ConnectionPool;
import com.xyra.schemecraft.dao.*;
import com.xyra.schemecraft.exception.*;
import com.xyra.schemecraft.model.*;
import com.xyra.schemecraft.service.gateway.ChargeResult;
import com.xyra.schemecraft.service.gateway.FakePaymentGateway;
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
    private final PaymentMethodDAO paymentMethodDAO;
    private final CartDAO cartDAO;
    private final OrderDAO orderDAO;
    private final OrderItemDAO orderItemDAO;
    private final PaymentMethodTypeDAO paymentMethodTypeDAO;
    private final AddressDAO addressDAO;
    private final CountryDAO countryDAO;
    private final AccountDAO accountDAO;
    private final CurrencyDAO currencyDAO;
    private final AccountProductDAO accountProductDAO;
    private final FakePaymentGateway paymentGateway;

    public OrderService() {
        this.productDAO = new ProductDAO();
        this.paymentMethodDAO = new PaymentMethodDAO();
        this.cartDAO = new CartDAO();
        this.orderDAO = new OrderDAO();
        this.orderItemDAO = new OrderItemDAO();
        this.paymentMethodTypeDAO = new PaymentMethodTypeDAO();
        this.addressDAO = new AddressDAO();
        this.countryDAO = new CountryDAO();
        this.accountDAO = new AccountDAO();
        this.currencyDAO = new CurrencyDAO();
        this.accountProductDAO = new AccountProductDAO();
        this.paymentGateway = new FakePaymentGateway();
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

    public OrderBean getOrderById(String orderId) throws EntityNotFoundException {
        throw new UnsupportedOperationException("TODO: getOrderById");
    }

    public List<OrderBean> listAccountOrders(String accountId) {
        throw new UnsupportedOperationException("TODO: listAccountOrders");
    }

    public void updateOrderStatus(String orderId, int statusId) throws EntityNotFoundException {
        throw new UnsupportedOperationException("TODO: updateOrderStatus");
    }

    private OrderCreationContext validateAndCreatePendingOrder(String accountId, String directProductId,
                                                               boolean isFromCart) {
        Connection connection = null;
        try {
            connection = ConnectionPool.getConnection();
            connection.setAutoCommit(false);

            AccountBean account = validateAccount(connection, accountId);
            CountryBean country = validateAccountCountry(connection, account, accountId);
            PaymentMethodBean paymentMethod = validateDefaultPaymentMethod(connection, accountId);
            CurrencyBean currency = validateCurrency(connection, account.getCurrencyId());
            AddressBean address = validateAddress(connection, accountId);

            List<String> productIdsToValidate = new ArrayList<>();

            if (isFromCart) {
                List<CartBean> cartItems = cartDAO.findAllByAccountId(connection, accountId);
                if (cartItems.isEmpty()) {
                    logger.error("Cart is empty for Account: {}", accountId);
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
                    boolean alreadyProcessing = orderDAO.existsActiveOrPending(connection, accountId, productId,
                            15);
                    if (alreadyProcessing) {
                        throw new DuplicateEntityException("An order for this product is already being " +
                                "processed or completed.");
                    }

                    ProductBean product = validateProduct(connection, productId);
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

                return new OrderCreationContext(orderId, account, paymentMethod, currency, items,
                        totalAmount, isFromCart);

            } finally {
                for (String lockKey : acquiredLocks) {
                    purchaseLocks.remove(lockKey);
                }
            }

        } catch (ServiceException e) {
            rollback(connection);
            throw e;
        } catch (SQLException e) {
            rollback(connection);
            logger.error("Database error during order validation/creation for Account: {}", accountId, e);
            throw new ServiceException("Unable to process the order due to an internal error", e);
        } finally {
            closeConnection(connection);
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
            // price * (1 - (discount / 100))
            BigDecimal discountFactor = BigDecimal.ONE.subtract(item.discount().divide(HUNDRED, 4,
                    RoundingMode.HALF_UP));
            BigDecimal priceAfterDiscount = item.price().multiply(discountFactor);

            // priceAfterDiscount * (1 + (tax / 100))
            BigDecimal taxFactor = BigDecimal.ONE.add(item.tax().divide(HUNDRED, 4, RoundingMode.HALF_UP));
            BigDecimal finalItemPrice = priceAfterDiscount.multiply(taxFactor).setScale(2,
                    RoundingMode.HALF_UP);

            total = total.add(finalItemPrice);
        }

        return total;
    }

    private AccountBean validateAccount(Connection connection, String accountId) throws SQLException {
        return accountDAO.findById(connection, accountId).orElseThrow(() -> {
            logger.error("Account not found for ID: {}", accountId);
            return new EntityNotFoundException("Account not found for ID: " + accountId);
        });
    }

    private CountryBean validateAccountCountry(Connection connection, AccountBean account, String accountId)
            throws SQLException {
        String countryId = account.getCountryId();

        if (countryId == null) {
            logger.error("Country ID is null for Account: {}", accountId);
            throw new EntityNotFoundException("Country ID is null for Account: " + accountId);
        }

        CountryBean country = countryDAO.findById(connection, countryId).orElseThrow(() -> {
            logger.error("Country not found for ID: {} (Account: {})", countryId, accountId);
            return new EntityNotFoundException("Country not found for ID: " + countryId);
        });

        if (!country.isActive()) {
            logger.error("Country is not active for Account: {}", accountId);
            throw new InactiveEntityException("Country is not active for Account: " + accountId);
        }
        return country;
    }

    private PaymentMethodBean validateDefaultPaymentMethod(Connection connection, String accountId)
            throws SQLException {
        PaymentMethodBean paymentMethod =
                paymentMethodDAO.findDefaultByAccountId(connection, accountId).orElseThrow(
                        () -> {
                            logger.error("No default payment method found for Account: {}", accountId);
                            return new EntityNotFoundException("No default payment method found for account " + accountId);
                        });

        int paymentMethodTypeId = paymentMethod.getMethodType();

        PaymentMethodTypeBean paymentMethodType =
                paymentMethodTypeDAO.findById(connection, paymentMethodTypeId).orElseThrow(() -> {
                    logger.error("Payment method type not found for ID: {}", paymentMethodTypeId);
                    return new EntityNotFoundException("Invalid payment method type");
                });

        if (!paymentMethodType.isActive()) {
            logger.error("Payment method type is not active for ID: {}", paymentMethodTypeId);
            throw new InactiveEntityException("PaymentMethodType with id " + paymentMethodTypeId + " is not active");
        }

        return paymentMethod;
    }

    private CurrencyBean validateCurrency(Connection connection, String currencyId) throws SQLException {
        CurrencyBean currency = currencyDAO.findById(connection, currencyId).orElseThrow(() -> {
            logger.error("Currency not found for ID: {}", currencyId);
            return new EntityNotFoundException("Currency not found for ID: " + currencyId);
        });

        if (!currency.isActive()) {
            logger.error("Currency is not active for ID: {}", currencyId);
            throw new InactiveEntityException("Currency with id " + currencyId + " is not active");
        }

        return currency;
    }

    private AddressBean validateAddress(Connection connection, String accountId) throws SQLException {
        AddressBean address = addressDAO.findDefaultByAccountId(connection, accountId).orElseThrow(() -> {
            logger.error("Address not found for Account ID: {}", accountId);
            return new EntityNotFoundException("Address not found for Account ID: " + accountId);
        });

        if (!address.isActive()) {
            logger.error("Address is not active for ID: {}", address.getAddressId());
            throw new InactiveEntityException("Address with id " + address.getAddressId() + " is not active");
        }

        return address;
    }

    private void validateProductNotAlreadyOwned(Connection connection, String accountId, String productId)
            throws SQLException {
        boolean isOwned = accountProductDAO.findById(connection, accountId, productId).isPresent();
        if (isOwned) {
            logger.error("Account: {} already owns Product: {}", accountId, productId);
            throw new DuplicateEntityException("Account already owns this product");
        }
    }

    private ProductBean validateProduct(Connection connection, String productId) throws SQLException {
        ProductBean product = productDAO.findById(connection, productId).orElseThrow(() -> {
            logger.error("Product not found for ID: {}", productId);
            return new EntityNotFoundException("Product not found for ID: " + productId);
        });

        if (!product.isActive()) {
            logger.error("Product is not active for ID: {}", productId);
            throw new InactiveEntityException("Product with id " + productId + " is not active");
        }

        if (product.getStockQuantity() != null && product.getStockQuantity() <= 0) {
            logger.error("Product is out of stock for ID: {}", productId);
            throw new InsufficientStockException("Product with id " + productId + " is out of stock");
        }
        return product;
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
        if (connection != null) {
            try (connection) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    logger.error("Error while resetting autoCommit on connection", e);
                }
            } catch (SQLException e) {
                logger.error("Error while releasing connection back to the pool", e);
            }
        }
    }
}
