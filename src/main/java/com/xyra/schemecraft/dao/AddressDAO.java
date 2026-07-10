package com.xyra.schemecraft.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.xyra.schemecraft.exception.DAOException;
import com.xyra.schemecraft.exception.DuplicateEntityException;
import com.xyra.schemecraft.model.Address;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

public class AddressDAO extends BaseDAO {
    private static final String SELECT_BASE = "SELECT address_id, account_id, country_id, city, flag_default, " +
            "is_active, postal_code, state_province, street_address FROM address";

    public void insert(Connection conn, Address address) throws DAOException {
        if (address == null) {
            throw new IllegalArgumentException("Cannot insert a null Address");
        }

        String sql = "INSERT INTO address (address_id, account_id, country_id, city, flag_default, " +
                "is_active, postal_code, state_province, street_address) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, address.getAddressId());
            ps.setString(2, address.getAccountId());
            ps.setString(3, address.getCountryId());
            ps.setString(4, address.getCity());

            if (address.isDefault()) {
                ps.setBoolean(5, true);
            } else {
                ps.setNull(5, Types.BOOLEAN);
            }

            ps.setBoolean(6, address.isActive());
            ps.setString(7, address.getPostalCode());
            ps.setString(8, address.getStateProvince());
            ps.setString(9, address.getStreetAddress());

            ps.executeUpdate();
            logger.info("Address successfully inserted with ID: {} for Account: {}", address.getAddressId(),
                    address.getAccountId());
        } catch (SQLException e) {
            logger.error("Failed to insert address for Account: {}", address.getAccountId(), e);
            if (MYSQL_DUPLICATE_KEY_STATE.equals(e.getSQLState()) || e.getErrorCode() == MYSQL_DUPLICATE_KEY_CODE) {
                throw new DuplicateEntityException("An active default address already exists for this account", e);
            }
            throw new DAOException("Error occurred while inserting address", e);
        }
    }

    public Optional<Address> findById(Connection conn, String addressId) throws DAOException {
        String sql = SELECT_BASE + " WHERE address_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, addressId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching address with ID: {}", addressId, e);
            throw new DAOException("Error fetching address by ID", e);
        }
        return Optional.empty();
    }

    public List<Address> findAllByAccountId(Connection conn, String accountId) throws DAOException {
        String sql = SELECT_BASE + " WHERE account_id = ?";
        List<Address> addresses = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    addresses.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving addresses for account ID: {}", accountId, e);
            throw new DAOException("Error retrieving addresses by account ID", e);
        }
        return addresses;
    }

    public Optional<Address> findDefaultByAccountId(Connection conn, String accountId) throws DAOException {
        String sql = SELECT_BASE + " WHERE account_id = ? AND flag_default = TRUE";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching default address for account ID: {}", accountId, e);
            throw new DAOException("Error fetching default address", e);
        }
        return Optional.empty();
    }

    public List<Address> findAll(Connection conn) throws DAOException {
        List<Address> addresses = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                addresses.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving all addresses", e);
            throw new DAOException("Error retrieving all addresses", e);
        }
        return addresses;
    }

    public boolean update(Connection conn, String addressId, Address address) throws DAOException {
        if (address == null) {
            throw new IllegalArgumentException("Cannot update with a null Address object");
        }

        String sql = "UPDATE address SET country_id = ?, city = ?, flag_default = ?, is_active = ?, " +
                "postal_code = ?, state_province = ?, street_address = ? WHERE address_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, address.getCountryId());
            ps.setString(2, address.getCity());

            if (address.isDefault()) {
                ps.setBoolean(3, true);
            } else {
                ps.setNull(3, Types.BOOLEAN);
            }

            ps.setBoolean(4, address.isActive());
            ps.setString(5, address.getPostalCode());
            ps.setString(6, address.getStateProvince());
            ps.setString(7, address.getStreetAddress());
            ps.setString(8, addressId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Address with ID: {} successfully updated", addressId);
                return true;
            } else {
                logger.warn("Update issued for non-existent address ID: {}", addressId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update address with ID: {}", addressId, e);
            if (MYSQL_DUPLICATE_KEY_STATE.equals(e.getSQLState()) || e.getErrorCode() == MYSQL_DUPLICATE_KEY_CODE) {
                throw new DuplicateEntityException("An active default address already exists for this account", e);
            }
            throw new DAOException("Error updating address", e);
        }
        return false;
    }

    public boolean update(Connection conn, Address address) throws DAOException, IllegalArgumentException {
        if (address == null || address.getAddressId() == null) {
            throw new IllegalArgumentException("Attempted to update a null address or an address without an ID");
        }
        return update(conn, address.getAddressId(), address);
    }

    public boolean activate(Connection conn, String addressId) throws DAOException {
        String sql = "UPDATE address SET is_active = TRUE WHERE address_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, addressId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Address with ID: {} successfully activated", addressId);
                return true;
            } else {
                logger.warn("Activation issued for non-existent address ID: {}", addressId);
            }
        } catch (SQLException e) {
            logger.error("Failed to activate address with ID: {}", addressId, e);
            throw new DAOException("Error activating address", e);
        }
        return false;
    }

    public boolean activate(Connection conn, Address address) throws DAOException, IllegalArgumentException {
        if (address == null || address.getAddressId() == null) {
            throw new IllegalArgumentException("Attempted to activate a null address or an address without an ID");
        }
        return activate(conn, address.getAddressId());
    }

    public boolean deactivate(Connection conn, String addressId) throws DAOException {
        String sql = "UPDATE address SET is_active = FALSE WHERE address_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, addressId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Address with ID: {} successfully deactivated", addressId);
                return true;
            } else {
                logger.warn("Deactivation issued for non-existent address ID: {}", addressId);
            }
        } catch (SQLException e) {
            logger.error("Failed to deactivate address with ID: {}", addressId, e);
            throw new DAOException("Error deactivating address", e);
        }
        return false;
    }

    public boolean deactivate(Connection conn, Address address) throws DAOException, IllegalArgumentException {
        if (address == null || address.getAddressId() == null) {
            throw new IllegalArgumentException("Attempted to deactivate a null address or an address without an ID");
        }
        return deactivate(conn, address.getAddressId());
    }

    public boolean forceDelete(Connection conn, String addressId) throws DAOException {
        String sql = "DELETE FROM address WHERE address_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, addressId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Address with ID: {} successfully physical deleted from database", addressId);
                return true;
            } else {
                logger.warn("Force delete issued for non-existent address ID: {}", addressId);
            }
        } catch (SQLException e) {
            logger.error("Failed to force delete address with ID: {}", addressId, e);
            throw new DAOException("Error force deleting address", e);
        }
        return false;
    }

    public boolean forceDelete(Connection conn, Address address) throws DAOException, IllegalArgumentException {
        if (address == null || address.getAddressId() == null) {
            throw new IllegalArgumentException("Attempted to force delete a null address or an address without an ID");
        }
        return forceDelete(conn, address.getAddressId());
    }

    private Address mapRow(ResultSet rs) throws SQLException {
        Address address = new Address();
        address.setAddressId(rs.getString("address_id"));
        address.setAccountId(rs.getString("account_id"));
        address.setCountryId(rs.getString("country_id"));
        address.setCity(rs.getString("city"));

        boolean isDefault = rs.getBoolean("flag_default");
        if (rs.wasNull()) {
            address.setDefault(false);
        } else {
            address.setDefault(isDefault);
        }

        address.setActive(rs.getBoolean("is_active"));
        address.setPostalCode(rs.getString("postal_code"));
        address.setStateProvince(rs.getString("state_province"));
        address.setStreetAddress(rs.getString("street_address"));

        return address;
    }
}
