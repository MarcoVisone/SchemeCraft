package com.xyra.schemecraft.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.xyra.schemecraft.exception.DAOException;
import com.xyra.schemecraft.exception.DuplicateEntityException;
import com.xyra.schemecraft.model.PaymentMethodTypeBean;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

public class PaymentMethodTypeDAO extends BaseDAO {
    private static final String SELECT_BASE = "SELECT type_id, type_name, is_active FROM payment_method_type";

    public void insert(Connection conn, PaymentMethodTypeBean type) throws DAOException {
        if (type == null) {
            throw new IllegalArgumentException("Cannot insert a null PaymentMethodType");
        }

        String sql = "INSERT INTO payment_method_type (type_name, is_active) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, type.getTypeName());
            ps.setBoolean(2, type.isActive());

            ps.executeUpdate();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    type.setTypeId(generatedKeys.getInt(1));
                } else {
                    throw new DAOException("Creating payment method type failed, no ID obtained.");
                }
            }
            logger.info("PaymentMethodType successfully inserted with ID: {}", type.getTypeId());
        } catch (SQLException e) {
            logger.error("Failed to insert payment method type with Name: {}", type.getTypeName(), e);
            if (MYSQL_DUPLICATE_KEY_STATE.equals(e.getSQLState()) || e.getErrorCode() == MYSQL_DUPLICATE_KEY_CODE) {
                throw new DuplicateEntityException("Payment method type name already exists: "
                        + type.getTypeName(), e);
            }
            throw new DAOException("Error occurred while inserting payment method type", e);
        }
    }

    public Optional<PaymentMethodTypeBean> findById(Connection conn, int typeId) throws DAOException {
        String sql = SELECT_BASE + " WHERE type_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, typeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching payment method type with ID: {}", typeId, e);
            throw new DAOException("Error fetching payment method type by ID", e);
        }
        return Optional.empty();
    }

    public Optional<PaymentMethodTypeBean> findByName(Connection conn, String typeName) throws DAOException {
        String sql = SELECT_BASE + " WHERE type_name = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, typeName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching payment method type with Name: {}", typeName, e);
            throw new DAOException("Error fetching payment method type by Name", e);
        }
        return Optional.empty();
    }

    public List<PaymentMethodTypeBean> findAll(Connection conn) throws DAOException {
        List<PaymentMethodTypeBean> types = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                types.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving all payment method types", e);
            throw new DAOException("Error retrieving all payment method types", e);
        }
        return types;
    }

    public List<PaymentMethodTypeBean> findAllActive(Connection conn) throws DAOException {
        String sql = SELECT_BASE + " WHERE is_active = TRUE";
        List<PaymentMethodTypeBean> types = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                types.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving all active payment method types", e);
            throw new DAOException("Error retrieving all active payment method types", e);
        }
        return types;
    }

    public boolean update(Connection conn, int typeId, PaymentMethodTypeBean type) throws DAOException {
        if (type == null) {
            throw new IllegalArgumentException("Cannot update with a null PaymentMethodType object");
        }

        String sql = "UPDATE payment_method_type SET type_name = ?, is_active = ? WHERE type_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type.getTypeName());
            ps.setBoolean(2, type.isActive());
            ps.setInt(3, typeId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("PaymentMethodType with ID: {} successfully updated", typeId);
                return true;
            } else {
                logger.warn("Update issued for non-existent payment method type ID: {}", typeId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update payment method type with ID: {}", typeId, e);
            if (MYSQL_DUPLICATE_KEY_STATE.equals(e.getSQLState()) || e.getErrorCode() == MYSQL_DUPLICATE_KEY_CODE) {
                throw new DuplicateEntityException("Payment method type name already exists for update: "
                        + type.getTypeName(), e);
            }
            throw new DAOException("Error updating payment method type", e);
        }
        return false;
    }

    public boolean update(Connection conn, PaymentMethodTypeBean type) throws DAOException {
        if (type == null) {
            throw new IllegalArgumentException("Attempted to update a null payment method type");
        }
        return update(conn, type.getTypeId(), type);
    }

    public boolean activate(Connection conn, int typeId) throws DAOException {
        String sql = "UPDATE payment_method_type SET is_active = TRUE WHERE type_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, typeId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("PaymentMethodType with ID: {} successfully activated", typeId);
                return true;
            } else {
                logger.warn("Activation issued for non-existent payment method type ID: {}", typeId);
            }
        } catch (SQLException e) {
            logger.error("Failed to activate payment method type with ID: {}", typeId, e);
            throw new DAOException("Error activating payment method type", e);
        }
        return false;
    }

    public boolean activate(Connection conn, PaymentMethodTypeBean type) throws DAOException {
        if (type == null) {
            throw new IllegalArgumentException("Attempted to activate a null payment method type");
        }
        return activate(conn, type.getTypeId());
    }

    public boolean deactivate(Connection conn, int typeId) throws DAOException {
        String sql = "UPDATE payment_method_type SET is_active = FALSE WHERE type_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, typeId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("PaymentMethodType with ID: {} successfully deactivated", typeId);
                return true;
            } else {
                logger.warn("Deactivation issued for non-existent payment method type ID: {}", typeId);
            }
        } catch (SQLException e) {
            logger.error("Failed to deactivate payment method type with ID: {}", typeId, e);
            throw new DAOException("Error deactivating payment method type", e);
        }
        return false;
    }

    public boolean deactivate(Connection conn, PaymentMethodTypeBean type) throws DAOException {
        if (type == null) {
            throw new IllegalArgumentException("Attempted to deactivate a null payment method type");
        }
        return deactivate(conn, type.getTypeId());
    }

    public boolean forceDelete(Connection conn, int typeId) throws DAOException {
        String sql = "DELETE FROM payment_method_type WHERE type_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, typeId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("PaymentMethodType with ID: {} successfully physical deleted from database", typeId);
                return true;
            } else {
                logger.warn("Force delete issued for non-existent payment method type ID: {}", typeId);
            }
        } catch (SQLException e) {
            logger.error("Failed to force delete payment method type with ID: {}", typeId, e);
            throw new DAOException("Error force deleting payment method type", e);
        }
        return false;
    }

    public boolean forceDelete(Connection conn, PaymentMethodTypeBean type) throws DAOException {
        if (type == null) {
            throw new IllegalArgumentException("Attempted to force delete a null payment method type");
        }
        return forceDelete(conn, type.getTypeId());
    }

    private PaymentMethodTypeBean mapRow(ResultSet rs) throws SQLException {
        PaymentMethodTypeBean type = new PaymentMethodTypeBean();
        type.setTypeId(rs.getInt("type_id"));
        type.setTypeName(rs.getString("type_name"));
        type.setActive(rs.getBoolean("is_active"));
        return type;
    }
}
