package com.xyra.schemecraft.dao;

import java.math.BigDecimal;
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
import com.xyra.schemecraft.model.Account;

public class AccountDAO {
    private static final Logger logger = LoggerFactory.getLogger(AccountDAO.class);

    public boolean insert(Account account) {
        String sql = "INSERT INTO account (account_id, username, email, password_hash, is_admin, " +
                "country_id, currency_id, language_id, bio, profile_image_path, banner_path, is_active, balance) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, account.getAccountId());
            ps.setString(2, account.getUsername());
            ps.setString(3, account.getEmail());
            ps.setString(4, account.getPasswordHash());
            ps.setBoolean(5, account.isAdmin());
            ps.setString(6, account.getCountryId());
            ps.setString(7, account.getCurrencyId());
            ps.setString(8, account.getLanguageId());
            ps.setString(9, account.getBio());
            ps.setString(10, account.getProfileImagePath());
            ps.setString(11, account.getBannerPath());
            ps.setBoolean(12, account.isActive());
            ps.setBigDecimal(13, account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Account successfully inserted with ID: {} and Username: {}", account.getAccountId(), account.getUsername());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Failed to insert account with Username: {}", account.getUsername(), e);
        }
        return false;
    }

    public Account findById(String accountId) {
        String sql = "SELECT * FROM account WHERE account_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            ps.setString(1, accountId);
            if (rs.next()) {
                return mapResultSetToAccount(rs);
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching account with ID: {}", accountId, e);
        }
        return null;
    }

    public Account findByUsername(String username) {
        String sql = "SELECT * FROM account WHERE username = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            ps.setString(1, username);
            if (rs.next()) {
                return mapResultSetToAccount(rs);
            }
        } catch (SQLException e) {
            logger.error("Database error while searching for account with Username: {}", username, e);
        }
        return null;
    }

    public Account findByEmail(String email) {
        String sql = "SELECT * FROM account WHERE email = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            ps.setString(1, email);
            if (rs.next()) {
                return mapResultSetToAccount(rs);
            }
        } catch (SQLException e) {
            logger.error("Database error while searching for account with Email: {}", email, e);
        }
        return null;
    }

    public List<Account> findAll() {
        String sql = "SELECT * FROM account";
        List<Account> accounts = new ArrayList<>();

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving all accounts", e);
        }
        return accounts;
    }

    public boolean update(String accountId, Account account) {
        String sql = "UPDATE account SET username = ?, email = ?, password_hash = ?, is_admin = ?, " +
                "country_id = ?, currency_id = ?, language_id = ?, bio = ?, profile_image_path = ?, " +
                "banner_path = ?, is_active = ?, balance = ? WHERE account_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, account.getUsername());
            ps.setString(2, account.getEmail());
            ps.setString(3, account.getPasswordHash());
            ps.setBoolean(4, account.isAdmin());
            ps.setString(5, account.getCountryId());
            ps.setString(6, account.getCurrencyId());
            ps.setString(7, account.getLanguageId());
            ps.setString(8, account.getBio());
            ps.setString(9, account.getProfileImagePath());
            ps.setString(10, account.getBannerPath());
            ps.setBoolean(11, account.isActive());
            ps.setBigDecimal(12, account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO);
            ps.setString(13, accountId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Account with ID: {} successfully updated", accountId);
                return true;
            } else {
                logger.warn("Update issued for non-existent account ID: {}", accountId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update account with ID: {}", accountId, e);
        }
        return false;
    }

    public boolean update(Account account) {
        if (account == null || account.getAccountId() == null) {
            logger.warn("Attempted to update a null account or a account without an ID");
            return false;
        }
        return update(account.getCurrencyId(), account);
    }

    public boolean deactivate(String accountId) {
        String sql = "UPDATE account SET is_active = FALSE WHERE account_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Account with ID: {} successfully deactivated (Soft Delete)", accountId);
                return true;
            } else {
                logger.warn("Soft delete issued for non-existent account ID: {}", accountId);
            }
        } catch (SQLException e) {
            logger.error("Failed to logically delete account with ID: {}", accountId, e);
        }
        return false;
    }

    public boolean deactivate(Account account) {
        if (account == null || account.getAccountId() == null) {
            logger.warn("Attempted to deactivate a null account or an account without an ID");
            return false;
        }
        return deactivate(account.getAccountId());
    }

    public boolean strongDelete(String accountId) {
        String sql = "DELETE FROM account WHERE account_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Account with ID: {} successfully deleted", accountId);
                return true;
            } else {
                logger.warn("Delete issued for non-existent account ID: {}", accountId);
            }
        } catch (SQLException e) {
            logger.error("Failed to delete account with ID: {}", accountId, e);
        }
        return false;
    }

    public boolean strongDelete(Account account) {
        if (account == null || account.getAccountId() == null) {
            logger.warn("Attempted to delete a null account or an account without an ID");
            return false;
        }
        return strongDelete(account.getAccountId());
    }

    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setAccountId(rs.getString("account_id"));
        account.setUsername(rs.getString("username"));
        account.setEmail(rs.getString("email"));
        account.setPasswordHash(rs.getString("password_hash"));
        account.setAdmin(rs.getBoolean("is_admin"));
        account.setCountryId(rs.getString("country_id"));
        account.setCurrencyId(rs.getString("currency_id"));
        account.setLanguageId(rs.getString("language_id"));
        account.setBio(rs.getString("bio"));
        account.setProfileImagePath(rs.getString("profile_image_path"));
        account.setBannerPath(rs.getString("banner_path"));
        account.setActive(rs.getBoolean("is_active"));

        BigDecimal balance = rs.getBigDecimal("balance");
        account.setBalance(balance != null ? balance : BigDecimal.ZERO);

        Timestamp timestamp = rs.getTimestamp("created_at");
        if (timestamp != null) {
            account.setCreatedAt(timestamp.toLocalDateTime());
        }
        return account;
    }
}
