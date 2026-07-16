package com.xyra.schemecraft.dao;

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
import com.xyra.schemecraft.model.AccountProductBean;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

/**
 * Data Access Object (DAO) for managing persistent {@link AccountProductBean} entities.
 */
public class AccountProductDAO extends BaseDAO {

    private static final String SELECT_BASE = "SELECT account_id, product_id, unlocked_at FROM account_product ";

    /**
     * Inserts a new AccountProduct relationship into the database.
     *
     * @param conn        Active database connection
     * @param association The AccountProduct relationship to persist
     * @throws DAOException             if a database error occurs
     * @throws DuplicateEntityException if the association already exists
     * @throws IllegalArgumentException if the association is null or contains invalid keys
     */
    public void insert(Connection conn, AccountProductBean association) throws DAOException {
        if (association == null) {
            throw new IllegalArgumentException("Cannot insert a null AccountProduct association");
        }
        if (association.getAccountId() == null || association.getAccountId().trim().isEmpty() ||
                association.getProductId() == null || association.getProductId().trim().isEmpty()) {
            throw new IllegalArgumentException("Association keys (AccountId and ProductId) must be valid " +
                    "and populated");
        }

        String sql = "INSERT INTO account_product (account_id, product_id) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, association.getAccountId());
            ps.setString(2, association.getProductId());

            ps.executeUpdate();
            logger.info("Product ID: {} successfully unlocked for Account ID: {}",
                    association.getProductId(), association.getAccountId());
        } catch (SQLException e) {
            logger.error("Failed to insert association for Account ID: {} and Product ID: {}",
                    association.getAccountId(), association.getProductId(), e);
            if (SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION.equals(e.getSQLState()) ||
                    e.getErrorCode() == MYSQL_ERR_DUPLICATE_KEY) {
                throw new DuplicateEntityException("Product already unlocked for this account", e);
            }
            throw new DAOException("Error occurred while unlocking product for account", e);
        }
    }

    /**
     * Finds an AccountProduct relationship by its composite key.
     *
     * @param conn      Active database connection
     * @param accountId Unique identifier of the target account
     * @param productId Unique identifier of the target product
     * @return An Optional containing the populated bean, or empty if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the accountId or productId is null or empty
     */
    public Optional<AccountProductBean> findById(Connection conn, String accountId, String productId)
            throws DAOException {
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
            logger.error("Database error while fetching association for Account ID: {} and Product ID: {}",
                    accountId, productId, e);
            throw new DAOException("Error fetching account-product association", e);
        }
        return Optional.empty();
    }

    /**
     * Retrieves all products unlocked by a specific account, ordered by unlock date descending.
     *
     * @param conn      Active database connection
     * @param accountId Unique identifier of the target account
     * @return List of product relationships owned by this account
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the accountId is null or empty
     */
    public List<AccountProductBean> findAllByAccountId(Connection conn, String accountId) throws DAOException {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty for retrieval query");
        }

        String sql = SELECT_BASE + "WHERE account_id = ? ORDER BY unlocked_at DESC";
        List<AccountProductBean> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving unlocked products for Account ID: {}", accountId, e);
            throw new DAOException("Error retrieving unlocked products by account ID", e);
        }
        return list;
    }

    /**
     * Retrieves all accounts that have unlocked a specific product, ordered by unlock date descending.
     *
     * @param conn      Active database connection
     * @param productId Unique identifier of the target product
     * @return List of account relationships that own this product
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the productId is null or empty
     */
    public List<AccountProductBean> findAllByProductId(Connection conn, String productId) throws DAOException {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty for retrieval query");
        }

        String sql = SELECT_BASE + "WHERE product_id = ? ORDER BY unlocked_at DESC";
        List<AccountProductBean> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving accounts for Product ID: {}", productId, e);
            throw new DAOException("Error retrieving accounts by product ID", e);
        }
        return list;
    }

    /**
     * Deletes a product unlock association from the database.
     *
     * @param conn      Active database connection
     * @param accountId Unique identifier of the target account
     * @param productId Unique identifier of the target product
     * @return true if the record was successfully deleted; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the accountId or productId is null or empty
     */
    public boolean delete(Connection conn, String accountId, String productId) throws DAOException {
        if (accountId == null || accountId.trim().isEmpty() || productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID and Product ID cannot be null or empty for revocation");
        }

        String sql = "DELETE FROM account_product WHERE account_id = ? AND product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            ps.setString(2, productId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Successfully revoked Product ID: {} from Account ID: {}", productId, accountId);
                return true;
            } else {
                logger.warn("Revocation issued for non-existent association between Account ID: {} and Product ID: {}",
                        accountId, productId);
            }
        } catch (SQLException e) {
            logger.error("Failed to delete association for Account ID: {} and Product ID: {}", accountId, productId, e);
            throw new DAOException("Error deleting account-product association", e);
        }
        return false;
    }

    /**
     * Deletes an association using its domain model representation.
     *
     * @param conn        Active database connection
     * @param association The model containing the target association's identifiers
     * @return true if the record was successfully deleted; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the association is null or does not have valid IDs
     */
    public boolean delete(Connection conn, AccountProductBean association) throws DAOException {
        if (association == null || association.getAccountId() == null || association.getProductId() == null) {
            throw new IllegalArgumentException("Attempted to delete a null association or an object with " +
                    "missing composite keys");
        }
        return delete(conn, association.getAccountId(), association.getProductId());
    }

    /**
     * Maps a database row from a {@link ResultSet} into an {@link AccountProductBean}.
     */
    private AccountProductBean mapRow(ResultSet rs) throws SQLException {
        AccountProductBean ap = new AccountProductBean();
        ap.setAccountId(rs.getString("account_id"));
        ap.setProductId(rs.getString("product_id"));

        Timestamp unlockedAt = rs.getTimestamp("unlocked_at");
        if (unlockedAt != null) {
            ap.setUnlockedAt(unlockedAt.toLocalDateTime());
        }
        return ap;
    }
}
