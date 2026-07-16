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

/**
 * Data Access Object (DAO) for managing persistent {@link PaymentMethodTypeBean} entities.
 */
public class PaymentMethodTypeDAO extends BaseDAO {

    private static final String SELECT_BASE = "SELECT type_id, type_name, is_active FROM payment_method_type ";

    /**
     * Inserts a new PaymentMethodType and populates its generated primary key ID.
     *
     * @param conn Active database connection
     * @param type The PaymentMethodType bean to persist
     * @throws DuplicateEntityException if the Type Name already exists in the database
     * @throws DAOException             if a generic database error occurs
     * @throws IllegalArgumentException if the type is null, or if Type Name is null or empty
     */
    public void insert(Connection conn, PaymentMethodTypeBean type) throws DAOException {
        if (type == null) {
            throw new IllegalArgumentException("Cannot insert a null PaymentMethodType");
        }
        if (type.getTypeName() == null || type.getTypeName().trim().isEmpty()) {
            throw new IllegalArgumentException("Type Name must be valid and populated");
        }

        String sql = "INSERT INTO payment_method_type (type_name, is_active) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, type.getTypeName().trim());
            ps.setBoolean(2, type.isActive());

            ps.executeUpdate();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    type.setTypeId(generatedKeys.getInt(1));
                } else {
                    throw new DAOException("Creating payment method type failed, no ID obtained.");
                }
            }
            logger.info("Payment method type successfully inserted with Type ID: {}", type.getTypeId());
        } catch (SQLException e) {
            logger.error("Failed to insert payment method type with Type Name: {}", type.getTypeName(), e);
            if (SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION.equals(e.getSQLState()) ||
                    e.getErrorCode() == MYSQL_ERR_DUPLICATE_KEY) {
                throw new DuplicateEntityException("Type Name already exists: " + type.getTypeName(), e);
            }
            throw new DAOException("Error occurred while inserting payment method type", e);
        }
    }

    /**
     * Retrieves a payment method type configuration by its Type ID.
     *
     * @param conn   Active database connection
     * @param typeId Unique integer ID of the type
     * @return An Optional containing the populated bean, or empty if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the typeId is less than or equal to zero
     */
    public Optional<PaymentMethodTypeBean> findById(Connection conn, int typeId) throws DAOException {
        if (typeId <= 0) {
            throw new IllegalArgumentException("Type ID must be a positive integer");
        }

        String sql = SELECT_BASE + "WHERE type_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, typeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching payment method type with Type ID: {}", typeId, e);
            throw new DAOException("Error fetching payment method type by Type ID", e);
        }
        return Optional.empty();
    }

    /**
     * Retrieves a payment method type configuration by its unique Type Name.
     *
     * @param conn     Active database connection
     * @param typeName Unique name of the payment type
     * @return An Optional containing the populated bean, or empty if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the typeName is null or empty
     */
    public Optional<PaymentMethodTypeBean> findByName(Connection conn, String typeName) throws DAOException {
        if (typeName == null || typeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Type Name cannot be null or empty for lookup");
        }

        String sql = SELECT_BASE + "WHERE type_name = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, typeName.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching payment method type with Type Name: {}", typeName, e);
            throw new DAOException("Error fetching payment method type by Type Name", e);
        }
        return Optional.empty();
    }

    /**
     * Retrieves all payment method types configured in the system.
     *
     * @param conn Active database connection
     * @return List of all payment method types
     * @throws DAOException if a database error occurs
     */
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

    /**
     * Retrieves all active payment method types configured in the system.
     *
     * @param conn Active database connection
     * @return List of active payment method types
     * @throws DAOException if a database error occurs
     */
    public List<PaymentMethodTypeBean> findAllActive(Connection conn) throws DAOException {
        String sql = SELECT_BASE + "WHERE is_active = TRUE";
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

    /**
     * Updates details of an existing payment method type.
     *
     * @param conn   Active database connection
     * @param typeId Unique integer ID of the payment type to update
     * @param type   PaymentMethodType model containing updated values
     * @return true if the row was updated; false if not found
     * @throws DuplicateEntityException if the new name already conflicts with another record
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the typeId is less than or equal to zero, or if the type/name are invalid
     */
    public boolean update(Connection conn, int typeId, PaymentMethodTypeBean type) throws DAOException {
        if (typeId <= 0) {
            throw new IllegalArgumentException("Type ID must be a positive integer for updates");
        }
        if (type == null) {
            throw new IllegalArgumentException("Cannot update with a null PaymentMethodType object");
        }
        if (type.getTypeName() == null || type.getTypeName().trim().isEmpty()) {
            throw new IllegalArgumentException("New Type Name must be valid and populated");
        }

        String sql = "UPDATE payment_method_type SET type_name = ?, is_active = ? WHERE type_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type.getTypeName().trim());
            ps.setBoolean(2, type.isActive());
            ps.setInt(3, typeId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Payment method type with Type ID: {} successfully updated", typeId);
                return true;
            } else {
                logger.warn("Update issued for non-existent Type ID: {}", typeId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update payment method type with Type ID: {}", typeId, e);
            if (SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION.equals(e.getSQLState()) ||
                    e.getErrorCode() == MYSQL_ERR_DUPLICATE_KEY) {
                throw new DuplicateEntityException("Type Name already exists for update: " + type.getTypeName(), e);
            }
            throw new DAOException("Error updating payment method type", e);
        }
        return false;
    }

    /**
     * Updates details of an existing payment method type using its domain model representation.
     *
     * @param conn   Active database connection
     * @param type   PaymentMethodType model containing updated details and its Type ID
     * @return true if the row was updated; false if not found
     * @throws DuplicateEntityException if the new name already conflicts with another record
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the type is null, or if Type ID is invalid
     */
    public boolean update(Connection conn, PaymentMethodTypeBean type) throws DAOException {
        if (type == null) {
            throw new IllegalArgumentException("Attempted to update a null payment method type");
        }
        return update(conn, type.getTypeId(), type);
    }

    /**
     * Activates a payment method type configuration.
     *
     * @param conn   Active database connection
     * @param typeId Unique integer ID of the target type
     * @return true if activated successfully; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the typeId is less than or equal to zero
     */
    public boolean activate(Connection conn, int typeId) throws DAOException {
        if (typeId <= 0) {
            throw new IllegalArgumentException("Type ID must be a positive integer for activation");
        }

        String sql = "UPDATE payment_method_type SET is_active = TRUE WHERE type_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, typeId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Payment method type with Type ID: {} successfully activated", typeId);
                return true;
            } else {
                logger.warn("Activation issued for non-existent Type ID: {}", typeId);
            }
        } catch (SQLException e) {
            logger.error("Failed to activate payment method type with Type ID: {}", typeId, e);
            throw new DAOException("Error activating payment method type", e);
        }
        return false;
    }

    /**
     * Activates a payment method type configuration using its domain model representation.
     *
     * @param conn   Active database connection
     * @param type   PaymentMethodType model containing the Type ID of the target record to activate
     * @return true if activated successfully; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the type is null, or if Type ID is invalid
     */
    public boolean activate(Connection conn, PaymentMethodTypeBean type) throws DAOException {
        if (type == null) {
            throw new IllegalArgumentException("Attempted to activate a null payment method type");
        }
        return activate(conn, type.getTypeId());
    }

    /**
     * Deactivates a payment method type configuration.
     *
     * @param conn   Active database connection
     * @param typeId Unique integer ID of the target type
     * @return true if deactivated successfully; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the typeId is less than or equal to zero
     */
    public boolean deactivate(Connection conn, int typeId) throws DAOException {
        if (typeId <= 0) {
            throw new IllegalArgumentException("Type ID must be a positive integer for deactivation");
        }

        String sql = "UPDATE payment_method_type SET is_active = FALSE WHERE type_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, typeId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Payment method type with Type ID: {} successfully deactivated", typeId);
                return true;
            } else {
                logger.warn("Deactivation issued for non-existent Type ID: {}", typeId);
            }
        } catch (SQLException e) {
            logger.error("Failed to deactivate payment method type with Type ID: {}", typeId, e);
            throw new DAOException("Error deactivating payment method type", e);
        }
        return false;
    }

    /**
     * Deactivates a payment method type configuration using its domain model representation.
     *
     * @param conn   Active database connection
     * @param type   PaymentMethodType model containing the Type ID of the target record to deactivate
     * @return true if deactivated successfully; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the type is null, or if Type ID is invalid
     */
    public boolean deactivate(Connection conn, PaymentMethodTypeBean type) throws DAOException {
        if (type == null) {
            throw new IllegalArgumentException("Attempted to deactivate a null payment method type");
        }
        return deactivate(conn, type.getTypeId());
    }

    /**
     * Hard-deletes a payment method type from the database.
     *
     * @param conn   Active database connection
     * @param typeId Unique integer ID of the type to delete
     * @return true if the record was deleted; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the typeId is less than or equal to zero
     */
    public boolean forceDelete(Connection conn, int typeId) throws DAOException {
        if (typeId <= 0) {
            throw new IllegalArgumentException("Type ID must be a positive integer for physical deletion");
        }

        String sql = "DELETE FROM payment_method_type WHERE type_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, typeId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Payment method type with Type ID: {} successfully physical deleted from database", typeId);
                return true;
            } else {
                logger.warn("Force delete issued for non-existent Type ID: {}", typeId);
            }
        } catch (SQLException e) {
            logger.error("Failed to force delete payment method type with Type ID: {}", typeId, e);
            throw new DAOException("Error force deleting payment method type", e);
        }
        return false;
    }

    /**
     * Hard-deletes a payment method type using its domain model representation.
     *
     * @param conn   Active database connection
     * @param type   PaymentMethodType model containing the Type ID of the target record to delete
     * @return true if the record was deleted; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the type is null, or if Type ID is invalid
     */
    public boolean forceDelete(Connection conn, PaymentMethodTypeBean type) throws DAOException {
        if (type == null) {
            throw new IllegalArgumentException("Attempted to force delete a null payment method type");
        }
        return forceDelete(conn, type.getTypeId());
    }

    /**
     * Maps a database row from a {@link ResultSet} into a {@link PaymentMethodTypeBean}.
     */
    private PaymentMethodTypeBean mapRow(ResultSet rs) throws SQLException {
        PaymentMethodTypeBean type = new PaymentMethodTypeBean();
        type.setTypeId(rs.getInt("type_id"));
        type.setTypeName(rs.getString("type_name"));
        type.setActive(rs.getBoolean("is_active"));
        return type;
    }
}
