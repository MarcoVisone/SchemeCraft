package com.xyra.schemecraft.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.xyra.schemecraft.exception.DAOException;
import com.xyra.schemecraft.exception.DuplicateEntityException;
import com.xyra.schemecraft.model.Order;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

public class OrderDAO extends BaseDAO {
    private static final String SELECT_BASE = "SELECT order_id, account_id, address_id, currency_id, method_type, " +
            "status, created_at, total_amount, transaction_id FROM order_table";

    public void insert(Connection conn, Order order) throws DAOException {
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

    public Optional<Order> findById(Connection conn, String orderId) throws DAOException {
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

    public List<Order> findAllByAccountId(Connection conn, String accountId, int pageNumber, int pageSize)
            throws DAOException {
        String sql = SELECT_BASE + " WHERE account_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<Order> orders = new ArrayList<>();

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

    public boolean update(Connection conn, String orderId, Order order) throws DAOException {
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

    public boolean update(Connection conn, Order order) throws DAOException {
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

    public boolean updateStatus(Connection conn, Order order, int newStatus) throws DAOException {
        if (order == null || order.getOrderId() == null) {
            throw new IllegalArgumentException("Attempted to update status on a null order or an order without an ID");
        }
        return updateStatus(conn, order.getOrderId(), newStatus);
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

    public boolean delete(Connection conn, Order order) throws DAOException {
        if (order == null || order.getOrderId() == null) {
            throw new IllegalArgumentException("Attempted to delete a null order or an order without an ID");
        }
        return delete(conn, order.getOrderId());
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        Order order = new Order();
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
