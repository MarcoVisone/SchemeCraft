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
import com.xyra.schemecraft.model.PaymentMethodType;


public class PaymentMethodTypeDAO {
    private static final Logger logger = LoggerFactory.getLogger(PaymentMethodTypeDAO.class);

    public void save(PaymentMethodType paymentMethodType) {
        String sql = "INSERT INTO payment_method_type (type_name) VALUES (?)";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paymentMethodType.getTypeName());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while saving payment method type: {}", paymentMethodType.getTypeName(), e);
        }
    }

    public PaymentMethodType findById(int paymentMethodTypeId) {
        String sql = "SELECT * FROM payment_method_type WHERE type_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentMethodTypeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPaymentMethodType(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error occurred while searching for payment method type with ID: {}", paymentMethodTypeId, e);
        }

        return null;
    }

    public List<PaymentMethodType> findAll() {
        String sql = "SELECT * FROM payment_method_type";
        List<PaymentMethodType> paymentMethodTypes = new ArrayList<>();

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                PaymentMethodType paymentMethodType = mapResultSetToPaymentMethodType(rs);
                paymentMethodTypes.add(paymentMethodType);
            }
        } catch (SQLException e) {
            logger.error("Error occurred while retrieving all payment method types", e);
        }

        return paymentMethodTypes;
    }

    public void update(PaymentMethodType paymentMethodType) {
        String sql = "UPDATE payment_method_type SET type_id = ? WHERE type_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentMethodType.getTypeId());
            ps.setInt(2, paymentMethodType.getTypeId());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while updating payment method type with ID: {}", paymentMethodType.getTypeId(), e);
        }
    }

    public void deleteById(int paymentMethodTypeId) {
        String sql = "DELETE FROM payment_method_type WHERE type_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentMethodTypeId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while deleting payment method type with ID: {}", paymentMethodTypeId, e);
        }
    }

    private PaymentMethodType mapResultSetToPaymentMethodType(ResultSet rs) throws SQLException {
        int typeId = rs.getInt("type_id");
        String typeName = rs.getString("type_name");
        return new PaymentMethodType(typeId, typeName);
    }
}
