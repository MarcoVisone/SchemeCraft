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
import com.xyra.schemecraft.model.FavoriteBean;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

/**
 * Data Access Object (DAO) for managing persistent {@link FavoriteBean} entities.
 */
public class FavoriteDAO extends BaseDAO {

    // Added trailing space to prevent query syntax issues during dynamic query appending
    private static final String SELECT_BASE = "SELECT account_id, product_id FROM favorite ";

    /**
     * Inserts a product into an account's favorites list.
     *
     * @param conn     Active database connection
     * @param favorite The Favorite bean containing composite relation keys
     * @throws DuplicateEntityException if the product is already bookmarked by the user
     * @throws DAOException             if a generic database error occurs
     * @throws IllegalArgumentException if the favorite association is null,
     * or if Account ID or Product ID are null or empty
     */
    public void insert(Connection conn, FavoriteBean favorite) throws DAOException {
        if (favorite == null) {
            throw new IllegalArgumentException("Cannot insert a null Favorite association");
        }
        if (favorite.getAccountId() == null || favorite.getAccountId().trim().isEmpty() ||
                favorite.getProductId() == null || favorite.getProductId().trim().isEmpty()) {
            throw new IllegalArgumentException("Favorite association keys must be valid and populated");
        }

        String sql = "INSERT INTO favorite (account_id, product_id) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, favorite.getAccountId());
            ps.setString(2, favorite.getProductId());

            ps.executeUpdate();
            logger.info("Product ID: {} successfully added to favorites for Account ID: {}",
                    favorite.getProductId(), favorite.getAccountId());
        } catch (SQLException e) {
            logger.error("Failed to add Product ID: {} to favorites for Account ID: {}", favorite.getProductId(),
                    favorite.getAccountId(), e);
            if (SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION.equals(e.getSQLState()) ||
                    e.getErrorCode() == MYSQL_ERR_DUPLICATE_KEY) {
                throw new DuplicateEntityException("Product is already in the favorites list for this account", e);
            }
            throw new DAOException("Error occurred while adding product to favorites", e);
        }
    }

    /**
     * Checks if a specific product is currently bookmarked by a given account.
     *
     * @param conn      Active database connection
     * @param accountId Unique identifier of the account
     * @param productId Unique identifier of the product
     * @return An Optional containing the favorite record if found, or empty otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Account ID or Product ID are null or empty
     */
    public Optional<FavoriteBean> findById(Connection conn, String accountId, String productId) throws DAOException {
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
            logger.error("Database error while checking favorite for Account ID: {} and Product ID: {}",
                    accountId, productId, e);
            throw new DAOException("Error fetching favorite association", e);
        }
        return Optional.empty();
    }

    /**
     * Retrieves all bookmarked products for a given account.
     *
     * @param conn      Active database connection
     * @param accountId Unique identifier of the target account
     * @return List of all favorite items linked to the account
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Account ID is null or empty
     */
    public List<FavoriteBean> findAllByAccountId(Connection conn, String accountId) throws DAOException {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty for retrieval query");
        }

        String sql = SELECT_BASE + "WHERE account_id = ?";
        List<FavoriteBean> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving favorites for Account ID: {}", accountId, e);
            throw new DAOException("Error retrieving favorites by Account ID", e);
        }
        return list;
    }

    /**
     * Removes a product from an account's favorites list using composite keys.
     *
     * @param conn      Active database connection
     * @param accountId Unique identifier of the account
     * @param productId Unique identifier of the product to remove
     * @return true if the favorite was found and removed; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Account ID or Product ID are null or empty
     */
    public boolean delete(Connection conn, String accountId, String productId) throws DAOException {
        if (accountId == null || accountId.trim().isEmpty() || productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID and Product ID cannot be null or empty for deletion");
        }

        String sql = "DELETE FROM favorite WHERE account_id = ? AND product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            ps.setString(2, productId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Product ID: {} successfully removed from Favorites of Account ID: {}",
                        productId, accountId);
                return true;
            } else {
                logger.warn("No active favorite found to delete for Account ID: {} and Product ID: {}",
                        accountId, productId);
            }
        } catch (SQLException e) {
            logger.error("Failed to remove Product ID: {} from favorites for Account ID: {}", productId, accountId, e);
            throw new DAOException("Error removing product from favorites", e);
        }
        return false;
    }

    /**
     * Removes a product from a favorites list using its domain model representation.
     *
     * @param conn     Active database connection
     * @param favorite The favorite relationship model containing composite keys to remove
     * @return true if the relationship was deleted; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the favorite object is null or has missing composite keys
     */
    public boolean delete(Connection conn, FavoriteBean favorite) throws DAOException {
        if (favorite == null || favorite.getAccountId() == null || favorite.getProductId() == null) {
            throw new IllegalArgumentException("Attempted to delete a null favorite " +
                    "or an object with missing composite keys");
        }
        return delete(conn, favorite.getAccountId(), favorite.getProductId());
    }

    /**
     * Maps a database row from a {@link ResultSet} into a {@link FavoriteBean}.
     */
    private FavoriteBean mapRow(ResultSet rs) throws SQLException {
        FavoriteBean favorite = new FavoriteBean();
        favorite.setAccountId(rs.getString("account_id"));
        favorite.setProductId(rs.getString("product_id"));
        return favorite;
    }
}
