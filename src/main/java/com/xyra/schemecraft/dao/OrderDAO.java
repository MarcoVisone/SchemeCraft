package com.xyra.schemecraft.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import com.xyra.schemecraft.dto.OrderAdminView;
import com.xyra.schemecraft.dto.OrderSearchCriteria;
import com.xyra.schemecraft.exception.DAOException;
import com.xyra.schemecraft.exception.DuplicateEntityException;
import com.xyra.schemecraft.model.OrderBean;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

/**
 * Data Access Object (DAO) for managing persistent {@link OrderBean} entities.
 */
public class OrderDAO extends BaseDAO {

    private static final String SELECT_BASE = "SELECT order_id, account_id, address_id, currency_id, method_type, " +
            "status, created_at, total_amount, transaction_id FROM order_table ";

    private static final Map<String, String> ORDER_BY_COLUMN_MAP = Map.of(
            "orderId", "o.order_id",
            "createdAt", "o.created_at",
            "totalAmount", "o.total_amount",
            "status", "o.status",
            "accountId", "o.account_id",
            "customerId", "o.account_id",
            "username", "a.username",
            "customerUsername", "a.username",
            "email", "a.email",
            "customerEmail", "a.email"
    );

    /**
     * Inserts a new Order record into the database.
     *
     * @param conn  Active database connection
     * @param order The Order bean to persist
     * @throws DuplicateEntityException if the Order ID already exists
     * @throws DAOException             if a generic database error occurs
     * @throws IllegalArgumentException if the order is null, or if Order ID or Account ID are null or empty
     */
    public void insert(Connection conn, OrderBean order) throws DAOException {
        if (order == null) {
            throw new IllegalArgumentException("Cannot insert a null Order");
        }
        if (order.getOrderId() == null || order.getOrderId().trim().isEmpty() ||
                order.getAccountId() == null || order.getAccountId().trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID and Account ID must be valid and populated");
        }

        String sql = "INSERT INTO order_table (order_id, account_id, address_id, currency_id, " +
                "method_type, status, total_amount, transaction_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, order.getOrderId());
            ps.setString(2, order.getAccountId());
            ps.setString(3, order.getAddressId());
            ps.setString(4, order.getCurrencyId());
            ps.setInt(5, order.getMethodType());
            ps.setInt(6, order.getStatus());
            ps.setBigDecimal(7, order.getTotalAmount());
            ps.setString(8, order.getTransactionId());

            ps.executeUpdate();
            logger.info("Order successfully created with Order ID: {} for Account ID: {}", order.getOrderId(),
                    order.getAccountId());
        } catch (SQLException e) {
            logger.error("Failed to insert order with Order ID: {}", order.getOrderId(), e);
            if (SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION.equals(e.getSQLState()) ||
                    e.getErrorCode() == MYSQL_ERR_DUPLICATE_KEY) {
                throw new DuplicateEntityException("Order ID already exists: " + order.getOrderId(), e);
            }
            throw new DAOException("Error occurred while processing order insertion", e);
        }
    }

    /**
     * Retrieves an order by its unique identifier.
     *
     * @param conn    Active database connection
     * @param orderId Unique identifier of the target order
     * @return An Optional containing the populated bean, or empty if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the orderId is null or empty
     */
    public Optional<OrderBean> findById(Connection conn, String orderId) throws DAOException {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty for lookup");
        }

        String sql = SELECT_BASE + "WHERE order_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching order with Order ID: {}", orderId, e);
            throw new DAOException("Error fetching order by ID", e);
        }
        return Optional.empty();
    }

    /**
     * Retrieves a paginated list of orders placed by a specific account, ordered newest first.
     *
     * @param conn       Active database connection
     * @param accountId  Unique identifier of the customer account
     * @param pageNumber Page index (1-based)
     * @param pageSize   Number of records per page
     * @return List of orders representing the user history segment
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the accountId is null or empty
     */
    public List<OrderBean> findAllByAccountId(Connection conn, String accountId, int pageNumber, int pageSize)
            throws DAOException {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty for retrieval query");
        }

        String sql = SELECT_BASE + "WHERE account_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<OrderBean> orders = new ArrayList<>();

        int limit = pageSize < 1 ? 10 : pageSize;
        int offset = (pageNumber < 1 ? 0 : pageNumber - 1) * limit;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving orders for Account ID: {}", accountId, e);
            throw new DAOException("Error retrieving account order history", e);
        }
        return orders;
    }

    /**
     * Executes a dynamic search for admin order listing based on specified criteria,
     * including filters, sorting, and pagination. Performs a JOIN with the account table
     * to populate customer information.
     *
     * @param conn     Active database connection
     * @param criteria Object containing filters, pagination, and sorting preferences
     * @return List of matching {@link OrderAdminView} DTOs
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if criteria or connection is null
     */
    public List<OrderAdminView> searchOrdersForAdmin(Connection conn, OrderSearchCriteria criteria)
            throws DAOException {
        if (criteria == null) {
            throw new IllegalArgumentException("Search criteria cannot be null");
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT o.order_id, o.created_at, o.total_amount, o.status, ")
                .append("o.account_id, a.username, a.email ")
                .append("FROM order_table o ")
                .append("JOIN account a ON o.account_id = a.account_id ")
                .append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (criteria.getDateFrom() != null) {
            sql.append("AND o.created_at >= ? ");
            params.add(Timestamp.valueOf(criteria.getDateFrom()));
        }

        if (criteria.getDateTo() != null) {
            sql.append("AND o.created_at <= ? ");
            params.add(Timestamp.valueOf(criteria.getDateTo()));
        }

        if (criteria.getCustomerId() != null && !criteria.getCustomerId().isEmpty()) {
            sql.append("AND o.account_id = ? ");
            params.add(criteria.getCustomerId());
        }

        if (criteria.getCustomerUsername() != null && !criteria.getCustomerUsername().isEmpty()) {
            sql.append("AND LOWER(a.username) LIKE LOWER(?) ");
            params.add("%" + criteria.getCustomerUsername() + "%");
        }

        if (criteria.getCustomerEmail() != null && !criteria.getCustomerEmail().isEmpty()) {
            sql.append("AND LOWER(a.email) LIKE LOWER(?) ");
            params.add("%" + criteria.getCustomerEmail() + "%");
        }

        if (criteria.getStatus() != null) {
            sql.append("AND o.status = ? ");
            params.add(criteria.getStatus());
        }

        String rawOrderBy = criteria.getOrderByColumn();
        String targetColumn = null;

        if (rawOrderBy != null) {
            String cleanKey = rawOrderBy.replace("_", "").replaceAll("\\s+", "");

            for (Map.Entry<String, String> entry : ORDER_BY_COLUMN_MAP.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(cleanKey)) {
                    targetColumn = entry.getValue();
                    break;
                }
            }
        }

        if (targetColumn != null) {
            sql.append("ORDER BY ").append(targetColumn).append(" ");
        } else {
            sql.append("ORDER BY o.created_at ");
        }

        if (Boolean.TRUE.equals(criteria.getAscending())) {
            sql.append("ASC ");
        } else {
            sql.append("DESC ");
        }

        int limit = criteria.getPageSize();
        int offset = (criteria.getPageNumber() - 1) * limit;

        sql.append("LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        List<OrderAdminView> resultList = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp createdAtTs = rs.getTimestamp("created_at");
                    LocalDateTime createdAt = (createdAtTs != null) ? createdAtTs.toLocalDateTime() : null;

                    OrderAdminView view = new OrderAdminView(
                            rs.getString("order_id"),
                            createdAt,
                            rs.getBigDecimal("total_amount"),
                            rs.getInt("status"),
                            rs.getString("account_id"),
                            rs.getString("username"),
                            rs.getString("email")
                    );
                    resultList.add(view);
                }
            }
        } catch (SQLException e) {
            logger.error("Error executing admin order dynamic search query", e);
            throw new DAOException("Error searching orders for admin view", e);
        }

        return resultList;
    }

    /**
     * Updates all persistent details of an existing Order using its unique ID.
     *
     * @param conn    Active database connection
     * @param orderId Unique identifier of the order to update
     * @param order   Order model containing the new details
     * @return true if the row was updated; false if the ID was not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the orderId is null or empty, or if the order is null
     */
    public boolean update(Connection conn, String orderId, OrderBean order) throws DAOException {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty for updates");
        }
        if (order == null) {
            throw new IllegalArgumentException("Cannot update with a null Order object");
        }

        String sql = "UPDATE order_table SET account_id = ?, address_id = ?, currency_id = ?, " +
                "method_type = ?, status = ?, total_amount = ?, transaction_id = ? WHERE order_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, order.getAccountId());
            ps.setString(2, order.getAddressId());
            ps.setString(3, order.getCurrencyId());
            ps.setInt(4, order.getMethodType());
            ps.setInt(5, order.getStatus());
            ps.setBigDecimal(6, order.getTotalAmount());
            ps.setString(7, order.getTransactionId());
            ps.setString(8, orderId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Order with Order ID: {} successfully updated", orderId);
                return true;
            } else {
                logger.warn("Update issued for non-existent Order ID: {}", orderId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update order with Order ID: {}", orderId, e);
            throw new DAOException("Error updating order data", e);
        }
        return false;
    }

    /**
     * Updates all persistent details of an existing Order using its domain model representation.
     *
     * @param conn  Active database connection
     * @param order Order model containing updated details and unique identifier
     * @return true if the row was updated; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the order is null or does not have a valid ID
     */
    public boolean update(Connection conn, OrderBean order) throws DAOException {
        if (order == null || order.getOrderId() == null) {
            throw new IllegalArgumentException("Attempted to update a null order or an order without an ID");
        }
        return update(conn, order.getOrderId(), order);
    }

    /**
     * Updates the status of an order using its unique ID.
     *
     * @param conn      Active database connection
     * @param orderId   Unique identifier of the order
     * @param newStatus New status index to set
     * @return true if the status changed; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the orderId is null or empty
     */
    public boolean updateStatus(Connection conn, String orderId, int newStatus) throws DAOException {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty for status update");
        }

        String sql = "UPDATE order_table SET status = ? WHERE order_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newStatus);
            ps.setString(2, orderId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Status for Order ID: {} successfully updated to: {}", orderId, newStatus);
                return true;
            } else {
                logger.warn("Status update issued for non-existent Order ID: {}", orderId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update status for Order ID: {}", orderId, e);
            throw new DAOException("Error updating order status", e);
        }
        return false;
    }

    /**
     * Updates the status of an order using its domain model representation.
     *
     * @param conn      Active database connection
     * @param order     Order model containing the identifier of the target order
     * @param newStatus New status index to set
     * @return true if the status changed; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the order is null or does not have a valid ID
     */
    public boolean updateStatus(Connection conn, OrderBean order, int newStatus) throws DAOException {
        if (order == null || order.getOrderId() == null) {
            throw new IllegalArgumentException("Attempted to update status on a null order or an order without an ID");
        }
        return updateStatus(conn, order.getOrderId(), newStatus);
    }

    /**
     * Checks if a user has an active or pending order containing a specific product,
     * factoring in expiration minutes for uncompleted checkouts.
     *
     * @param conn                     Active database connection
     * @param accountId                Unique identifier of the customer account
     * @param productId                Unique identifier of the product
     * @param pendingExpirationMinutes Time threshold for pending orders to be considered valid
     * @return true if an active/pending order exists; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Account ID or Product ID are null or empty
     */
    public boolean existsActiveOrPending(Connection conn, String accountId, String productId,
                                         int pendingExpirationMinutes) throws DAOException {
        if (accountId == null || accountId.trim().isEmpty() || productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID and Product ID must be valid and " +
                    "populated for constraint checks");
        }

        final int PENDING_STATUS_ID = 1;
        final int CANCELLED_STATUS_ID = 4;

        String sql = "SELECT 1 FROM order_table o JOIN order_item oi ON oi.order_id = o.order_id " +
                "WHERE o.account_id = ? AND oi.product_id = ? AND o.status <> ? " +
                "AND NOT (o.status = ? AND o.created_at < ?) LIMIT 1";

        Timestamp expirationThreshold = Timestamp.valueOf(LocalDateTime.now().minusMinutes(pendingExpirationMinutes));

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            ps.setString(2, productId);
            ps.setInt(3, CANCELLED_STATUS_ID);
            ps.setInt(4, PENDING_STATUS_ID);
            ps.setTimestamp(5, expirationThreshold);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Database error while checking active/pending order for Account ID: {} and Product ID: {}",
                    accountId, productId, e);
            throw new DAOException("Error checking for active or pending order", e);
        }
    }

    /**
     * Updates an order's status and records the payment Gateway Transaction ID.
     *
     * @param conn          Active database connection
     * @param orderId       Unique identifier of the order
     * @param newStatus     New status index to set
     * @param transactionId Reference ID generated by the payment gateway (e.g., Stripe, PayPal)
     * @return true if the record was updated; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Order ID or Transaction ID are null or empty
     */
    public boolean updateStatus(Connection conn, String orderId, int newStatus, String transactionId)
            throws DAOException {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
        if (transactionId == null || transactionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction ID cannot be null or empty for completed statuses");
        }

        String sql = "UPDATE order_table SET status = ?, transaction_id = ? WHERE order_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newStatus);
            ps.setString(2, transactionId);
            ps.setString(3, orderId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Successfully updated status to: {} and Transaction ID to: {} for Order ID: {}",
                        newStatus, transactionId, orderId);
                return true;
            } else {
                logger.warn("Status/Transaction update issued for non-existent Order ID: {}", orderId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update status and Transaction ID for Order ID: {}", orderId, e);
            throw new DAOException("Error updating order completion details", e);
        }
        return false;
    }

    /**
     * Updates an order's status and records the payment Gateway Transaction ID using its domain model representation.
     *
     * @param conn          Active database connection
     * @param order         Order model containing the identifier of the target order
     * @param newStatus     New status index to set
     * @param transactionId Reference ID generated by the payment gateway (e.g., Stripe, PayPal)
     * @return true if the record was updated; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the order is null, or if Order ID or Transaction ID are null or empty
     */
    public boolean updateStatus(Connection conn, OrderBean order, int newStatus, String transactionId)
            throws DAOException {
        if (order == null || order.getOrderId() == null) {
            throw new IllegalArgumentException("Order ID and Transaction ID cannot be null");
        }
        return updateStatus(conn, order.getOrderId(), newStatus, transactionId);
    }

    /**
     * Hard-deletes an order from the database using its unique ID.
     *
     * @param conn    Active database connection
     * @param orderId Unique identifier of the order to delete
     * @return true if the order record was deleted; false if the ID was not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the orderId is null or empty
     */
    public boolean delete(Connection conn, String orderId) throws DAOException {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty for deletion");
        }

        String sql = "DELETE FROM order_table WHERE order_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Order with Order ID: {} successfully deleted from database", orderId);
                return true;
            } else {
                logger.warn("Delete issued for non-existent Order ID: {}", orderId);
            }
        } catch (SQLException e) {
            logger.error("Failed to delete order with Order ID: {}. Note: RESTRICT constraint might prevent this.",
                    orderId, e);
            throw new DAOException("Error deleting order record", e);
        }
        return false;
    }

    /**
     * Hard-deletes an order from the database using its domain model representation.
     *
     * @param conn  Active database connection
     * @param order Order model containing the identifier of the order to delete
     * @return true if the order record was deleted; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the order is null or does not have a valid ID
     */
    public boolean delete(Connection conn, OrderBean order) throws DAOException {
        if (order == null || order.getOrderId() == null) {
            throw new IllegalArgumentException("Attempted to delete a null order or an order without an ID");
        }
        return delete(conn, order.getOrderId());
    }

    /**
     * Maps a database row from a {@link ResultSet} into an {@link OrderBean}.
     */
    private OrderBean mapRow(ResultSet rs) throws SQLException {
        OrderBean order = new OrderBean();
        order.setOrderId(rs.getString("order_id"));
        order.setAccountId(rs.getString("account_id"));
        order.setAddressId(rs.getString("address_id"));
        order.setCurrencyId(rs.getString("currency_id"));
        order.setMethodType(rs.getInt("method_type"));
        order.setStatus(rs.getInt("status"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setTransactionId(rs.getString("transaction_id"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            order.setCreatedAt(createdAt.toLocalDateTime());
        }
        return order;
    }
}
