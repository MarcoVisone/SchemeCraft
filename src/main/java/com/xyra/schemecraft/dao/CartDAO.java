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
import com.xyra.schemecraft.model.CartBean;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

/**
 * Data Access Object (DAO) for managing persistent {@link CartBean} entities.
 */
public class CartDAO extends BaseDAO {

    private static final String SELECT_BASE = "SELECT account_id, product_id FROM cart ";

    /**
     * Inserts a product into an account's shopping cart.
     *
     * @param conn Active database connection
     * @param cart The cart item relationship model
     * @throws DuplicateEntityException if the product is already in the user's cart
     * @throws DAOException             if a generic database error occurs
     * @throws IllegalArgumentException if the cart is null or lacks valid composite keys
     */
    public void insert(Connection conn, CartBean cart) throws DAOException {
        if (cart == null) {
            throw new IllegalArgumentException("Cannot insert a null Cart association");
        }
        if (cart.getAccountId() == null || cart.getAccountId().trim().isEmpty() ||
                cart.getProductId() == null || cart.getProductId().trim().isEmpty()) {
            throw new IllegalArgumentException("Cart association keys must be valid and populated");
        }

        String sql = "INSERT INTO cart (account_id, product_id) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cart.getAccountId());
            ps.setString(2, cart.getProductId());

            ps.executeUpdate();
            logger.info("Product ID: {} successfully added to cart for Account ID: {}",
                    cart.getProductId(), cart.getAccountId());
        } catch (SQLException e) {
            logger.error("Failed to add Product ID: {} to cart for Account ID: {}", cart.getProductId(),
                    cart.getAccountId(), e);
            if (SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION.equals(e.getSQLState()) ||
                    e.getErrorCode() == MYSQL_ERR_DUPLICATE_KEY) {
                throw new DuplicateEntityException("Product is already in the cart for this account", e);
            }
            throw new DAOException("Error occurred while adding product to cart", e);
        }
    }

    /**
     * Checks if a specific product is currently in a given account's cart.
     *
     * @param conn      Active database connection
     * @param accountId Unique identifier of the account
     * @param productId Unique identifier of the product
     * @return An Optional containing the cart bean if found, or empty otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the accountId or productId is null or empty
     */
    public Optional<CartBean> findById(Connection conn, String accountId, String productId) throws DAOException {
        if (accountId == null || accountId.trim().isEmpty() || productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID and Product ID cannot be null or empty for lookup");
        }

        String sql = SELECT_BASE + "WHERE account_id = ? AND product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            ps.setString(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while checking cart item for Account ID: {} and Product ID: {}",
                    accountId, productId, e);
            throw new DAOException("Error fetching cart association", e);
        }
        return Optional.empty();
    }

    /**
     * Retrieves all products currently stored in a given account's cart.
     *
     * @param conn      Active database connection
     * @param accountId Unique identifier of the target account
     * @return List of all cart items linked to the account
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the accountId is null or empty
     */
    public List<CartBean> findAllByAccountId(Connection conn, String accountId) throws DAOException {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty for retrieval query");
        }

        String sql = SELECT_BASE + "WHERE account_id = ?";
        List<CartBean> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving cart for Account ID: {}", accountId, e);
            throw new DAOException("Error retrieving cart items by account ID", e);
        }
        return list;
    }

    /**
     * Removes a single product from an account's cart using its unique composite identifiers.
     *
     * @param conn      Active database connection
     * @param accountId Unique identifier of the account
     * @param productId Unique identifier of the product to remove
     * @return true if the product was found and removed; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the accountId or productId is null or empty
     */
    public boolean delete(Connection conn, String accountId, String productId) throws DAOException {
        if (accountId == null || accountId.trim().isEmpty() || productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID and Product ID cannot be null or empty for deletion");
        }

        String sql = "DELETE FROM cart WHERE account_id = ? AND product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            ps.setString(2, productId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Product ID: {} successfully removed from cart for Account ID: {}", productId, accountId);
                return true;
            } else {
                logger.warn("No active cart item found to delete for Account ID: {} and Product ID: {}",
                        accountId, productId);
            }
        } catch (SQLException e) {
            logger.error("Failed to remove Product ID: {} from cart for Account ID: {}", productId, accountId, e);
            throw new DAOException("Error removing product from cart", e);
        }
        return false;
    }

    /**
     * Removes a single product from an account's cart using its domain model representation.
     *
     * @param conn Active database connection
     * @param cart The model containing the target cart item's identifiers
     * @return true if the relationship was deleted; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the cart is null or has missing composite keys
     */
    public boolean delete(Connection conn, CartBean cart) throws DAOException {
        if (cart == null || cart.getAccountId() == null || cart.getProductId() == null) {
            throw new IllegalArgumentException("Attempted to delete a null cart item or " +
                    "an object with missing composite keys");
        }
        return delete(conn, cart.getAccountId(), cart.getProductId());
    }

    /**
     * Completely empties the cart for a given account.
     *
     * @param conn      Active database connection
     * @param accountId Unique identifier of the account to clear
     * @return true if one or more items were removed; false if the cart was already empty
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the accountId is null or empty
     */
    public boolean deleteAllByAccountId(Connection conn, String accountId) throws DAOException {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty for clearing the cart");
        }

        String sql = "DELETE FROM cart WHERE account_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            int rowsAffected = ps.executeUpdate();
            logger.info("Cart for Account ID: {} has been completely emptied. Items removed: {}", accountId,
                    rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Failed to empty cart for Account ID: {}", accountId, e);
            throw new DAOException("Error emptying the cart", e);
        }
    }

    /**
     * Maps a database row from a {@link ResultSet} into a {@link CartBean}.
     */
    private CartBean mapRow(ResultSet rs) throws SQLException {
        CartBean cart = new CartBean();
        cart.setAccountId(rs.getString("account_id"));
        cart.setProductId(rs.getString("product_id"));
        return cart;
    }
}
