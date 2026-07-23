package com.xyra.schemecraft.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.xyra.schemecraft.exception.DAOException;
import com.xyra.schemecraft.exception.DuplicateEntityException;
import com.xyra.schemecraft.model.PaymentMethodBean;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

/**
 * Data Access Object (DAO) for managing persistent {@link PaymentMethodBean} entities.
 */
public class PaymentMethodDAO extends BaseDAO {

    private static final String SELECT_BASE = "SELECT payment_method_id, account_id, method_type, card_brand, " +
            "card_expiration, card_last_four, flag_default, payment_email, payment_token FROM payment_method ";

    /**
     * Inserts a new PaymentMethod record into the database.
     *
     * @param conn   Active database connection
     * @param method The PaymentMethod bean to persist
     * @throws DuplicateEntityException if a default payment method already exists for this account
     * @throws DAOException             if a generic database error occurs
     * @throws IllegalArgumentException if the method is null, or if Payment Method ID or Account ID are null or empty
     */
    public void insert(Connection conn, PaymentMethodBean method) throws DAOException {
        if (method == null) {
            throw new IllegalArgumentException("Cannot insert a null PaymentMethod");
        }
        if (method.getPaymentMethodId() == null || method.getPaymentMethodId().trim().isEmpty() ||
                method.getAccountId() == null || method.getAccountId().trim().isEmpty()) {
            throw new IllegalArgumentException("Payment Method ID and Account ID must be valid and populated");
        }

        String sql = "INSERT INTO payment_method (payment_method_id, account_id, method_type, card_brand, " +
                "card_expiration, card_last_four, flag_default, payment_email, payment_token) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, method.getPaymentMethodId());
            ps.setString(2, method.getAccountId());
            ps.setInt(3, method.getMethodType());
            ps.setString(4, method.getCardBrand());
            ps.setString(5, method.getCardExpiration());
            ps.setString(6, method.getCardLastFour());

            if (method.isDefault()) {
                ps.setBoolean(7, true);
            } else {
                ps.setNull(7, Types.BOOLEAN);
            }

            ps.setString(8, method.getPaymentEmail());
            ps.setString(9, method.getPaymentToken());

            ps.executeUpdate();
            logger.info("Payment method successfully created with Payment Method ID: {} for Account ID: {}",
                    method.getPaymentMethodId(), method.getAccountId());
        } catch (SQLException e) {
            logger.error("Failed to insert payment method for Account ID: {}", method.getAccountId(), e);
            if (SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION.equals(e.getSQLState()) ||
                    e.getErrorCode() == MYSQL_ERR_DUPLICATE_KEY) {
                throw new DuplicateEntityException("A default payment method already exists for Account ID: " +
                        method.getAccountId(), DuplicateEntityException.ConflictingField.DEFAULT_PAYMENT_METHOD);
            }
            throw new DAOException("Error occurred while inserting payment method", e);
        }
    }

    /**
     * Retrieves a payment method configuration by its Payment Method ID.
     *
     * @param conn            Active database connection
     * @param paymentMethodId Unique identifier of the payment method
     * @return An Optional containing the populated bean, or empty if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the paymentMethodId is null or empty
     */
    public Optional<PaymentMethodBean> findById(Connection conn, String paymentMethodId) throws DAOException {
        if (paymentMethodId == null || paymentMethodId.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment Method ID cannot be null or empty for lookup");
        }

        String sql = SELECT_BASE + "WHERE payment_method_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paymentMethodId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching payment method with Payment Method ID: {}", paymentMethodId, e);
            throw new DAOException("Error fetching payment method by Payment Method ID", e);
        }
        return Optional.empty();
    }

    /**
     * Retrieves all payment methods associated with a specific customer Account ID.
     *
     * @param conn      Active database connection
     * @param accountId Unique identifier of the account
     * @return List of all registered payment methods for the account
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the accountId is null or empty
     */
    public List<PaymentMethodBean> findAllByAccountId(Connection conn, String accountId) throws DAOException {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty for lookup");
        }

        String sql = SELECT_BASE + "WHERE account_id = ?";
        List<PaymentMethodBean> methods = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    methods.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving payment methods for Account ID: {}", accountId, e);
            throw new DAOException("Error retrieving payment methods by Account ID", e);
        }
        return methods;
    }

    /**
     * Retrieves the single default payment method associated with a specific customer Account ID.
     *
     * @param conn      Active database connection
     * @param accountId Unique identifier of the account
     * @return An Optional containing the default payment method, or empty if none is set
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the accountId is null or empty
     */
    public Optional<PaymentMethodBean> findDefaultByAccountId(Connection conn, String accountId) throws DAOException {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty for default lookup");
        }

        String sql = SELECT_BASE + "WHERE account_id = ? AND flag_default = TRUE";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching default payment method for Account ID: {}", accountId, e);
            throw new DAOException("Error fetching default payment method", e);
        }
        return Optional.empty();
    }

    /**
     * Retrieves all registered payment methods in the system.
     *
     * @param conn Active database connection
     * @return List of all payment methods
     * @throws DAOException if a database error occurs
     */
    public List<PaymentMethodBean> findAll(Connection conn) throws DAOException {
        List<PaymentMethodBean> methods = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                methods.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving all payment methods", e);
            throw new DAOException("Error retrieving all payment methods", e);
        }
        return methods;
    }

    /**
     * Updates details of an existing payment method configuration.
     *
     * @param conn            Active database connection
     * @param paymentMethodId Unique identifier of the payment method to update
     * @param method          PaymentMethod model containing updated values
     * @return true if the row was updated; false if not found
     * @throws DuplicateEntityException if a default payment method already exists for this account
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the paymentMethodId is null or empty, or if the method is null
     */
    public boolean update(Connection conn, String paymentMethodId, PaymentMethodBean method) throws DAOException {
        if (paymentMethodId == null || paymentMethodId.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment Method ID cannot be null or empty for updates");
        }
        if (method == null) {
            throw new IllegalArgumentException("Cannot update with a null PaymentMethod object");
        }

        String sql = "UPDATE payment_method SET method_type = ?, card_brand = ?, card_expiration = ?, " +
                "card_last_four = ?, flag_default = ?, payment_email = ?, payment_token = ? " +
                "WHERE payment_method_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, method.getMethodType());
            ps.setString(2, method.getCardBrand());
            ps.setString(3, method.getCardExpiration());
            ps.setString(4, method.getCardLastFour());

            if (method.isDefault()) {
                ps.setBoolean(5, true);
            } else {
                ps.setNull(5, Types.BOOLEAN);
            }

            ps.setString(6, method.getPaymentEmail());
            ps.setString(7, method.getPaymentToken());
            ps.setString(8, paymentMethodId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Payment method with Payment Method ID: {} successfully updated", paymentMethodId);
                return true;
            } else {
                logger.warn("Update issued for non-existent Payment Method ID: {}", paymentMethodId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update payment method with Payment Method ID: {}", paymentMethodId, e);
            if (SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION.equals(e.getSQLState()) ||
                    e.getErrorCode() == MYSQL_ERR_DUPLICATE_KEY) {
                throw new DuplicateEntityException("A default payment method already exists for this account", e);
            }
            throw new DAOException("Error updating payment method", e);
        }
        return false;
    }

    /**
     * Updates details of an existing payment method configuration using its domain model representation.
     *
     * @param conn   Active database connection
     * @param method PaymentMethod model containing updated details and unique identifier
     * @return true if the row was updated; false if not found
     * @throws DuplicateEntityException if a default payment method already exists for this account
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the method is null, or if Payment Method ID is invalid
     */
    public boolean update(Connection conn, PaymentMethodBean method) throws DAOException {
        if (method == null || method.getPaymentMethodId() == null) {
            throw new IllegalArgumentException("Attempted to update a null payment method " +
                    "or a payment method without an ID");
        }
        return update(conn, method.getPaymentMethodId(), method);
    }

    /**
     * Unsets the default payment method flag for a specific payment method entry.
     *
     * @param conn            Active database connection
     * @param paymentMethodId Unique identifier of the target payment method
     * @return true if the flag was successfully cleared; false if the record was not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Payment Method ID is null or empty
     */
    public boolean unsetDefault(Connection conn, String paymentMethodId) throws DAOException {
        if (paymentMethodId == null || paymentMethodId.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment Method ID cannot be null or empty for unsetting default");
        }

        String sql = "UPDATE payment_method SET flag_default = NULL WHERE payment_method_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paymentMethodId.trim());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Default flag successfully cleared for Payment Method ID: {}", paymentMethodId);
                return true;
            } else {
                logger.warn("Unset default issued for non-existent Payment Method ID: {}", paymentMethodId);
            }
        } catch (SQLException e) {
            logger.error("Failed to clear default flag for Payment Method ID: {}", paymentMethodId, e);
            throw new DAOException("Error clearing default payment method flag", e);
        }
        return false;
    }

    /**
     * Unsets the default payment method flag for a specific payment method entry.
     *
     * @param conn   Active database connection
     * @param method The payment method for which to unset the default flag
     * @return true if the flag was successfully cleared; false if the record was not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the payment method is null or has no ID
     */
    public boolean unsetDefault(Connection conn, PaymentMethodBean method) throws DAOException {
        if (method == null || method.getPaymentMethodId() == null) {
            throw new IllegalArgumentException("Attempted to unset default for a null payment method " +
                    "or a payment method without an ID");
        }
        return unsetDefault(conn, method.getPaymentMethodId());
    }

    /**
     * Retrieves any single payment method belonging to a specific account, excluding a designated payment method.
     * <p>
     * Useful for finding a fallback default candidate when soft-deleting or modifying an existing payment method.
     * </p>
     *
     * @param conn        Active database connection
     * @param accountId   Unique identifier of the account owner
     * @param excludingId Unique identifier of the payment method to exclude from search
     * @return An Optional containing an alternative payment method, or empty if none exist
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Account ID or excluding Payment Method ID are null or empty
     */
    public Optional<PaymentMethodBean> findAnyByAccountIdExcluding(Connection conn, String accountId,
                                                                   String excludingId) throws DAOException {
        if (accountId == null || accountId.trim().isEmpty() ||
                excludingId == null || excludingId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID and excluding Payment Method ID cannot be null or empty");
        }

        String sql = SELECT_BASE + "WHERE account_id = ? AND payment_method_id <> ? LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId.trim());
            ps.setString(2, excludingId.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while finding candidate payment method for Account ID: " +
                    "{} excluding Payment Method ID: {}", accountId, excludingId, e);
            throw new DAOException("Error finding candidate payment method", e);
        }
        return Optional.empty();
    }

    /**
     * Hard-deletes a payment method configuration.
     * Note: This operation can fail if transactions or active subscription plans
     * are referencing this Payment Method ID.
     *
     * @param conn            Active database connection
     * @param paymentMethodId Unique identifier of the payment method to delete
     * @return true if the record was successfully deleted; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the paymentMethodId is null or empty
     */
    public boolean forceDelete(Connection conn, String paymentMethodId) throws DAOException {
        if (paymentMethodId == null || paymentMethodId.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment Method ID cannot be null or empty for deletion");
        }

        String sql = "DELETE FROM payment_method WHERE payment_method_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paymentMethodId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Payment method with Payment Method ID: {} successfully deleted from database",
                        paymentMethodId);
                return true;
            } else {
                logger.warn("Delete issued for non-existent Payment Method ID: {}", paymentMethodId);
            }
        } catch (SQLException e) {
            logger.error("Failed to delete payment method with Payment Method ID: {}", paymentMethodId, e);
            throw new DAOException("Error deleting payment method", e);
        }
        return false;
    }

    /**
     * Hard-deletes a payment method configuration using its domain model representation.
     * Note: This operation can fail if transactions or active subscription plans
     * are referencing this Payment Method ID.
     *
     * @param conn   Active database connection
     * @param method PaymentMethod model containing the identifier of the record to remove
     * @return true if the record was successfully deleted; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the method is null, or if Payment Method ID is invalid
     */
    public boolean forceDelete(Connection conn, PaymentMethodBean method) throws DAOException {
        if (method == null || method.getPaymentMethodId() == null) {
            throw new IllegalArgumentException("Attempted to delete a null payment method " +
                    "or a payment method without an ID");
        }
        return forceDelete(conn, method.getPaymentMethodId());
    }

    /**
     * Maps a database row from a {@link ResultSet} into a {@link PaymentMethodBean}.
     */
    private PaymentMethodBean mapRow(ResultSet rs) throws SQLException {
        PaymentMethodBean method = new PaymentMethodBean();
        method.setPaymentMethodId(rs.getString("payment_method_id"));
        method.setAccountId(rs.getString("account_id"));
        method.setMethodType(rs.getInt("method_type"));
        method.setCardBrand(rs.getString("card_brand"));
        method.setCardExpiration(rs.getString("card_expiration"));
        method.setCardLastFour(rs.getString("card_last_four"));

        boolean isDefault = rs.getBoolean("flag_default");
        if (rs.wasNull()) {
            method.setDefault(false);
        } else {
            method.setDefault(isDefault);
        }

        method.setPaymentEmail(rs.getString("payment_email"));
        method.setPaymentToken(rs.getString("payment_token"));

        return method;
    }
}
