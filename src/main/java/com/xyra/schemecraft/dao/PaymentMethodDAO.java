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
import com.xyra.schemecraft.model.PaymentMethod;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

public class PaymentMethodDAO extends BaseDAO {
    private static final String SELECT_BASE = "SELECT payment_method_id, account_id, method_type, card_brand, " +
            "card_expiration, card_last_four, flag_default, payment_email, payment_token FROM payment_method";

    public void insert(Connection conn, PaymentMethod method) throws DAOException, IllegalArgumentException {
        if (method == null) {
            throw new IllegalArgumentException("Cannot insert a null PaymentMethod");
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
            logger.info("PaymentMethod successfully inserted with ID: {} for Account: {}", method.getPaymentMethodId(),
                    method.getAccountId());
        } catch (SQLException e) {
            logger.error("Failed to insert payment method for Account: {}", method.getAccountId(), e);
            if (MYSQL_DUPLICATE_KEY_STATE.equals(e.getSQLState()) || e.getErrorCode() == MYSQL_DUPLICATE_KEY_CODE) {
                throw new DuplicateEntityException("A default payment method already exists for this account", e);
            }
            throw new DAOException("Error occurred while inserting payment method", e);
        }
    }

    public Optional<PaymentMethod> findById(Connection conn, String paymentMethodId) throws DAOException {
        String sql = SELECT_BASE + " WHERE payment_method_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paymentMethodId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching payment method with ID: {}", paymentMethodId, e);
            throw new DAOException("Error fetching payment method by ID", e);
        }
        return Optional.empty();
    }

    public List<PaymentMethod> findAllByAccountId(Connection conn, String accountId) throws DAOException {
        String sql = SELECT_BASE + " WHERE account_id = ?";
        List<PaymentMethod> methods = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    methods.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving payment methods for account ID: {}", accountId, e);
            throw new DAOException("Error retrieving payment methods by account ID", e);
        }
        return methods;
    }

    public Optional<PaymentMethod> findDefaultByAccountId(Connection conn, String accountId) throws DAOException {
        String sql = SELECT_BASE + " WHERE account_id = ? AND flag_default = TRUE";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching default payment method for account ID: {}", accountId, e);
            throw new DAOException("Error fetching default payment method", e);
        }
        return Optional.empty();
    }

    public List<PaymentMethod> findAll(Connection conn) throws DAOException {
        List<PaymentMethod> methods = new ArrayList<>();

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

    public boolean update(Connection conn, String paymentMethodId, PaymentMethod method) throws DAOException {
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
                logger.info("PaymentMethod with ID: {} successfully updated", paymentMethodId);
                return true;
            } else {
                logger.warn("Update issued for non-existent payment method ID: {}", paymentMethodId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update payment method with ID: {}", paymentMethodId, e);
            if (MYSQL_DUPLICATE_KEY_STATE.equals(e.getSQLState()) || e.getErrorCode() == MYSQL_DUPLICATE_KEY_CODE) {
                throw new DuplicateEntityException("A default payment method already exists for this account", e);
            }
            throw new DAOException("Error updating payment method", e);
        }
        return false;
    }

    public boolean update(Connection conn, PaymentMethod method) throws DAOException {
        if (method == null || method.getPaymentMethodId() == null) {
            throw new IllegalArgumentException("Attempted to update a null payment method " +
                    "or a payment method without an ID");
        }
        return update(conn, method.getPaymentMethodId(), method);
    }

    public boolean forceDelete(Connection conn, String paymentMethodId) throws DAOException {
        String sql = "DELETE FROM payment_method WHERE payment_method_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paymentMethodId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("PaymentMethod with ID: {} successfully deleted from database", paymentMethodId);
                return true;
            } else {
                logger.warn("Delete issued for non-existent payment method ID: {}", paymentMethodId);
            }
        } catch (SQLException e) {
            logger.error("Failed to delete payment method with ID: {}", paymentMethodId, e);
            throw new DAOException("Error deleting payment method", e);
        }
        return false;
    }

    public boolean forceDelete(Connection conn, PaymentMethod method) throws DAOException {
        if (method == null || method.getPaymentMethodId() == null) {
            throw new IllegalArgumentException("Attempted to delete a null payment method " +
                    "or a payment method without an ID");
        }
        return forceDelete(conn, method.getPaymentMethodId());
    }

    private PaymentMethod mapRow(ResultSet rs) throws SQLException {
        PaymentMethod method = new PaymentMethod();
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
