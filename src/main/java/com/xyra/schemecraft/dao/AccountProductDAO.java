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

public class AccountProductDAO extends BaseDAO {
    private static final String SELECT_BASE = "SELECT account_id, product_id, unlocked_at FROM account_product";

    public void insert(Connection conn, AccountProductBean association) throws DAOException {
        if (association == null) {
            throw new IllegalArgumentException("Cannot insert a null AccountProduct association");
        }

        String sql = "INSERT INTO account_product (account_id, product_id) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, association.getAccountId());
            ps.setString(2, association.getProductId());

            ps.executeUpdate();
            logger.info("Product ID: {} successfully unlocked for Account ID: {}",
                    association.getProductId(), association.getAccountId());
        } catch (SQLException e) {
            logger.error("Failed to link account {} with product {}", association.getAccountId(),
                    association.getProductId(), e);
            if (MYSQL_DUPLICATE_KEY_STATE.equals(e.getSQLState()) || e.getErrorCode() == MYSQL_DUPLICATE_KEY_CODE) {
                throw new DuplicateEntityException("Product already unlocked for this account", e);
            }
            throw new DAOException("Error occurred while unlocking product for account", e);
        }
    }

    public Optional<AccountProductBean> findById(Connection conn, String accountId, String productId) throws DAOException {
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
            logger.error("Database error while fetching association for account {} and product {}",
                    accountId, productId, e);
            throw new DAOException("Error fetching account-product association", e);
        }
        return Optional.empty();
    }

    public List<AccountProductBean> findAllByAccountId(Connection conn, String accountId) throws DAOException {
        String sql = SELECT_BASE + " WHERE account_id = ? ORDER BY unlocked_at DESC";
        List<AccountProductBean> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving unlocked products for account ID: {}", accountId, e);
            throw new DAOException("Error retrieving unlocked products by account ID", e);
        }
        return list;
    }

    public List<AccountProductBean> findAllByProductId(Connection conn, String productId) throws DAOException {
        String sql = SELECT_BASE + " WHERE product_id = ? ORDER BY unlocked_at DESC";
        List<AccountProductBean> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving accounts for product ID: {}", productId, e);
            throw new DAOException("Error retrieving accounts by product ID", e);
        }
        return list;
    }

    public boolean delete(Connection conn, String accountId, String productId) throws DAOException {
        String sql = "DELETE FROM account_product WHERE account_id = ? AND product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            ps.setString(2, productId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Failed to delete association for account {} and product {}", accountId, productId, e);
            throw new DAOException("Error deleting account-product association", e);
        }
    }

    public boolean delete(Connection conn, AccountProductBean association) throws DAOException {
        if (association == null || association.getAccountId() == null || association.getProductId() == null) {
            throw new IllegalArgumentException("Attempted to delete a null association or an object with " +
                    "missing composite keys");
        }
        return delete(conn, association.getAccountId(), association.getProductId());
    }

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
