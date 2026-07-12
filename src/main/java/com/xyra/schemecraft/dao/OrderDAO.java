package com.xyra.schemecraft.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.xyra.schemecraft.exception.DAOException;
import com.xyra.schemecraft.exception.DuplicateEntityException;
import com.xyra.schemecraft.model.OrderBean;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

public class OrderDAO extends BaseDAO {
    private static final String SELECT_BASE = "SELECT order_id, account_id, address_id, currency_id, method_type, " +
            "status, created_at, total_amount, transaction_id FROM order_table";

    public void insert(Connection conn, OrderBean order) throws DAOException {
        if (order == null) {
            throw new IllegalArgumentException("Cannot insert a null Order");
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
            logger.info("Order successfully created with ID: {} for Account: {}", order.getOrderId(),
                    order.getAccountId());
        } catch (SQLException e) {
            logger.error("Failed to insert order with ID: {}", order.getOrderId(), e);
            if (MYSQL_DUPLICATE_KEY_STATE.equals(e.getSQLState()) || e.getErrorCode() == MYSQL_DUPLICATE_KEY_CODE) {
                throw new DuplicateEntityException("Order ID already exists: " + order.getOrderId(), e);
            }
            throw new DAOException("Error occurred while processing order insertion", e);
        }
    }

    public Optional<OrderBean> findById(Connection conn, String orderId) throws DAOException {
        String sql = SELECT_BASE + " WHERE order_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching order with ID: {}", orderId, e);
            throw new DAOException("Error fetching order by ID", e);
        }
        return Optional.empty();
    }

    public List<OrderBean> findAllByAccountId(Connection conn, String accountId, int pageNumber, int pageSize)
            throws DAOException {
        String sql = SELECT_BASE + " WHERE account_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
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
            logger.error("Database error while retrieving orders for account ID: {}", accountId, e);
            throw new DAOException("Error retrieving account order history", e);
        }
        return orders;
    }

    public boolean update(Connection conn, String orderId, OrderBean order) throws DAOException {
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
                logger.info("Order with ID: {} successfully updated", orderId);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Failed to update order with ID: {}", orderId, e);
            throw new DAOException("Error updating order data", e);
        }
        return false;
    }

    public boolean update(Connection conn, OrderBean order) throws DAOException {
        if (order == null || order.getOrderId() == null) {
            throw new IllegalArgumentException("Attempted to update a null order or an order without an ID");
        }
        return update(conn, order.getOrderId(), order);
    }

    public boolean updateStatus(Connection conn, String orderId, int newStatus) throws DAOException {
        String sql = "UPDATE order_table SET status = ? WHERE order_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newStatus);
            ps.setString(2, orderId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Status for order ID: {} successfully updated to: {}", orderId, newStatus);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Failed to update status for order ID: {}", orderId, e);
            throw new DAOException("Error updating order status", e);
        }
        return false;
    }

    public boolean updateStatus(Connection conn, OrderBean order, int newStatus) throws DAOException {
        if (order == null || order.getOrderId() == null) {
            throw new IllegalArgumentException("Attempted to update status on a null order or an order without an ID");
        }
        return updateStatus(conn, order.getOrderId(), newStatus);
    }

    public boolean existsActiveOrPending(Connection conn, String accountId, String productId,
                                         int pendingExpirationMinutes) throws DAOException {
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
            logger.error("Database error while checking active/pending order for Account: {} Product: {}",
                    accountId, productId, e);
            throw new DAOException("Error checking for active or pending order", e);
        }
    }


    public boolean updateStatus(Connection conn, String orderId, int newStatus, String transactionId)
            throws DAOException {
        if (orderId == null || transactionId == null) {
            throw new IllegalArgumentException("Order ID and Transaction ID cannot be null");
        }

        String sql = "UPDATE order_table SET status = ?, transaction_id = ? WHERE order_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newStatus);
            ps.setString(2, transactionId);
            ps.setString(3, orderId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Successfully updated status to: {} and transaction ID to: {} for order: {}",
                        newStatus, transactionId, orderId);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Failed to update status and transaction ID for order: {}", orderId, e);
            throw new DAOException("Error updating order completion details", e);
        }
        return false;
    }

    public boolean updateStatus(Connection conn, OrderBean order, int newStatus, String transactionId)
            throws DAOException {
        if (order == null || order.getOrderId() == null) {
            throw new IllegalArgumentException("Order ID and Transaction ID cannot be null");
        }
        return updateStatus(conn, order.getOrderId(), newStatus, transactionId);
    }

    public boolean delete(Connection conn, String orderId) throws DAOException {
        String sql = "DELETE FROM order_table WHERE order_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Failed to delete order with ID: {}. Note: RESTRICT constraint might prevent this.",
                    orderId, e);
            throw new DAOException("Error deleting order record", e);
        }
    }

    public boolean delete(Connection conn, OrderBean order) throws DAOException {
        if (order == null || order.getOrderId() == null) {
            throw new IllegalArgumentException("Attempted to delete a null order or an order without an ID");
        }
        return delete(conn, order.getOrderId());
    }

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
