package com.xyra.schemecraft.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyra.schemecraft.connection.ConnectionPool;
import com.xyra.schemecraft.model.PaymentMethod;

public class PaymentMethodDAO {
    private static final Logger logger = LoggerFactory.getLogger(PaymentMethodDAO.class);

    public void save(PaymentMethod paymentMethod) {
        String sql = "INSERT INTO payment_method (payment_method_id, account_id, flag_default, method_type, " +
                "payment_token, card_brand, card_last_four, card_expiration, payment_email) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paymentMethod.getPaymentMethodId());
            ps.setString(2, paymentMethod.getAccountId());
            if (paymentMethod.isFlagDefault()) {
                ps.setBoolean(3, true);
            } else {
                ps.setNull(3, java.sql.Types.BOOLEAN);
            }
            ps.setInt(4, paymentMethod.getMethodType());
            ps.setString(5, paymentMethod.getPaymentToken());
            ps.setString(6, paymentMethod.getCardBrand());
            ps.setString(7, paymentMethod.getCardLastFour());
            ps.setString(8, paymentMethod.getCardExpiration());
            ps.setString(9, paymentMethod.getPaymentEmail());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while saving payment method for account ID: {}", paymentMethod.getAccountId(), e);
        }
    }

    public PaymentMethod findById(String paymentMethodId) {
        String sql = "SELECT * FROM payment_method WHERE payment_method_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paymentMethodId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPaymentMethod(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error occurred while searching for payment method with ID: {}", paymentMethodId, e);
        }
        return null;
    }

    public List<PaymentMethod> findByAccountId(String accountId) {
        String sql = "SELECT * FROM payment_method WHERE account_id = ?";
        List<PaymentMethod> paymentMethods = new ArrayList<>();

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    paymentMethods.add(mapResultSetToPaymentMethod(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error occurred while fetching payment methods for account ID: {}", accountId, e);
        }
        return paymentMethods;
    }

    public PaymentMethod findDefaultByAccountId(String accountId) {
        String sql = "SELECT * FROM payment_method WHERE account_id = ? AND flag_default = true";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPaymentMethod(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error occurred while searching for default payment method for account ID: {}", accountId, e);
        }
        return null;
    }

    public List<PaymentMethod> findAll() {
        String sql = "SELECT * FROM payment_method";
        List<PaymentMethod> paymentMethods = new ArrayList<>();

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                paymentMethods.add(mapResultSetToPaymentMethod(rs));
            }
        } catch (SQLException e) {
            logger.error("Error occurred while retrieving all payment methods", e);
        }
        return paymentMethods;
    }

    public void update(PaymentMethod paymentMethod) {
        String sql = "UPDATE payment_method SET account_id = ?, flag_default = ?, method_type = ?, " +
                "payment_token = ?, card_brand = ?, card_last_four = ?, card_expiration = ?, payment_email = ? " +
                "WHERE payment_method_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paymentMethod.getAccountId());
            if (paymentMethod.isFlagDefault()) {
                ps.setBoolean(2, true);
            } else {
                ps.setNull(2, java.sql.Types.BOOLEAN);
            }
            ps.setInt(3, paymentMethod.getMethodType());
            ps.setString(4, paymentMethod.getPaymentToken());
            ps.setString(5, paymentMethod.getCardBrand());
            ps.setString(6, paymentMethod.getCardLastFour());
            ps.setString(7, paymentMethod.getCardExpiration());
            ps.setString(8, paymentMethod.getPaymentEmail());
            ps.setString(9, paymentMethod.getPaymentMethodId());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while updating payment method with ID: {}", paymentMethod.getPaymentMethodId(), e);
        }
    }

    public void deleteById(String paymentMethodId) {
        String sql = "DELETE FROM payment_method WHERE payment_method_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paymentMethodId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while deleting payment method with ID: {}", paymentMethodId, e);
        }
    }

    private PaymentMethod mapResultSetToPaymentMethod(ResultSet rs) throws SQLException {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setPaymentMethodId(rs.getString("payment_method_id"));
        paymentMethod.setAccountId(rs.getString("account_id"));
        paymentMethod.setFlagDefault(rs.getBoolean("flag_default"));
        paymentMethod.setMethodType(rs.getInt("method_type"));
        paymentMethod.setPaymentToken(rs.getString("payment_token"));
        paymentMethod.setCardBrand(rs.getString("card_brand"));
        paymentMethod.setCardLastFour(rs.getString("card_last_four"));
        paymentMethod.setCardExpiration(rs.getString("card_expiration"));
        paymentMethod.setPaymentEmail(rs.getString("payment_email"));
        return paymentMethod;
    }
}
