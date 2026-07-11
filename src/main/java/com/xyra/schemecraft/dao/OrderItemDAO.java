package com.xyra.schemecraft.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.xyra.schemecraft.exception.DAOException;
import com.xyra.schemecraft.exception.DuplicateEntityException;
import com.xyra.schemecraft.model.OrderItemBean;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

public class OrderItemDAO extends BaseDAO {
    private static final String SELECT_BASE = "SELECT order_id, product_id, discount, price, tax FROM order_item";

    public void insert(Connection conn, OrderItemBean item) throws DAOException {
        if (item == null) {
            throw new IllegalArgumentException("Cannot insert a null OrderItem");
        }

        String sql = "INSERT INTO order_item (order_id, product_id, discount, price, tax) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getOrderId());
            ps.setString(2, item.getProductId());
            ps.setBigDecimal(3, item.getDiscount());
            ps.setBigDecimal(4, item.getPrice());
            ps.setBigDecimal(5, item.getTax());

            ps.executeUpdate();
            logger.info("Product ID: {} successfully added to Order ID: {}", item.getProductId(), item.getOrderId());
        } catch (SQLException e) {
            logger.error("Failed to insert order item for order {} and product {}", item.getOrderId(),
                    item.getProductId(), e);
            if (MYSQL_DUPLICATE_KEY_STATE.equals(e.getSQLState()) || e.getErrorCode() == MYSQL_DUPLICATE_KEY_CODE) {
                throw new DuplicateEntityException("This product is already present in this order", e);
            }
            throw new DAOException("Error occurred while saving order item", e);
        }
    }

    public Optional<OrderItemBean> findById(Connection conn, String orderId, String productId) throws DAOException {
        String sql = SELECT_BASE + " WHERE order_id = ? AND product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            ps.setString(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching order item for order {} and product {}",
                    orderId, productId, e);
            throw new DAOException("Error fetching order item", e);
        }
        return Optional.empty();
    }

    public List<OrderItemBean> findAllByOrderId(Connection conn, String orderId) throws DAOException {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }

        String sql = SELECT_BASE + " WHERE order_id = ?";
        List<OrderItemBean> items = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving items for order ID: {}", orderId, e);
            throw new DAOException("Error retrieving items for the specified order", e);
        }
        return items;
    }

    public boolean update(Connection conn, String orderId, String productId, OrderItemBean item) throws DAOException {
        if (item == null) {
            throw new IllegalArgumentException("Cannot update with a null OrderItem object");
        }

        String sql = "UPDATE order_item SET discount = ?, price = ?, tax = ? WHERE order_id = ? AND product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, item.getDiscount());
            ps.setBigDecimal(2, item.getPrice());
            ps.setBigDecimal(3, item.getTax());
            ps.setString(4, orderId);
            ps.setString(5, productId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Order item (Product: {}) in Order: {} successfully updated", productId, orderId);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Failed to update order item for order {} and product {}", orderId, productId, e);
            throw new DAOException("Error updating order item data", e);
        }
        return false;
    }

    public boolean update(Connection conn, OrderItemBean item) throws DAOException {
        if (item == null || item.getOrderId() == null || item.getProductId() == null) {
            throw new IllegalArgumentException("Attempted to update a null order item " +
                    "or an object with missing composite keys");
        }
        return update(conn, item.getOrderId(), item.getProductId(), item);
    }

    public boolean delete(Connection conn, String orderId, String productId) throws DAOException {
        String sql = "DELETE FROM order_item WHERE order_id = ? AND product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            ps.setString(2, productId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Failed to delete order item for order {} and product {}. " +
                            "Note: RESTRICT constraint might prevent this.",
                    orderId, productId, e);
            throw new DAOException("Error deleting order item record", e);
        }
    }

    public boolean delete(Connection conn, OrderItemBean item) throws DAOException {
        if (item == null || item.getOrderId() == null || item.getProductId() == null) {
            throw new IllegalArgumentException("Attempted to delete a null order item " +
                    "or an object with missing composite keys");
        }
        return delete(conn, item.getOrderId(), item.getProductId());
    }

    private OrderItemBean mapRow(ResultSet rs) throws SQLException {
        OrderItemBean item = new OrderItemBean();
        item.setOrderId(rs.getString("order_id"));
        item.setProductId(rs.getString("product_id"));
        item.setDiscount(rs.getBigDecimal("discount"));
        item.setPrice(rs.getBigDecimal("price"));
        item.setTax(rs.getBigDecimal("tax"));
        return item;
    }
}
