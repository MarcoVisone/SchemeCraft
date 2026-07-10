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
import com.xyra.schemecraft.model.Favorite;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

public class FavoriteDAO extends BaseDAO {
    private static final String SELECT_BASE = "SELECT account_id, product_id FROM favorite";

    public void insert(Connection conn, Favorite favorite) throws DAOException {
        if (favorite == null) {
            throw new IllegalArgumentException("Cannot insert a null Favorite association");
        }

        String sql = "INSERT INTO favorite (account_id, product_id) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, favorite.getAccountId());
            ps.setString(2, favorite.getProductId());

            ps.executeUpdate();
            logger.info("Product ID: {} successfully added to favorites for Account ID: {}",
                    favorite.getProductId(), favorite.getAccountId());
        } catch (SQLException e) {
            logger.error("Failed to add product {} to favorites for account {}", favorite.getProductId(),
                    favorite.getAccountId(), e);
            if (MYSQL_DUPLICATE_KEY_STATE.equals(e.getSQLState()) || e.getErrorCode() == MYSQL_DUPLICATE_KEY_CODE) {
                throw new DuplicateEntityException("Product is already in the favorites list for this account", e);
            }
            throw new DAOException("Error occurred while adding product to favorites", e);
        }
    }

    public Optional<Favorite> findById(Connection conn, String accountId, String productId) throws DAOException {
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
            logger.error("Database error while checking favorite for account {} and product {}",
                    accountId, productId, e);
            throw new DAOException("Error fetching favorite association", e);
        }
        return Optional.empty();
    }

    public List<Favorite> findAllByAccountId(Connection conn, String accountId) throws DAOException {
        String sql = SELECT_BASE + " WHERE account_id = ?";
        List<Favorite> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving favorites for account ID: {}", accountId, e);
            throw new DAOException("Error retrieving favorites by account ID", e);
        }
        return list;
    }

    public boolean delete(Connection conn, String accountId, String productId) throws DAOException {
        String sql = "DELETE FROM favorite WHERE account_id = ? AND product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            ps.setString(2, productId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Failed to remove product {} from favorites for account {}", productId, accountId, e);
            throw new DAOException("Error removing product from favorites", e);
        }
    }

    public boolean delete(Connection conn, Favorite favorite) throws DAOException {
        if (favorite == null || favorite.getAccountId() == null || favorite.getProductId() == null) {
            throw new IllegalArgumentException("Attempted to delete a null favorite " +
                    "or an object with missing composite keys");
        }
        return delete(conn, favorite.getAccountId(), favorite.getProductId());
    }

    private Favorite mapRow(ResultSet rs) throws SQLException {
        Favorite favorite = new Favorite();
        favorite.setAccountId(rs.getString("account_id"));
        favorite.setProductId(rs.getString("product_id"));
        return favorite;
    }
}
