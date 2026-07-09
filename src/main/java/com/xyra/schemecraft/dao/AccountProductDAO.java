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

    public boolean checkOwnership(String accountId, String productId) {
        String sql = "SELECT 1 FROM account_product WHERE account_id = ? AND product_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountId);
            ps.setString(2, productId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Error checking product ownership for Account: {} and Product: {}", accountId, productId, e);
            return false;
        }
    }

    public List<AccountProduct> findByAccountId(String accountId) {
        String sql = "SELECT * FROM account_product WHERE account_id = ?";
        List<AccountProduct> list = new ArrayList<>();

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAccountProduct(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error occurred while fetching unlocked products for account ID: {}", accountId, e);
        }
        return list;
    }

    private AccountProduct mapResultSetToAccountProduct(ResultSet rs) throws SQLException {
        AccountProduct ap = new AccountProduct();
        ap.setAccountId(rs.getString("account_id"));
        ap.setProductId(rs.getString("product_id"));

        Timestamp timestamp = rs.getTimestamp("unlocked_at");
        if (timestamp != null) {
            ap.setUnlockedAt(timestamp.toLocalDateTime());
        }
        return ap;
    }
}
