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
import com.xyra.schemecraft.dto.ProfileUpdateRequest;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

/**
 * Data Access Object (DAO) for managing persistent {@link AccountBean} entities.
 */
public class AccountDAO extends BaseDAO {

    private static final String SELECT_BASE = "SELECT account_id, username, email, country_id, currency_id, " +
            "language_id, banner_path, bio, created_at, is_active, is_admin, password_hash, " +
            "profile_image_path FROM account ";

    /**
     * Inserts a new Account record into the database.
     *
     * @param conn    Active database connection
     * @param account The Account bean to persist
     * @throws DAOException             if a database error occurs
     * @throws DuplicateEntityException if the username or email already exists in the database
     * @throws IllegalArgumentException if the account is null
     */
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
            logger.info("Account successfully inserted with ID: {} and Username: {}",
                    account.getAccountId(), account.getUsername());
        } catch (SQLException e) {
            logger.error("Failed to insert account with Username: {}", account.getUsername(), e);
            if (SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION.equals(e.getSQLState()) ||
                    e.getErrorCode() == MYSQL_ERR_DUPLICATE_KEY) {
                throw new DuplicateEntityException("Username or Email already exists: " + account.getUsername(),
                        DuplicateEntityException.ConflictingField.UNKNOWN);
            }
            throw new DAOException("Error occurred while inserting account", e);
        }
    }

    /**
     * Finds an Account by its unique ID.
     *
     * @param conn      Active database connection
     * @param accountId Unique identifier of the target account
     * @return An Optional containing the populated bean, or empty if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the accountId is null or empty
     */
    public Optional<AccountBean> findById(Connection conn, String accountId) throws DAOException {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty");
        }

        String sql = SELECT_BASE + "WHERE account_id = ?";

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

    /**
     * Finds an Account by its unique username.
     *
     * @param conn     Active database connection
     * @param username Unique username of the target account
     * @return An Optional containing the populated bean, or empty if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the username is null or empty
     */
    public Optional<AccountBean> findByUsername(Connection conn, String username) throws DAOException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        String sql = SELECT_BASE + "WHERE username = ?";

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

    /**
     * Finds an Account by its unique registered email address.
     *
     * @param conn  Active database connection
     * @param email Unique email of the target account
     * @return An Optional containing the populated bean, or empty if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the email is null or empty
     */
    public Optional<AccountBean> findByEmail(Connection conn, String email) throws DAOException {
        if  (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        String sql = SELECT_BASE + "WHERE email = ?";

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

    /**
     * Retrieves all Account records.
     *
     * @param conn Active database connection
     * @return List of all accounts
     * @throws DAOException if a database error occurs
     */
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

    /**
     * Retrieves all active Account records.
     *
     * @param conn Active database connection
     * @return List of active accounts
     * @throws DAOException if a database error occurs
     */
    public List<AccountBean> findAllActive(Connection conn) throws DAOException {
        String sql = SELECT_BASE + "WHERE is_active = TRUE";
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

    /**
     * Retrieves all administrator Account records.
     *
     * @param conn Active database connection
     * @return List of administrator accounts
     * @throws DAOException if a database error occurs
     */
    public List<AccountBean> findAllAdmin(Connection conn) throws DAOException {
        String sql = SELECT_BASE + "WHERE is_admin = TRUE";
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

    /**
     * Updates an existing Account profile with new values using its unique ID.
     *
     * @param conn      Active database connection
     * @param accountId Unique identifier of the target account
     * @param account   The model containing the updated details
     * @return true if the row was successfully updated; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws DuplicateEntityException if the update violates a unique constraint
     * @throws IllegalArgumentException if the accountId is null or empty, or if the account is null
     */
    public boolean update(Connection conn, String accountId, AccountBean account) throws DAOException {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty for updates");
        }
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
            ps.setString(12, accountId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Account with ID: {} successfully updated", accountId);
                return true;
            } else {
                logger.warn("Update issued for non-existent account ID: {}", accountId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update account with ID: {}", accountId, e);
            if (SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION.equals(e.getSQLState()) ||
                    e.getErrorCode() == MYSQL_ERR_DUPLICATE_KEY) {
                throw new DuplicateEntityException("Username or Email already exists for update: " +
                        account.getUsername(), e);
            }
            throw new DAOException("Error updating account", e);
        }
        return false;
    }

    /**
     * Updates an existing Account profile with new values using its domain model representation.
     *
     * @param conn    Active database connection
     * @param account The model containing the updated details
     * @return true if the row was successfully updated; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the account is null or does not have a valid ID
     */
    public boolean update(Connection conn, AccountBean account) throws DAOException {
        if (account == null || account.getAccountId() == null) {
            throw new IllegalArgumentException("Attempted to update a null account or an account without an ID");
        }
        return update(conn, account.getAccountId(), account);
    }

    /**
     * Updates only the username parameter of an Account.
     *
     * @param conn        Active database connection
     * @param accountId   Unique identifier of the target account
     * @param newUsername The new username to be set for the account
     * @return true if the username update succeeded; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws DuplicateEntityException if the new username is already taken by another account
     * @throws IllegalArgumentException if the accountId or newUsername is null or empty
     */
    public boolean updateUsername(Connection conn, String accountId, String newUsername) throws DAOException {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty");
        }
        if (newUsername == null || newUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("New username cannot be null or empty");
        }

        String sql = "UPDATE account SET username = ? WHERE account_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newUsername);
            ps.setString(2, accountId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Username successfully updated for account ID: {}", accountId);
                return true;
            } else {
                logger.warn("Username update issued for non-existent account ID: {}", accountId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update username for account ID: {}", accountId, e);
            if (SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION.equals(e.getSQLState()) ||
                    e.getErrorCode() == MYSQL_ERR_DUPLICATE_KEY) {
                throw new DuplicateEntityException("Username already taken: " + newUsername,
                        DuplicateEntityException.ConflictingField.USERNAME);
            }
            throw new DAOException("Error updating account username", e);
        }
        return false;
    }

    /**
     * Updates only the username parameter of an Account using its domain model representation.
     *
     * @param conn    Active database connection
     * @param account The model containing the target account's identifier and new username
     * @return true if the username update succeeded; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the account is null or does not have a valid ID
     */
    public boolean updateUsername(Connection conn, AccountBean account) throws DAOException {
        if (account == null || account.getAccountId() == null || account.getUsername() == null) {
            throw new IllegalArgumentException("Account ID and username cannot be null");
        }
        return updateUsername(conn, account.getAccountId(), account.getUsername());
    }

    /**
     * Updates only the email parameter of an Account.
     *
     * @param conn      Active database connection
     * @param accountId Unique identifier of the target account
     * @param newEmail  The new email address to be set for the account
     * @return true if the email update succeeded; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the accountId or newEmail is null or empty
     */
    public boolean updateEmail(Connection conn, String accountId, String newEmail) throws DAOException {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty");
        }
        if (newEmail == null || newEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("New email cannot be null or empty");
        }

        String sql = "UPDATE account SET email = ? WHERE account_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newEmail);
            ps.setString(2, accountId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Email successfully updated for account ID: {}", accountId);
                return true;
            } else {
                logger.warn("Email update issued for non-existent account ID: {}", accountId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update email for account ID: {}", accountId, e);
            if (SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION.equals(e.getSQLState()) ||
                    e.getErrorCode() == MYSQL_ERR_DUPLICATE_KEY) {
                throw new DuplicateEntityException("Email already registered: " + newEmail,
                        DuplicateEntityException.ConflictingField.EMAIL);
            }
            throw new DAOException("Error updating account email", e);
        }
        return false;
    }

    /**
     * Updates only the email parameter of an Account using its domain model representation.
     *
     * @param conn    Active database connection
     * @param account The model containing the target account's identifier and new email
     * @return true if the email update succeeded; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the account is null or does not have a valid ID
     */
    public boolean updateEmail(Connection conn, AccountBean account) throws DAOException {
        if (account == null || account.getAccountId() == null || account.getEmail() == null) {
            throw new IllegalArgumentException("Account ID and/or email cannot be null");
        }
        return updateEmail(conn, account.getAccountId(), account.getEmail());
    }

    /**
     * Updates only the password hash parameter of an Account.
     *
     * @param conn            Active database connection
     * @param accountId       Unique identifier of the target account
     * @param newPasswordHash The new secure hash string of the password
     * @return true if the password update succeeded; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the accountId is null or empty, or if the newPasswordHash is null or empty
     */
    public boolean updatePassword(Connection conn, String accountId, String newPasswordHash) throws DAOException {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty");
        }
        if (newPasswordHash == null || newPasswordHash.trim().isEmpty()) {
            throw new IllegalArgumentException("New password hash cannot be null or empty");
        }

        String sql = "UPDATE account SET password_hash = ? WHERE account_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setString(2, accountId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Password successfully updated for account ID: {}", accountId);
                return true;
            } else {
                logger.warn("Password update issued for non-existent account ID: {}", accountId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update password for account ID: {}", accountId, e);
            throw new DAOException("Error updating account password", e);
        }
        return false;
    }

    /**
     * Updates only the password hash parameter of an Account using its domain model representation.
     *
     * @param conn    Active database connection
     * @param account The model containing the target account's identifier and new password hash
     * @return true if the password update succeeded; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the account is null or does not have a valid ID
     */
    public boolean updatePassword(Connection conn, AccountBean account) throws DAOException {
        if (account == null || account.getAccountId() == null || account.getPasswordHash() == null) {
            throw new IllegalArgumentException("Account and its ID and password hash cannot be null");
        }
        return updatePassword(conn, account.getAccountId(), account.getPasswordHash());
    }

    /**
     * Dynamically updates only the non-null fields provided in the {@link ProfileUpdateRequest}.
     *
     * @param conn    the active database connection
     * @param request payload containing the fields to update and the target account identifier
     * @return true if the record was successfully updated in the database; false otherwise
     * @throws DAOException             if a database access error occurs during execution
     * @throws IllegalArgumentException if the request is null
     */
    public boolean softUpdate(Connection conn, ProfileUpdateRequest request) throws DAOException {
        if (request == null) {
            throw new IllegalArgumentException("request cannot be null");
        }

        List<String> setClauses = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        if (request.countryId() != null) {
            setClauses.add("country_id = ?");
            parameters.add(request.countryId());
        }

        if (request.currencyId() != null) {
            setClauses.add("currency_id = ?");
            parameters.add(request.currencyId());
        }

        if (request.languageId() != null) {
            setClauses.add("language_id = ?");
            parameters.add(request.languageId());
        }

        if (request.bio() != null) {
            setClauses.add("bio = ?");
            parameters.add(request.bio());
        }

        if (request.profileImagePath() != null) {
            setClauses.add("profile_image_path = ?");
            parameters.add(request.profileImagePath());
        }

        if (setClauses.isEmpty()) {
            return false;
        }

        String sql = "UPDATE account SET " + String.join(", ", setClauses) + " WHERE account_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            for (Object param : parameters) {
                ps.setObject(paramIndex++, param);
            }

            ps.setString(paramIndex, request.accountId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new DAOException("Failed to perform soft update for accountId: " + request.accountId(), e);
        }
    }

    /**
     * Reactivates an Account using its unique ID.
     *
     * @param conn      Active database connection
     * @param accountId Unique identifier of the target account
     * @return true if the account was successfully activated; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the accountId is null or empty
     */
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

    /**
     * Reactivates an Account using its domain model representation.
     *
     * @param conn    Active database connection
     * @param account The model containing the target account's identifier
     * @return true if the account was successfully activated; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the account is null or does not have a valid ID
     */
    public boolean activate(Connection conn, AccountBean account) throws DAOException {
        if (account == null || account.getAccountId() == null) {
            throw new IllegalArgumentException("Attempted to activate a null account or an account without an ID");
        }
        return activate(conn, account.getAccountId());
    }

    /**
     * Deactivates an Account using its unique ID.
     *
     * @param conn      Active database connection
     * @param accountId Unique identifier of the target account
     * @return true if the account was successfully deactivated; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the accountId is null or empty
     */
    public boolean deactivate(Connection conn, String accountId) throws DAOException {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty for deactivation");
        }

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

    /**
     * Deactivates an Account using its domain model representation.
     *
     * @param conn    Active database connection
     * @param account The model containing the target account's identifier
     * @return true if the account was successfully deactivated; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the account is null or does not have a valid ID
     */
    public boolean deactivate(Connection conn, AccountBean account) throws DAOException {
        if (account == null || account.getAccountId() == null) {
            throw new IllegalArgumentException("Attempted to deactivate a null account or an account without an ID");
        }
        return deactivate(conn, account.getAccountId());
    }

    /**
     * Hard-deletes an Account record from the database using its unique ID.
     *
     * @param conn      Active database connection
     * @param accountId Unique identifier of the target account
     * @return true if the record was successfully deleted; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the accountId is null or empty
     */
    public boolean forceDelete(Connection conn, String accountId) throws DAOException {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty for deletion");
        }

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

    /**
     * Hard-deletes an Account record from the database using its domain model representation.
     *
     * @param conn    Active database connection
     * @param account The model containing the target account's identifier
     * @return true if the record was successfully deleted; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the account is null or does not have a valid ID
     */
    public boolean forceDelete(Connection conn, AccountBean account) throws DAOException {
        if (account == null || account.getAccountId() == null) {
            throw new IllegalArgumentException("Attempted to force delete a null account or an account without an ID");
        }
        return forceDelete(conn, account.getAccountId());
    }

    /**
     * Maps a database row from a {@link ResultSet} into an {@link AccountBean}.
     */
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
