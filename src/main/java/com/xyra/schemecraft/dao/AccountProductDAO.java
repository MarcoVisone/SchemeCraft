package com.xyra.schemecraft.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyra.schemecraft.connection.ConnectionPool;
import com.xyra.schemecraft.model.AccountProduct;


public class AccountProductDAO {
    private static final Logger logger = LoggerFactory.getLogger(AccountProductDAO.class);

    public void save(AccountProduct accountProduct) {
        String sql = "INSERT INTO account_product (account_id, product_id) VALUES (?, ?)";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountProduct.getAccountId());
            ps.setString(2, accountProduct.getProductId());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while unlocking product {} for account {}",
                    accountProduct.getProductId(), accountProduct.getAccountId(), e);
        }
    }

    public AccountProduct findById(String accountId, String productId) {
        String sql = "SELECT * FROM account_product WHERE account_id = ? AND product_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountId);
            ps.setString(2, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAccountProduct(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error occurred while searching account_product relation for Account: {} and Product: {}",
                    accountId, productId, e);
        }
        return null;
    }

    public List<AccountProduct> findByAccountId(String accountId) {
        String sql = "SELECT * FROM account_product WHERE account_id = ?";
        List<AccountProduct> accountProducts = new ArrayList<>();

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accountProducts.add(mapResultSetToAccountProduct(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error occurred while searching for products unlocked by Account: {}", accountId, e);
        }
        return accountProducts;
    }

    public List<AccountProduct> findByProductId(String productId) {
        String sql = "SELECT * FROM account_product WHERE product_id = ?";
        List<AccountProduct> accountProducts = new ArrayList<>();

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accountProducts.add(mapResultSetToAccountProduct(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error occurred while searching for accounts that unlocked Product: {}", productId, e);
        }
        return accountProducts;
    }

    public List<AccountProduct> findAll() {
        String sql = "SELECT * FROM account_product";
        List<AccountProduct> accountProducts = new ArrayList<>();

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                accountProducts.add(mapResultSetToAccountProduct(rs));
            }
        } catch (SQLException e) {
            logger.error("Error occurred while fetching all account_product relations", e);
        }
        return accountProducts;
    }

    public void update(AccountProduct accountProduct) {
        String sql = "UPDATE account_product SET unlocked_at = ? WHERE account_id = ? AND product_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, accountProduct.getUnlockedAt() != null ?
                    Timestamp.valueOf(accountProduct.getUnlockedAt()) : null);
            ps.setString(2, accountProduct.getAccountId());
            ps.setString(3, accountProduct.getProductId());

            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while updating account_product relation for Account: {} and Product: {}",
                    accountProduct.getAccountId(), accountProduct.getProductId(), e);
        }
    }

    public void deleteById(String accountId, String productId) {
        String sql = "DELETE FROM account_product WHERE account_id = ? AND product_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountId);
            ps.setString(2, productId);

            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while deleting account_product relation for Account: {} and Product: {}",
                    accountId, productId, e);
        }
    }

    private AccountProduct mapResultSetToAccountProduct(ResultSet rs) throws SQLException {
        String accountId = rs.getString("account_id");
        String productId = rs.getString("product_id");
        Timestamp unlockedAtTimestamp = rs.getTimestamp("unlocked_at");
        return new AccountProduct(accountId, productId, unlockedAtTimestamp != null ?
                unlockedAtTimestamp.toLocalDateTime() : null);
    }
}
