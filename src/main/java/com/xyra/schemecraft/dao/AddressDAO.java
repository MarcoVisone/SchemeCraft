package com.xyra.schemecraft.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyra.schemecraft.connection.ConnectionPool;
import com.xyra.schemecraft.model.Address;

public class AddressDAO {
    private static final Logger logger = LoggerFactory.getLogger(AddressDAO.class);

    public void save(Address address) {
        String sql = "INSERT INTO address (address_id, account_id, flag_default, street_address, " +
                "city, state_province, postal_code, country_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, address.getAddressId());
            ps.setString(2, address.getAccountId());

            if (address.isFlagDefault()) {
                ps.setBoolean(3, true);
            } else {
                ps.setNull(3, Types.BOOLEAN);
            }

            ps.setString(4, address.getStreetAddress());
            ps.setString(5, address.getCity());
            ps.setString(6, address.getStateProvince());
            ps.setString(7, address.getPostalCode());
            ps.setString(8, address.getCountryId());

            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while saving address for account ID: {}", address.getAccountId(), e);
        }
    }

    public Address findById(String addressId) {
        String sql = "SELECT * FROM address WHERE address_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, addressId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAddress(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error occurred while searching for address with ID: {}", addressId, e);
        }
        return null;
    }

    public List<Address> findByAccountId(String accountId) {
        String sql = "SELECT * FROM address WHERE account_id = ?";
        List<Address> addresses = new ArrayList<>();

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    addresses.add(mapResultSetToAddress(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error occurred while fetching addresses for account ID: {}", accountId, e);
        }
        return addresses;
    }

    private Address mapResultSetToAddress(ResultSet rs) throws SQLException {
        Address address = new Address();
        address.setAddressId(rs.getString("address_id"));
        address.setAccountId(rs.getString("account_id"));

        address.setFlagDefault(rs.getBoolean("flag_default"));

        address.setStreetAddress(rs.getString("street_address"));
        address.setCity(rs.getString("city"));
        address.setStateProvince(rs.getString("state_province"));
        address.setPostalCode(rs.getString("postal_code"));
        address.setCountryId(rs.getString("country_id"));
        return address;
    }
}
