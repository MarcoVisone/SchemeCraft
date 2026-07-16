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

/**
 * Data Access Object (DAO) for managing persistent {@link OrderItemBean} entities.
 */
public class OrderItemDAO extends BaseDAO {

    private static final String SELECT_BASE = "SELECT order_id, product_id, discount, price, tax FROM order_item ";

    /**
     * Inserts a new OrderItem record into the database.
     *
     * @param conn Active database connection
     * @param item The OrderItem bean to persist
     * @throws DuplicateEntityException if this product is already present in the specified order
     * @throws DAOException             if a generic database error occurs
     * @throws IllegalArgumentException if the item is null, or if Order ID or Product ID are null or empty
     */
    public void insert(Connection conn, OrderItemBean item) throws DAOException {
        if (item == null) {
            throw new IllegalArgumentException("Cannot insert a null OrderItem");
        }
        if (item.getOrderId() == null || item.getOrderId().trim().isEmpty() ||
                item.getProductId() == null || item.getProductId().trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID and Product ID must be valid and populated");
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
            logger.error("Failed to insert order item for Order ID: {} and Product ID: {}", item.getOrderId(),
                    item.getProductId(), e);
            if (SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION.equals(e.getSQLState()) ||
                    e.getErrorCode() == MYSQL_ERR_DUPLICATE_KEY) {
                throw new DuplicateEntityException("The specified Product ID is already present in this Order ID", e);
            }
            throw new DAOException("Error occurred while saving order item", e);
        }
    }

    /**
     * Finds a specific OrderItem by its composite primary key.
     *
     * @param conn      Active database connection
     * @param orderId   Unique identifier of the order
     * @param productId Unique identifier of the product
     * @return An Optional containing the populated bean, or empty if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the Order ID or Product ID are null or empty
     */
    public Optional<OrderItemBean> findById(Connection conn, String orderId, String productId) throws DAOException {
        if (orderId == null || orderId.trim().isEmpty() || productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID and Product ID cannot be null or empty for lookup");
        }

        String sql = SELECT_BASE + "WHERE order_id = ? AND product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            ps.setString(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching order item for Order ID: {} and Product ID: {}",
                    orderId, productId, e);
            throw new DAOException("Error fetching order item", e);
        }
        return Optional.empty();
    }

    /**
     * Retrieves all items registered within a given order.
     *
     * @param conn    Active database connection
     * @param orderId Unique identifier of the order
     * @return List of all order items associated with the order
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the Order ID is null or empty
     */
    public List<OrderItemBean> findAllByOrderId(Connection conn, String orderId) throws DAOException {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }

        String sql = SELECT_BASE + "WHERE order_id = ?";
        List<OrderItemBean> items = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving items for Order ID: {}", orderId, e);
            throw new DAOException("Error retrieving items for the specified order", e);
        }
        return items;
    }

    /**
     * Updates prices, tax, or discounts of an existing OrderItem.
     *
     * @param conn      Active database connection
     * @param orderId   Unique identifier of the order
     * @param productId Unique identifier of the product
     * @param item      OrderItem model with updated values
     * @return true if the row was updated; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the Order ID or Product ID are null or empty, or if the item is null
     */
    public boolean update(Connection conn, String orderId, String productId, OrderItemBean item) throws DAOException {
        if (orderId == null || orderId.trim().isEmpty() || productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID and Product ID cannot be null or empty for updates");
        }
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
                logger.info("Order item with Product ID: {} in Order ID: {} successfully updated", productId, orderId);
                return true;
            } else {
                logger.warn("Update issued for non-existent order item with Product ID: {} in Order ID: {}", productId,
                        orderId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update order item for Order ID: {} and Product ID: {}", orderId, productId, e);
            throw new DAOException("Error updating order item data", e);
        }
        return false;
    }

    /**
     * Updates prices, tax, or discounts of an existing OrderItem using its domain model representation.
     *
     * @param conn Active database connection
     * @param item OrderItem model containing updated details and composite identifiers
     * @return true if the row was updated; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the item is null, or if Order ID or Product ID are null
     */
    public boolean update(Connection conn, OrderItemBean item) throws DAOException {
        if (item == null || item.getOrderId() == null || item.getProductId() == null) {
            throw new IllegalArgumentException("Attempted to update a null order item " +
                    "or an object with missing composite keys");
        }
        return update(conn, item.getOrderId(), item.getProductId(), item);
    }

    /**
     * Deletes a product from a specific order using composite keys.
     *
     * @param conn      Active database connection
     * @param orderId   Unique identifier of the order
     * @param productId Unique identifier of the product to remove
     * @return true if the record was deleted; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the Order ID or Product ID are null or empty
     */
    public boolean delete(Connection conn, String orderId, String productId) throws DAOException {
        if (orderId == null || orderId.trim().isEmpty() || productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID and Product ID cannot be null or empty for deletion");
        }

        String sql = "DELETE FROM order_item WHERE order_id = ? AND product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            ps.setString(2, productId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Order item with Product ID: {} from Order ID: {} successfully deleted", productId,
                        orderId);
                return true;
            } else {
                logger.warn("Delete issued for non-existent order item with Product ID: {} in Order ID: {}", productId,
                        orderId);
            }
        } catch (SQLException e) {
            logger.error("Failed to delete order item for Order ID: {} and Product ID: {}. " +
                            "Note: RESTRICT constraint might prevent this.",
                    orderId, productId, e);
            throw new DAOException("Error deleting order item record", e);
        }
        return false;
    }

    /**
     * Deletes a product from a specific order using its domain model representation.
     *
     * @param conn Active database connection
     * @param item OrderItem model containing composite identifiers of the record to remove
     * @return true if the record was deleted; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the item is null, or if Order ID or Product ID are null
     */
    public boolean delete(Connection conn, OrderItemBean item) throws DAOException {
        if (item == null || item.getOrderId() == null || item.getProductId() == null) {
            throw new IllegalArgumentException("Attempted to delete a null order item " +
                    "or an object with missing composite keys");
        }
        return delete(conn, item.getOrderId(), item.getProductId());
    }

    /**
     * Maps a database row from a {@link ResultSet} into an {@link OrderItemBean}.
     */
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
