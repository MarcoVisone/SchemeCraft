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
import com.xyra.schemecraft.model.Cart;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

public class CartDAO extends BaseDAO {
    private static final String SELECT_BASE = "SELECT account_id, product_id FROM cart";

    public void insert(Connection conn, Cart cart) throws DAOException {
        if (cart == null) {
            throw new IllegalArgumentException("Cannot insert a null Cart association");
        }

        String sql = "INSERT INTO cart (account_id, product_id) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cart.getAccountId());
            ps.setString(2, cart.getProductId());

            ps.executeUpdate();
            logger.info("Product ID: {} successfully added to cart for Account ID: {}",
                    cart.getProductId(), cart.getAccountId());
        } catch (SQLException e) {
            logger.error("Failed to add product {} to cart for account {}", cart.getProductId(),
                    cart.getAccountId(), e);
            if (MYSQL_DUPLICATE_KEY_STATE.equals(e.getSQLState()) || e.getErrorCode() == MYSQL_DUPLICATE_KEY_CODE) {
                throw new DuplicateEntityException("Product is already in the cart for this account", e);
            }
            throw new DAOException("Error occurred while adding product to cart", e);
        }
    }

    public Optional<Cart> findById(Connection conn, String accountId, String productId) throws DAOException {
        String sql = SELECT_BASE + " WHERE account_id = ? AND product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            ps.setString(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while checking cart item for account {} and product {}",
                    accountId, productId, e);
            throw new DAOException("Error fetching cart association", e);
        }
        return Optional.empty();
    }

    public List<Cart> findAllByAccountId(Connection conn, String accountId) throws DAOException {
        String sql = SELECT_BASE + " WHERE account_id = ?";
        List<Cart> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving cart for account ID: {}", accountId, e);
            throw new DAOException("Error retrieving cart items by account ID", e);
        }
        return list;
    }

    public boolean delete(Connection conn, String accountId, String productId) throws DAOException {
        String sql = "DELETE FROM cart WHERE account_id = ? AND product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            ps.setString(2, productId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Failed to remove product {} from cart for account {}", productId, accountId, e);
            throw new DAOException("Error removing product from cart", e);
        }
    }

    public boolean delete(Connection conn, Cart cart) throws DAOException {
        if (cart == null || cart.getAccountId() == null || cart.getProductId() == null) {
            throw new IllegalArgumentException("Attempted to delete a null cart item or " +
                    "an object with missing composite keys");
        }
        return delete(conn, cart.getAccountId(), cart.getProductId());
    }

    public boolean deleteAllByAccountId(Connection conn, String accountId) throws DAOException {
        String sql = "DELETE FROM cart WHERE account_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            int rowsAffected = ps.executeUpdate();
            logger.info("Cart for account ID: {} has been completely emptied. Items removed: {}", accountId,
                    rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Failed to empty cart for account ID: {}", accountId, e);
            throw new DAOException("Error emptying the cart", e);
        }
    }

    private Cart mapRow(ResultSet rs) throws SQLException {
        Cart cart = new Cart();
        cart.setAccountId(rs.getString("account_id"));
        cart.setProductId(rs.getString("product_id"));
        return cart;
    }
}
