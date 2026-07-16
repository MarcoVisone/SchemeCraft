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
import com.xyra.schemecraft.model.OrderStatusBean;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

/**
 * Data Access Object (DAO) for managing persistent {@link OrderStatusBean} entities.
 */
public class OrderStatusDAO extends BaseDAO {

    private static final String SELECT_BASE = "SELECT status_id, status_name FROM order_status ";

    /**
     * Inserts a new OrderStatus and populates its generated primary key ID.
     *
     * @param conn   Active database connection
     * @param status The OrderStatus bean to persist
     * @throws DuplicateEntityException if the Status Name already exists
     * @throws DAOException             if a generic database error occurs
     * @throws IllegalArgumentException if the status is null, or if Status Name is null or empty
     */
    public void insert(Connection conn, OrderStatusBean status) throws DAOException {
        if (status == null) {
            throw new IllegalArgumentException("Cannot insert a null OrderStatus");
        }
        if (status.getStatusName() == null || status.getStatusName().trim().isEmpty()) {
            throw new IllegalArgumentException("Status Name must be valid and populated");
        }

        String sql = "INSERT INTO order_status (status_name) VALUES (?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, status.getStatusName().trim());

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    status.setStatusId(generatedKeys.getInt(1));
                } else {
                    throw new DAOException("Creating order status failed, no Status ID obtained.");
                }
            }

            logger.info("Order status successfully created: {} with Status ID: {}", status.getStatusName(),
                    status.getStatusId());
        } catch (SQLException e) {
            logger.error("Failed to insert order status with Status Name: {}", status.getStatusName(), e);
            if (SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION.equals(e.getSQLState()) ||
                    e.getErrorCode() == MYSQL_ERR_DUPLICATE_KEY) {
                throw new DuplicateEntityException("Status Name already exists: " + status.getStatusName(), e);
            }
            throw new DAOException("Error occurred while inserting order status", e);
        }
    }

    /**
     * Retrieves an order status configuration by its Status ID.
     *
     * @param conn     Active database connection
     * @param statusId Unique integer ID of the status
     * @return An Optional containing the populated bean, or empty if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the statusId is less than or equal to 0
     */
    public Optional<OrderStatusBean> findById(Connection conn, int statusId) throws DAOException {
        if (statusId <= 0) {
            throw new IllegalArgumentException("Status ID must be a positive integer");
        }

        String sql = SELECT_BASE + "WHERE status_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, statusId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching order status with Status ID: {}", statusId, e);
            throw new DAOException("Error fetching order status by Status ID", e);
        }
        return Optional.empty();
    }

    /**
     * Retrieves an order status configuration by its Status Name.
     *
     * @param conn       Active database connection
     * @param statusName Unique name of the status
     * @return An Optional containing the populated bean, or empty if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the statusName is null or empty
     */
    public Optional<OrderStatusBean> findByName(Connection conn, String statusName) throws DAOException {
        if (statusName == null || statusName.trim().isEmpty()) {
            throw new IllegalArgumentException("Status Name cannot be null or empty for lookup");
        }

        String sql = SELECT_BASE + "WHERE status_name = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statusName.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching order status with Status Name: {}", statusName, e);
            throw new DAOException("Error fetching order status by Status Name", e);
        }
        return Optional.empty();
    }

    /**
     * Retrieves all configured order statuses ordered by their Status ID.
     *
     * @param conn Active database connection
     * @return List of all registered order statuses
     * @throws DAOException if a database error occurs
     */
    public List<OrderStatusBean> findAll(Connection conn) throws DAOException {
        String sql = SELECT_BASE + "ORDER BY status_id";
        List<OrderStatusBean> statuses = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                statuses.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving all order statuses", e);
            throw new DAOException("Error retrieving all order statuses", e);
        }
        return statuses;
    }

    /**
     * Updates the name of an existing order status.
     *
     * @param conn     Active database connection
     * @param statusId Unique integer ID of the status to update
     * @param status   OrderStatus model containing the new status name
     * @return true if the row was updated; false if the ID was not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the statusId is less than or equal to 0, if status is null,
     * or if Status Name is null or empty
     */
    public boolean update(Connection conn, int statusId, OrderStatusBean status) throws DAOException {
        if (statusId <= 0) {
            throw new IllegalArgumentException("Status ID must be a positive integer for updates");
        }
        if (status == null) {
            throw new IllegalArgumentException("Cannot update with a null OrderStatus object");
        }
        if (status.getStatusName() == null || status.getStatusName().trim().isEmpty()) {
            throw new IllegalArgumentException("New Status Name must be valid and populated");
        }

        String sql = "UPDATE order_status SET status_name = ? WHERE status_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.getStatusName().trim());
            ps.setInt(2, statusId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Order status with Status ID: {} successfully updated to Status Name: {}", statusId,
                        status.getStatusName());
                return true;
            } else {
                logger.warn("Update issued for non-existent Status ID: {}", statusId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update order status with Status ID: {}", statusId, e);
            throw new DAOException("Error updating order status", e);
        }
        return false;
    }

    /**
     * Updates the name of an existing order status using its domain model representation.
     *
     * @param conn   Active database connection
     * @param status OrderStatus model containing updated details and unique identifier
     * @return true if the row was updated; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the status is null, or if Status ID or Status Name are invalid
     */
    public boolean update(Connection conn, OrderStatusBean status) throws DAOException {
        if (status == null) {
            throw new IllegalArgumentException("Attempted to update a null order status");
        }
        return update(conn, status.getStatusId(), status);
    }

    /**
     * Deletes an order status configuration.
     *
     * @param conn     Active database connection
     * @param statusId Unique identifier of the status to delete
     * @return true if the record was successfully deleted; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the statusId is less than or equal to 0
     */
    public boolean delete(Connection conn, int statusId) throws DAOException {
        if (statusId <= 0) {
            throw new IllegalArgumentException("Status ID must be a positive integer for deletion");
        }

        String sql = "DELETE FROM order_status WHERE status_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, statusId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Order status with Status ID: {} successfully deleted from database", statusId);
                return true;
            } else {
                logger.warn("Delete issued for non-existent Status ID: {}", statusId);
            }
        } catch (SQLException e) {
            logger.error("Failed to delete order status with Status ID: {}", statusId, e);
            throw new DAOException("Error deleting order status", e);
        }
        return false;
    }

    /**
     * Deletes an order status configuration using its domain model representation.
     *
     * @param conn   Active database connection
     * @param status OrderStatus model containing the identifier of the record to remove
     * @return true if the record was successfully deleted; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the status is null, or if Status ID is invalid
     */
    public boolean delete(Connection conn, OrderStatusBean status) throws DAOException {
        if (status == null) {
            throw new IllegalArgumentException("Attempted to delete a null order status");
        }
        return delete(conn, status.getStatusId());
    }

    /**
     * Maps a database row from a {@link ResultSet} into an {@link OrderStatusBean}.
     */
    private OrderStatusBean mapRow(ResultSet rs) throws SQLException {
        OrderStatusBean status = new OrderStatusBean();
        status.setStatusId(rs.getInt("status_id"));
        status.setStatusName(rs.getString("status_name"));
        return status;
    }
}
