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

/**
 * Data Access Object (DAO) for managing persistent {@link CountryBean} entities.
 */
public class CountryDAO extends BaseDAO {

    private static final String SELECT_BASE = "SELECT country_id, country_name, is_active, tax FROM country ";

    /**
     * Inserts a new Country record.
     *
     * @param conn    Active database connection
     * @param country The Country model to persist
     * @throws DuplicateEntityException if the Country ID or Country Name already exists
     * @throws DAOException             if a generic database error occurs
     * @throws IllegalArgumentException if the country is null, or if Country ID or Country Name are null or empty
     */
    public void insert(Connection conn, CountryBean country) throws DAOException {
        if (country == null) {
            throw new IllegalArgumentException("Cannot insert a null Country");
        }
        if (country.getCountryId() == null || country.getCountryId().trim().isEmpty() ||
                country.getCountryName() == null || country.getCountryName().trim().isEmpty()) {
            throw new IllegalArgumentException("Country ID and Country Name must be valid and populated");
        }

        String sql = "INSERT INTO country (country_id, country_name, is_active, tax) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, country.getCountryId());
            ps.setString(2, country.getCountryName());
            ps.setBoolean(3, country.isActive());
            ps.setBigDecimal(4, country.getTax());

            ps.executeUpdate();
            logger.info("Country successfully inserted with Country ID: {}", country.getCountryId());
        } catch (SQLException e) {
            logger.error("Failed to insert country. Country ID: {}, Country Name: {}, Tax: {}, Is Active: {}",
                    country.getCountryId(), country.getCountryName(), country.getTax(), country.isActive(), e);
            if (SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION.equals(e.getSQLState()) ||
                    e.getErrorCode() == MYSQL_ERR_DUPLICATE_KEY) {
                throw new DuplicateEntityException("Country ID or Country Name already exists: " +
                        country.getCountryId(), e);
            }
            throw new DAOException("Error occurred while inserting country", e);
        }
    }

    /**
     * Finds a Country by its unique ID.
     *
     * @param conn      Active database connection
     * @param countryId Unique identifier of the target country
     * @return An Optional containing the populated bean, or empty if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the countryId is null or empty
     */
    public Optional<CountryBean> findById(Connection conn, String countryId) throws DAOException {
        if (countryId == null || countryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Country ID cannot be null or empty for lookup");
        }

        String sql = SELECT_BASE + "WHERE country_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, countryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching country with Country ID: {}", countryId, e);
            throw new DAOException("Error fetching country by ID", e);
        }
        return Optional.empty();
    }

    /**
     * Finds a Country by its unique name.
     *
     * @param conn        Active database connection
     * @param countryName Name of the target country
     * @return An Optional containing the populated bean, or empty if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the countryName is null or empty
     */
    public Optional<CountryBean> findByName(Connection conn, String countryName) throws DAOException {
        if (countryName == null || countryName.trim().isEmpty()) {
            throw new IllegalArgumentException("Country Name cannot be null or empty for lookup");
        }

        String sql = SELECT_BASE + "WHERE country_name = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, countryName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching country with Country Name: {}", countryName, e);
            throw new DAOException("Error fetching country by Name", e);
        }
        return Optional.empty();
    }

    /**
     * Retrieves all countries registered in the system.
     *
     * @param conn Active database connection
     * @return List of all countries
     * @throws DAOException if a database error occurs
     */
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

    /**
     * Retrieves all active countries.
     *
     * @param conn Active database connection
     * @return List of active countries
     * @throws DAOException if a database error occurs
     */
    public List<CountryBean> findAllActive(Connection conn) throws DAOException {
        String sql = SELECT_BASE + "WHERE is_active = TRUE";
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

    /**
     * Updates an existing Country's details using its unique ID.
     *
     * @param conn      Active database connection
     * @param countryId Unique identifier of the country to update
     * @param country   Country model containing new property values
     * @return true if the country was updated; false if the ID was not found
     * @throws DuplicateEntityException if the update violates unique index constraints (country name)
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the countryId is null or empty, or if the country is null
     */
    public boolean update(Connection conn, String countryId, CountryBean country) throws DAOException {
        if (countryId == null || countryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Country ID cannot be null or empty for update");
        }
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
                logger.info("Country with Country ID: {} successfully updated", countryId);
                return true;
            } else {
                logger.warn("Update issued for non-existent Country ID: {}", countryId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update country with Country ID: {}", countryId, e);
            if (SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION.equals(e.getSQLState()) ||
                    e.getErrorCode() == MYSQL_ERR_DUPLICATE_KEY) {
                throw new DuplicateEntityException("Country name already exists: " + country.getCountryName(), e);
            }
            throw new DAOException("Error updating country", e);
        }
        return false;
    }

    /**
     * Updates an existing Country's details using its domain model representation.
     *
     * @param conn    Active database connection
     * @param country Country model containing updated details and unique identifier
     * @return true if the country was updated; false if the ID was not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the country is null or does not have a valid ID
     */
    public boolean update(Connection conn, CountryBean country) throws DAOException {
        if (country == null || country.getCountryId() == null) {
            throw new IllegalArgumentException("Attempted to update a null country or a country without an ID");
        }
        return update(conn, country.getCountryId(), country);
    }

    /**
     * Activates a country to enable purchases using its unique ID.
     *
     * @param conn      Active database connection
     * @param countryId Unique identifier of the target country
     * @return true if the country status changed; false if the country was not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the countryId is null or empty
     */
    public boolean activate(Connection conn, String countryId) throws DAOException {
        if (countryId == null || countryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Country ID cannot be null or empty for activation");
        }

        String sql = "UPDATE country SET is_active = TRUE WHERE country_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, countryId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Country with Country ID: {} successfully reactivated", countryId);
                return true;
            } else {
                logger.warn("Activation issued for non-existent Country ID: {}", countryId);
            }
        } catch (SQLException e) {
            logger.error("Failed to activate country with Country ID: {}", countryId, e);
            throw new DAOException("Error activating country", e);
        }
        return false;
    }

    /**
     * Activates a country to enable purchases using its domain model representation.
     *
     * @param conn    Active database connection
     * @param country Country model containing the identifier of the country to activate
     * @return true if the country status changed; false if the country was not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the country is null or does not have a valid ID
     */
    public boolean activate(Connection conn, CountryBean country) throws DAOException {
        if (country == null || country.getCountryId() == null) {
            throw new IllegalArgumentException("Attempted to activate a null country or a country without an ID");
        }
        return activate(conn, country.getCountryId());
    }

    /**
     * Deactivates a country using its unique ID.
     *
     * @param conn      Active database connection
     * @param countryId Unique identifier of the target country
     * @return true if the country status changed; false if the country was not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the countryId is null or empty
     */
    public boolean deactivate(Connection conn, String countryId) throws DAOException {
        if (countryId == null || countryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Country ID cannot be null or empty for deactivation");
        }

        String sql = "UPDATE country SET is_active = FALSE WHERE country_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, countryId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Country with Country ID: {} successfully deactivated", countryId);
                return true;
            } else {
                logger.warn("Deactivation issued for non-existent Country ID: {}", countryId);
            }
        } catch (SQLException e) {
            logger.error("Failed to deactivate country with Country ID: {}", countryId, e);
            throw new DAOException("Error deactivating country", e);
        }
        return false;
    }

    /**
     * Deactivates a country using its domain model representation.
     *
     * @param conn    Active database connection
     * @param country Country model containing the identifier of the country to deactivate
     * @return true if the country status changed; false if the country was not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the country is null or does not have a valid ID
     */
    public boolean deactivate(Connection conn, CountryBean country) throws DAOException {
        if (country == null || country.getCountryId() == null) {
            throw new IllegalArgumentException("Attempted to deactivate a null country or a country without an ID");
        }
        return deactivate(conn, country.getCountryId());
    }

    /**
     * Hard-deletes a country from the database using its unique ID.
     *
     * @param conn      Active database connection
     * @param countryId Unique identifier of the country to delete
     * @return true if the record was deleted; false if the ID was not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the countryId is null or empty
     */
    public boolean forceDelete(Connection conn, String countryId) throws DAOException {
        if (countryId == null || countryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Country ID cannot be null or empty for force delete");
        }

        String sql = "DELETE FROM country WHERE country_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, countryId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("CRITICAL: Country with Country ID: {} has been PERMANENTLY deleted from the database",
                        countryId);
                return true;
            } else {
                logger.warn("Force delete issued for non-existent Country ID: {}", countryId);
            }
        } catch (SQLException e) {
            logger.error("Failed to force delete country with Country ID: {}", countryId, e);
            throw new DAOException("Error permanently deleting country", e);
        }
        return false;
    }

    /**
     * Hard-deletes a country from the database using its domain model representation.
     *
     * @param conn    Active database connection
     * @param country Country model containing the identifier of the country to delete
     * @return true if the record was deleted; false if the ID was not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the country is null or does not have a valid ID
     */
    public boolean forceDelete(Connection conn, CountryBean country) throws DAOException {
        if (country == null || country.getCountryId() == null) {
            throw new IllegalArgumentException("Attempted to force delete a null country or a country without an ID");
        }
        return forceDelete(conn, country.getCountryId());
    }

    /**
     * Maps a database row from a {@link ResultSet} into a {@link CountryBean}.
     */
    private CountryBean mapRow(ResultSet rs) throws SQLException {
        String id = rs.getString("country_id");
        String name = rs.getString("country_name");
        BigDecimal tax = rs.getBigDecimal("tax");
        boolean isActive = rs.getBoolean("is_active");

        return new CountryBean(id, name, isActive, tax);
    }
}
