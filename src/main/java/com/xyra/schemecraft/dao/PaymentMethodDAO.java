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

    public void save(PaymentMethod method) {
        String sql = "INSERT INTO payment_method (payment_method_id, account_id, flag_default, method_type, " +
                "payment_token, card_brand, card_last_four, card_expiration, payment_email) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, method.getPaymentMethodId());
            ps.setString(2, method.getAccountId());

            if (method.isFlagDefault()) {
                ps.setBoolean(3, true);
            } else {
                ps.setNull(3, java.sql.Types.BOOLEAN);
            }

            ps.setInt(4, method.getMethodType());
            ps.setString(5, method.getPaymentToken());
            ps.setString(6, method.getCardBrand());
            ps.setString(7, method.getCardLastFour());
            ps.setString(8, method.getCardExpiration());
            ps.setString(9, method.getPaymentEmail());

            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while saving payment method for account ID: {}", method.getAccountId(), e);
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
        List<PaymentMethod> methods = new ArrayList<>();

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    methods.add(mapResultSetToPaymentMethod(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error occurred while fetching payment methods for account ID: {}", accountId, e);
        }
        return methods;
    }

    private PaymentMethod mapResultSetToPaymentMethod(ResultSet rs) throws SQLException {
        PaymentMethod method = new PaymentMethod();
        method.setPaymentMethodId(rs.getString("payment_method_id"));
        method.setAccountId(rs.getString("account_id"));

        method.setFlagDefault(rs.getBoolean("flag_default"));

        method.setMethodType(rs.getInt("method_type"));
        method.setPaymentToken(rs.getString("payment_token"));
        method.setCardBrand(rs.getString("card_brand"));
        method.setCardLastFour(rs.getString("card_last_four"));
        method.setCardExpiration(rs.getString("card_expiration"));
        method.setPaymentEmail(rs.getString("payment_email"));
        return method;
    }
}
