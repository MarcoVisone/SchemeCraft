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
import com.xyra.schemecraft.model.AccountBean;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

public class AccountDAO extends BaseDAO {
    private static final String SELECT_BASE = "SELECT account_id, username, email, country_id, currency_id, " +
            "language_id, banner_path, bio, created_at,is_active, is_admin, password_hash, " +
            "profile_image_path FROM account";

    public void insert(Connection conn, AccountBean account) throws DAOException {
        if (account == null) {
            throw new IllegalArgumentException("Cannot insert a null Account");
        }

        String sql = "INSERT INTO account (account_id, username, email, country_id, currency_id, language_id, " +
                "banner_path, bio, is_active, is_admin, password_hash, profile_image_path) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, account.getAccountId());
            ps.setString(2, account.getUsername());
            ps.setString(3, account.getEmail());
            ps.setString(4, account.getCountryId());
            ps.setString(5, account.getCurrencyId());
            ps.setString(6, account.getLanguageId());
            ps.setString(7, account.getBannerPath());
            ps.setString(8, account.getBio());
            ps.setBoolean(9, account.isActive());
            ps.setBoolean(10, account.isAdmin());
            ps.setString(11, account.getPasswordHash());
            ps.setString(12, account.getProfileImagePath());

            ps.executeUpdate();
            logger.info("Account successfully inserted with ID: {} and Username: {}", account.getAccountId(),
                    account.getUsername());
        } catch (SQLException e) {
            logger.error("Failed to insert account with Username: {}", account.getUsername(), e);
            if (MYSQL_DUPLICATE_KEY_STATE.equals(e.getSQLState()) || e.getErrorCode() == MYSQL_DUPLICATE_KEY_CODE) {
                throw new DuplicateEntityException("Username or Email already exists: " + account.getUsername(), e);
            }
            throw new DAOException("Error occurred while inserting account", e);
        }
    }

    public Optional<AccountBean> findById(Connection conn, String accountId) throws DAOException {
        String sql = SELECT_BASE + " WHERE account_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching account with ID: {}", accountId, e);
            throw new DAOException("Error fetching account by ID", e);
        }
        return Optional.empty();
    }

    public Optional<AccountBean> findByUsername(Connection conn, String username) throws DAOException {
        String sql = SELECT_BASE + " WHERE username = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while searching for account with Username: {}", username, e);
            throw new DAOException("Error fetching account by username", e);
        }
        return Optional.empty();
    }

    public Optional<AccountBean> findByEmail(Connection conn, String email) throws DAOException {
        String sql = SELECT_BASE + " WHERE email = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while searching for account with Email: {}", email, e);
            throw new DAOException("Error fetching account by email", e);
        }
        return Optional.empty();
    }

    public List<AccountBean> findAll(Connection conn) throws DAOException {
        List<AccountBean> accounts = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                accounts.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving all accounts", e);
            throw new DAOException("Error retrieving all accounts", e);
        }
        return accounts;
    }

    public List<AccountBean> findAllActive(Connection conn) throws DAOException {
        String sql = SELECT_BASE + " WHERE is_active = TRUE";
        List<AccountBean> accounts = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                accounts.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving all active accounts", e);
            throw new DAOException("Error retrieving all active accounts", e);
        }
        return accounts;
    }

    public List<AccountBean> findAllAdmin(Connection conn) throws DAOException {
        String sql = SELECT_BASE + " WHERE is_admin = TRUE";
        List<AccountBean> accounts = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                accounts.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving all admin accounts", e);
            throw new DAOException("Error retrieving all admin accounts", e);
        }
        return accounts;
    }

    public boolean update(Connection conn, String accountId, AccountBean account) throws DAOException {
        if (account == null) {
            throw new IllegalArgumentException("Cannot update with a null Account object");
        }

        String sql = "UPDATE account SET username = ?, email = ?, country_id = ?, currency_id = ?, language_id = ?, " +
                "banner_path = ?, bio = ?, is_active = ?, is_admin = ?, password_hash = ?, profile_image_path = ? " +
                "WHERE account_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, account.getUsername());
            ps.setString(2, account.getEmail());
            ps.setString(3, account.getCountryId());
            ps.setString(4, account.getCurrencyId());
            ps.setString(5, account.getLanguageId());
            ps.setString(6, account.getBannerPath());
            ps.setString(7, account.getBio());
            ps.setBoolean(8, account.isActive());
            ps.setBoolean(9, account.isAdmin());
            ps.setString(10, account.getPasswordHash());
            ps.setString(11, account.getProfileImagePath());
            ps.setString(12, account.getAccountId());
            ps.setString(13, account.getAccountId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Account with ID: {} successfully updated", accountId);
                return true;
            } else {
                logger.warn("Update issued for non-existent account ID: {}", accountId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update account with ID: {}", accountId, e);
            if (MYSQL_DUPLICATE_KEY_STATE.equals(e.getSQLState()) || e.getErrorCode() == MYSQL_DUPLICATE_KEY_CODE) {
                throw new DuplicateEntityException("Username or Email already exists for update: " +
                        account.getUsername(), e);
            }
            throw new DAOException("Error updating account", e);
        }
        return false;
    }

    public boolean update(Connection conn, AccountBean account) throws DAOException {
        if (account == null || account.getAccountId() == null) {
            throw new IllegalArgumentException("Attempted to update a null account or an account without an ID");
        }
        return update(conn, account.getAccountId(), account);
    }

    public boolean updatePassword(Connection conn, String accountId, String newPasswordHash) throws DAOException {
        if (accountId == null || newPasswordHash == null) {
            throw new IllegalArgumentException("Account ID and password hash cannot be null");
        }
        String sql = "UPDATE account SET password_hash = ? WHERE account_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setString(2, accountId);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Failed to update password for account ID: {}", accountId, e);
            throw new DAOException("Error updating account password", e);
        }
    }

    public boolean activate(Connection conn, String accountId) throws DAOException {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty for activation");
        }

        String sql = "UPDATE account SET is_active = TRUE WHERE account_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Account with ID: {} successfully reactivated", accountId);
                return true;
            } else {
                logger.warn("Activation issued for non-existent account ID: {}", accountId);
            }
        } catch (SQLException e) {
            logger.error("Failed to activate account with ID: {}", accountId, e);
            throw new DAOException("Error activating account", e);
        }
        return false;
    }

    public boolean activate(Connection conn, AccountBean account) throws DAOException, IllegalArgumentException {
        if (account == null || account.getAccountId() == null) {
            throw new IllegalArgumentException("Attempted to activate a null account or an account without an ID");
        }
        return activate(conn, account.getAccountId());
    }

    public boolean deactivate(Connection conn, String accountId) throws DAOException {
        String sql = "UPDATE account SET is_active = FALSE WHERE account_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Account with ID: {} successfully deactivated", accountId);
                return true;
            } else {
                logger.warn("Deactivation issued for non-existent account ID: {}", accountId);
            }
        } catch (SQLException e) {
            logger.error("Failed to deactivate account with ID: {}", accountId, e);
            throw new DAOException("Error deactivating account", e);
        }
        return false;
    }

    public boolean deactivate(Connection conn, AccountBean account) throws DAOException {
        if (account == null || account.getAccountId() == null) {
            throw new IllegalArgumentException("Attempted to deactivate a null account or an account without an ID");
        }
        return deactivate(conn, account.getAccountId());
    }

    public boolean forceDelete(Connection conn, String accountId) throws DAOException {
        String sql = "DELETE FROM account WHERE account_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Account with ID: {} successfully force deleted from database", accountId);
                return true;
            } else {
                logger.warn("Force delete issued for non-existent account ID: {}", accountId);
            }
        } catch (SQLException e) {
            logger.error("Failed to force delete account with ID: {}", accountId, e);
            throw new DAOException("Error force deleting account", e);
        }
        return false;
    }

    public boolean forceDelete(Connection conn, AccountBean account) throws DAOException {
        if (account == null || account.getAccountId() == null) {
            throw new IllegalArgumentException("Attempted to force delete a null account or an account without an ID");
        }
        return forceDelete(conn, account.getAccountId());
    }

    private AccountBean mapRow(ResultSet rs) throws SQLException {
        AccountBean account = new AccountBean();
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

        Timestamp timestamp = rs.getTimestamp("created_at");
        if (timestamp != null) {
            account.setCreatedAt(timestamp.toLocalDateTime());
        }
        return account;
    }
}
