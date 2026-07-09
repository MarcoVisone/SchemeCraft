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

    public void save(PaymentMethodType type) {
        String sql = "INSERT INTO payment_method_type (type_name) VALUES (?)";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, type.getTypeName());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    type.setTypeId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            logger.error("Error occurred while saving payment method type: {}", type.getTypeName(), e);
        }
    }

    public PaymentMethodType findById(int typeId) {
        String sql = "SELECT * FROM payment_method_type WHERE type_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, typeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToType(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error occurred while searching for payment method type with ID: {}", typeId, e);
        }
        return null;
    }

    public List<PaymentMethodType> findAll() {
        String query = "SELECT * FROM payment_method_type";
        List<PaymentMethodType> list = new ArrayList<>();

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToType(rs));
            }
        } catch (SQLException e) {
            logger.error("Error occurred while fetching all payment method types", e);
        }
        return list;
    }

    private PaymentMethodType mapResultSetToType(ResultSet rs) throws SQLException {
        PaymentMethodType type = new PaymentMethodType();
        type.setTypeId(rs.getInt("type_id"));
        type.setTypeName(rs.getString("type_name"));
        return type;
    }
}
