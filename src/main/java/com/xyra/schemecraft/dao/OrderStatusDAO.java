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
import com.xyra.schemecraft.model.OrderStatus;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

public class OrderStatusDAO extends BaseDAO {
    private static final String SELECT_BASE = "SELECT status_id, status_name FROM order_status";

    public void insert(Connection conn, OrderStatus status) throws DAOException {
        if (status == null) {
            throw new IllegalArgumentException("Cannot insert a null OrderStatus");
        }

        String sql = "INSERT INTO order_status (status_name) VALUES (?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, status.getStatusName());

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    status.setStatusId(generatedKeys.getInt(1));
                } else {
                    throw new DAOException("Creating order status failed, no ID obtained.");
                }
            }

            logger.info("Order status successfully created: {} with ID: {}", status.getStatusName(),
                    status.getStatusId());
        } catch (SQLException e) {
            logger.error("Failed to insert order status with Name: {}", status.getStatusName(), e);
            if (MYSQL_DUPLICATE_KEY_STATE.equals(e.getSQLState()) || e.getErrorCode() == MYSQL_DUPLICATE_KEY_CODE) {
                throw new DuplicateEntityException("Order status name already exists: " + status.getStatusName(), e);
            }
            throw new DAOException("Error occurred while inserting order status", e);
        }
    }

    public Optional<OrderStatus> findById(Connection conn, int statusId) throws DAOException {
        String sql = SELECT_BASE + " WHERE status_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, statusId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching order status with ID: {}", statusId, e);
            throw new DAOException("Error fetching order status by ID", e);
        }
        return Optional.empty();
    }

    public Optional<OrderStatus> findByName(Connection conn, String statusName) throws DAOException {
        if (statusName == null || statusName.trim().isEmpty()) {
            return Optional.empty();
        }

        String sql = SELECT_BASE + " WHERE status_name = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statusName.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching order status with Name: {}", statusName, e);
            throw new DAOException("Error fetching order status by Name", e);
        }
        return Optional.empty();
    }

    public List<OrderStatus> findAll(Connection conn) throws DAOException {
        String sql = SELECT_BASE + " ORDER BY status_id";
        List<OrderStatus> statuses = new ArrayList<>();

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

    public boolean update(Connection conn, int statusId, OrderStatus status) throws DAOException {
        if (status == null) {
            throw new IllegalArgumentException("Cannot update with a null OrderStatus object");
        }

        String sql = "UPDATE order_status SET status_name = ? WHERE status_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.getStatusName());
            ps.setInt(2, statusId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Order status with ID: {} successfully updated to: {}", statusId, status.getStatusName());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Failed to update order status with ID: {}", statusId, e);
            throw new DAOException("Error updating order status", e);
        }
        return false;
    }

    public boolean update(Connection conn, OrderStatus status) throws DAOException {
        if (status == null) {
            throw new IllegalArgumentException("Attempted to update a null order status");
        }
        return update(conn, status.getStatusId(), status);
    }

    public boolean delete(Connection conn, int statusId) throws DAOException {
        String sql = "DELETE FROM order_status WHERE status_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, statusId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Failed to delete order status with ID: {}", statusId, e);
            throw new DAOException("Error deleting order status", e);
        }
    }

    public boolean delete(Connection conn, OrderStatus status) throws DAOException {
        if (status == null) {
            throw new IllegalArgumentException("Attempted to delete a null order status");
        }
        return delete(conn, status.getStatusId());
    }

    private OrderStatus mapRow(ResultSet rs) throws SQLException {
        OrderStatus status = new OrderStatus();
        status.setStatusId(rs.getInt("status_id"));
        status.setStatusName(rs.getString("status_name"));
        return status;
    }
}
