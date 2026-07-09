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

    public void save(Country country) {
        String sql = "INSERT INTO country (country_id, country_name) VALUES (?, ?)";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, country.getCountryId());
            ps.setString(2, country.getCountryName());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while saving country with ID: {}", country.getCountryId(), e);
        }
    }

    public Country findById(String countryId) {
        String sql = "SELECT * FROM country WHERE country_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, countryId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String countryName = rs.getString("country_name");
                return new Country(countryId, countryName);
            }
        } catch (SQLException e) {
            logger.error("Error occurred while finding country with ID: {}", countryId, e);
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
                String countryId = rs.getString("country_id");
                String countryName = rs.getString("country_name");
                countries.add(new Country(countryId, countryName));
            }
        } catch (SQLException e) {
            logger.error("Error occurred while retrieving all countries", e);
        }
        return countries;
    }

    public void update(Country country) {
        String sql = "UPDATE country SET country_name = ? WHERE country_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, country.getCountryName());
            ps.setString(2, country.getCountryId());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while updating country with ID: {}", country.getCountryId(), e);
        }
    }

    public void deleteById(String countryId) {
        String sql = "DELETE FROM country WHERE country_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, countryId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while deleting country with ID: {}", countryId, e);
        }
    }
}
