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
import com.xyra.schemecraft.model.Country;

public class CountryDAO {
    private static final Logger logger = LoggerFactory.getLogger(CountryDAO.class);

    public boolean save(Country country) {
        String sql = "INSERT INTO country (country_id, country_name) VALUES (?, ?)";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, country.getCountryId());
            ps.setString(2, country.getCountryName());
            ps.executeUpdate();
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Country successfully saved with ID: {}", country.getCountryId());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Failed to save country with ID: {} and Name: {}", country.getCountryId(), country.getCountryName(), e);
        }
        return false;
    }

    public Country findById(String countryId) {
        String sql = "SELECT * FROM country WHERE country_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery(sql)) {
            if (rs.next()) {
                return mapResultSetToCountry(rs);
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching country with ID: {}", countryId, e);
        }
        return null;
    }

    public List<Country> findAll() {
        String sql = "SELECT * FROM country";
        List<Country> countries = new ArrayList<>();

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                countries.add(mapResultSetToCountry(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving all countries", e);
        }
        return countries;
    }

    public boolean update(String countryId, Country country) {
        String sql = "UPDATE country SET country_name = ? WHERE country_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, country.getCountryName());
            ps.setString(2, countryId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Country with ID: {} successfully updated", countryId);
                return true;
            } else {
                logger.warn("Update issued for non-existent country ID: {}", countryId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update country with ID: {}", countryId, e);
        }
        return false;
    }

    public boolean update(Country country) {
        if (country == null || country.getCountryId() == null) {
            logger.warn("Attempted to update a null country or a country without an ID");
            return false;
        }
        return update(country.getCountryId(), country);
    }

    public boolean delete(String countryId) {
        String sql = "DELETE FROM country WHERE country_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, countryId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Country with ID: {} successfully deleted", countryId);
                return true;
            } else {
                logger.warn("Delete issued for non-existent country ID: {}", countryId);
            }
        } catch (SQLException e) {
            logger.error("Failed to delete country with ID: {}", countryId, e);
        }
        return false;
    }

    public boolean delete(Country country) {
        if (country == null || country.getCountryId() == null) {
            logger.warn("Attempted to delete a null country or a country without an ID");
            return false;
        }
        return delete(country.getCountryId());
    }

    private Country mapResultSetToCountry(ResultSet rs) throws SQLException {
        String id = rs.getString("country_id");
        String name = rs.getString("country_name");
        return new Country(id, name);
    }
}
