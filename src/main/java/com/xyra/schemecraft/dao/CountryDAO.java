package com.xyra.schemecraft.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.xyra.schemecraft.exception.DAOException;
import com.xyra.schemecraft.exception.DuplicateEntityException;
import com.xyra.schemecraft.model.CountryBean;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

public class CountryDAO extends BaseDAO {
    private static final String SELECT_BASE = "SELECT country_id, country_name, is_active, tax FROM country";

    public void insert(Connection conn, CountryBean country) throws DAOException {
        if (country == null) {
            throw new IllegalArgumentException("Cannot insert a null Country");
        }

        String sql = "INSERT INTO country (country_id, country_name, is_active, tax) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, country.getCountryId());
            ps.setString(2, country.getCountryName());
            ps.setBoolean(3, country.isActive());
            ps.setBigDecimal(4, country.getTax());

            ps.executeUpdate();
            logger.info("Country successfully inserted with ID: {}", country.getCountryId());
        } catch (SQLException e) {
            logger.error("Failed to insert country. ID: {}, Name: {}, Tax: {}, Is Active: {}",
                    country.getCountryId(), country.getCountryName(), country.getTax(), country.isActive(), e);
            if (MYSQL_DUPLICATE_KEY_STATE.equals(e.getSQLState()) || e.getErrorCode() == MYSQL_DUPLICATE_KEY_CODE) {
                throw new DuplicateEntityException("Country ID or Name already exists: " + country.getCountryId(), e);
            }
            throw new DAOException("Error occurred while inserting country", e);
        }
    }

    public Optional<CountryBean> findById(Connection conn, String countryId) throws DAOException {
        String sql = SELECT_BASE + " WHERE country_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, countryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching country with ID: {}", countryId, e);
            throw new DAOException("Error fetching country by ID", e);
        }
        return Optional.empty();
    }

    public Optional<CountryBean> findByName(Connection conn, String countryName) throws DAOException {
        String sql = SELECT_BASE + " WHERE country_name = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, countryName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching country with Name: {}", countryName, e);
            throw new DAOException("Error fetching country by Name", e);
        }
        return Optional.empty();
    }

    public List<CountryBean> findAll(Connection conn) throws DAOException {
        List<CountryBean> countries = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                countries.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving all countries", e);
            throw new DAOException("Error retrieving all countries", e);
        }
        return countries;
    }

    public List<CountryBean> findAllActive(Connection conn) throws DAOException {
        String sql = SELECT_BASE + " WHERE is_active = TRUE";
        List<CountryBean> countries = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                countries.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving all active countries", e);
            throw new DAOException("Error retrieving all active countries", e);
        }
        return countries;
    }

    public boolean update(Connection conn, String countryId, CountryBean country) throws DAOException {
        if (country == null) {
            throw new IllegalArgumentException("Cannot update with a null Country object");
        }

        String sql = "UPDATE country SET country_name = ?, is_active = ?, tax = ? WHERE country_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, country.getCountryName());
            ps.setBoolean(2, country.isActive());
            ps.setBigDecimal(3, country.getTax());
            ps.setString(4, countryId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Country with ID: {} successfully updated", countryId);
                return true;
            } else {
                logger.warn("Update issued for non-existent country ID: {}", countryId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update country with ID: {}", countryId, e);
            if (MYSQL_DUPLICATE_KEY_STATE.equals(e.getSQLState()) || e.getErrorCode() == MYSQL_DUPLICATE_KEY_CODE) {
                throw new DuplicateEntityException("Country name already exists: " + country.getCountryName(), e);
            }
            throw new DAOException("Error updating country", e);
        }
        return false;
    }

    public boolean update(Connection conn, CountryBean country) throws DAOException {
        if (country == null || country.getCountryId() == null) {
            throw new IllegalArgumentException("Attempted to update a null country or a country without an ID");
        }
        return update(conn, country.getCountryId(), country);
    }

    public boolean activate(Connection conn, String countryId) throws DAOException {
        if (countryId == null || countryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Country ID cannot be null or empty for activation");
        }

        String sql = "UPDATE country SET is_active = TRUE WHERE country_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, countryId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Country with ID: {} successfully reactivated", countryId);
                return true;
            } else {
                logger.warn("Activation issued for non-existent country ID: {}", countryId);
            }
        } catch (SQLException e) {
            logger.error("Failed to activate country with ID: {}", countryId, e);
            throw new DAOException("Error activating country", e);
        }
        return false;
    }

    public boolean activate(Connection conn, CountryBean country) throws DAOException {
        if (country == null || country.getCountryId() == null) {
            throw new IllegalArgumentException("Attempted to activate a null country or a country without an ID");
        }
        return activate(conn, country.getCountryId());
    }

    public boolean deactivate(Connection conn, String countryId) throws DAOException {
        String sql = "UPDATE country SET is_active = FALSE WHERE country_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, countryId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Country with ID: {} successfully deactivated", countryId);
                return true;
            } else {
                logger.warn("Deactivation issued for non-existent country ID: {}", countryId);
            }
        } catch (SQLException e) {
            logger.error("Failed to deactivate country with ID: {}", countryId, e);
            throw new DAOException("Error deactivating country", e);
        }
        return false;
    }

    public boolean deactivate(Connection conn, CountryBean country) throws DAOException {
        if (country == null || country.getCountryId() == null) {
            throw new IllegalArgumentException("Attempted to deactivate a null country or a country without an ID");
        }
        return deactivate(conn, country.getCountryId());
    }

    public boolean forceDelete(Connection conn, String countryId) throws DAOException {
        if (countryId == null || countryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Country ID cannot be null or empty for force delete");
        }

        String sql = "DELETE FROM country WHERE country_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, countryId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("CRITICAL: Country with ID: {} has been PERMANENTLY deleted from the database", countryId);
                return true;
            } else {
                logger.warn("Force delete issued for non-existent country ID: {}", countryId);
            }
        } catch (SQLException e) {
            logger.error("Failed to force delete country with ID: {}", countryId, e);
            throw new DAOException("Error permanently deleting country", e);
        }
        return false;
    }

    public boolean forceDelete(Connection conn, CountryBean country) throws DAOException {
        if (country == null || country.getCountryId() == null) {
            throw new IllegalArgumentException("Attempted to force delete a null country or a country without an ID");
        }
        return forceDelete(conn, country.getCountryId());
    }

    private CountryBean mapRow(ResultSet rs) throws SQLException {
        String id = rs.getString("country_id");
        String name = rs.getString("country_name");
        BigDecimal tax = rs.getBigDecimal("tax");
        boolean isActive = rs.getBoolean("is_active");

        return new CountryBean(id, name, isActive, tax);
    }
}
