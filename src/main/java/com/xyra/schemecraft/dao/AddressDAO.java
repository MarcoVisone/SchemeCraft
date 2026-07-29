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
import com.xyra.schemecraft.model.AddressBean;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

/**
 * Data Access Object (DAO) for managing persistent {@link AddressBean} entities.
 */
public class AddressDAO extends BaseDAO {

    private static final String SELECT_BASE = "SELECT address_id, account_id, country_id, city, flag_default, " +
            "is_active, postal_code, state_province, street_address FROM address ";

    /**
     * Inserts a new Address record into the database.
     *
     * @param conn    Active database connection
     * @param address The Address bean to persist
     * @throws DAOException             if a database error occurs
     * @throws DuplicateEntityException if an active default address already exists for the account
     * @throws IllegalArgumentException if the address is null or lacks a valid account ID
     */
    public void insert(Connection conn, AddressBean address) throws DAOException {
        if (address == null) {
            throw new IllegalArgumentException("Cannot insert a null Address");
        }
        if (address.getAccountId() == null || address.getAccountId().trim().isEmpty()) {
            throw new IllegalArgumentException("Address must be associated with a valid Account ID");
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
            logger.info("Address successfully inserted with ID: {} for Account ID: {}", address.getAddressId(),
                    address.getAccountId());
        } catch (SQLException e) {
            logger.error("Failed to insert address for Account ID: {}", address.getAccountId(), e);
            if (e.getErrorCode() == MYSQL_ERR_DUPLICATE_KEY) {
                throw new DuplicateEntityException("An active default address already exists for this account", e);
            }

            if (e.getErrorCode() == MYSQL_ERR_NO_REFERENCED_ROW ||
                    e.getErrorCode() == MYSQL_ERR_NO_REFERENCED_ROW_LEGACY) {
                throw new DAOException("Invalid account or country reference for address", e);
            }

            throw new DAOException("Error occurred while inserting address", e);
        }
    }

    /**
     * Finds an Address by its unique ID.
     *
     * @param conn      Active database connection
     * @param addressId Unique identifier of the target address
     * @return An Optional containing the populated bean, or empty if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the addressId is null or empty
     */
    public Optional<AddressBean> findById(Connection conn, String addressId) throws DAOException {
        if (addressId == null || addressId.trim().isEmpty()) {
            throw new IllegalArgumentException("Address ID cannot be null or empty for lookup");
        }

        String sql = SELECT_BASE + "WHERE address_id = ?";

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

    /**
     * Retrieves all addresses associated with a specific account.
     *
     * @param conn      Active database connection
     * @param accountId Unique identifier of the parent account
     * @return List of addresses linked to the account
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the accountId is null or empty
     */
    public List<AddressBean> findAllByAccountId(Connection conn, String accountId) throws DAOException {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty for retrieval");
        }

        String sql = SELECT_BASE + "WHERE account_id = ?";
        List<AddressBean> addresses = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    addresses.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving addresses for Account ID: {}", accountId, e);
            throw new DAOException("Error retrieving addresses by account ID", e);
        }
        return addresses;
    }

    /**
     * Retrieves all active addresses associated with a specific account.
     *
     * @param conn      Active database connection
     * @param accountId Unique identifier of the parent account
     * @return List of active addresses linked to the account
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the accountId is null or empty
     */
    public List<AddressBean> findAllActiveByAccountId(Connection conn, String accountId) throws DAOException {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty for retrieval");
        }

        String sql = SELECT_BASE + "WHERE account_id = ? AND is_active = TRUE";
        List<AddressBean> addresses = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    addresses.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving active addresses for Account ID: {}", accountId, e);
            throw new DAOException("Error retrieving active addresses by account ID", e);
        }
        return addresses;
    }

    /**
     * Locates the active default address of a given account.
     *
     * @param conn      Active database connection
     * @param accountId Unique identifier of the target account
     * @return An Optional containing the default address, or empty if none is set
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the accountId is null or empty
     */
    public Optional<AddressBean> findDefaultByAccountId(Connection conn, String accountId) throws DAOException {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty for default address lookup");
        }

        String sql = SELECT_BASE + "WHERE account_id = ? AND flag_default = TRUE";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching default address for Account ID: {}", accountId, e);
            throw new DAOException("Error fetching default address", e);
        }
        return Optional.empty();
    }

    /**
     * Retrieves all Address records.
     *
     * @param conn Active database connection
     * @return List of all addresses
     * @throws DAOException if a database error occurs
     */
    public List<AddressBean> findAll(Connection conn) throws DAOException {
        List<AddressBean> addresses = new ArrayList<>();

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

    /**
     * Updates an existing Address record with new values using its unique ID.
     *
     * @param conn      Active database connection
     * @param addressId Unique identifier of the address to modify
     * @param address   Address bean with new properties
     * @return true if the row was successfully updated; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws DuplicateEntityException if changing the default flag conflicts with an existing default address
     * @throws IllegalArgumentException if the addressId is null or empty, or if the address is null
     */
    public boolean update(Connection conn, String addressId, AddressBean address) throws DAOException {
        if (addressId == null || addressId.trim().isEmpty()) {
            throw new IllegalArgumentException("Address ID cannot be null or empty for updates");
        }
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
            if (SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION.equals(e.getSQLState()) ||
                    e.getErrorCode() == MYSQL_ERR_DUPLICATE_KEY) {
                throw new DuplicateEntityException("An active default address already exists for this account", e);
            }
            throw new DAOException("Error updating address", e);
        }
        return false;
    }

    /**
     * Updates an existing Address record with new values using its domain model representation.
     *
     * @param conn    Active database connection
     * @param address The model containing the updated details
     * @return true if the row was successfully updated; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the address is null or does not have a valid ID
     */
    public boolean update(Connection conn, AddressBean address) throws DAOException {
        if (address == null || address.getAddressId() == null) {
            throw new IllegalArgumentException("Attempted to update a null address or an address without an ID");
        }
        return update(conn, address.getAddressId(), address);
    }

    /**
     * Reactivates an Address using its unique ID.
     *
     * @param conn      Active database connection
     * @param addressId Unique identifier of the target address
     * @return true if the address was successfully activated; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the addressId is null or empty
     */
    public boolean activate(Connection conn, String addressId) throws DAOException {
        if (addressId == null || addressId.trim().isEmpty()) {
            throw new IllegalArgumentException("Address ID cannot be null or empty for activation");
        }
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

    /**
     * Reactivates an Address using its domain model representation.
     *
     * @param conn    Active database connection
     * @param address The model containing the target address's identifier
     * @return true if the address was successfully activated; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the address is null or does not have a valid ID
     */
    public boolean activate(Connection conn, AddressBean address) throws DAOException {
        if (address == null || address.getAddressId() == null) {
            throw new IllegalArgumentException("Attempted to activate a null address or an address without an ID");
        }
        return activate(conn, address.getAddressId());
    }

    /**
     * Deactivates an Address using its unique ID.
     *
     * @param conn      Active database connection
     * @param addressId Unique identifier of the target address
     * @return true if the address was successfully deactivated; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the addressId is null or empty
     */
    public boolean deactivate(Connection conn, String addressId) throws DAOException {
        if (addressId == null || addressId.trim().isEmpty()) {
            throw new IllegalArgumentException("Address ID cannot be null or empty for deactivation");
        }
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

    /**
     * Deactivates an Address using its domain model representation.
     *
     * @param conn    Active database connection
     * @param address The model containing the target address's identifier
     * @return true if the address was successfully deactivated; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the address is null or does not have a valid ID
     */
    public boolean deactivate(Connection conn, AddressBean address) throws DAOException {
        if (address == null || address.getAddressId() == null) {
            throw new IllegalArgumentException("Attempted to deactivate a null address or an address without an ID");
        }
        return deactivate(conn, address.getAddressId());
    }

    /**
     * Hard-deletes an Address record from the database using its unique ID.
     *
     * @param conn      Active database connection
     * @param addressId Unique identifier of the address to delete
     * @return true if the record was successfully deleted; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the addressId is null or empty
     */
    public boolean forceDelete(Connection conn, String addressId) throws DAOException {
        if (addressId == null || addressId.trim().isEmpty()) {
            throw new IllegalArgumentException("Address ID cannot be null or empty for deletion");
        }
        String sql = "DELETE FROM address WHERE address_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, addressId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Address with ID: {} successfully force deleted from database", addressId);
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

    /**
     * Hard-deletes an Address record from the database using its domain model representation.
     *
     * @param conn    Active database connection
     * @param address The model containing the target address's identifier
     * @return true if the record was successfully deleted; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the address is null or does not have a valid ID
     */
    public boolean forceDelete(Connection conn, AddressBean address) throws DAOException {
        if (address == null || address.getAddressId() == null) {
            throw new IllegalArgumentException("Attempted to force delete a null address or an address without an ID");
        }
        return forceDelete(conn, address.getAddressId());
    }

    /**
     * Unsets the default address flag for a specific address entry.
     *
     * @param conn      Active database connection
     * @param addressId Unique identifier of the target address
     * @return true if the address flag was successfully updated; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Address ID is null or empty
     */
    public boolean unsetDefault(Connection conn, String addressId) throws DAOException {
        if (addressId == null || addressId.trim().isEmpty()) {
            throw new IllegalArgumentException("Address ID cannot be null or empty for unsetting default");
        }

        String sql = "UPDATE address SET flag_default = NULL WHERE address_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, addressId.trim());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Default flag successfully unset for Address ID: {}", addressId);
                return true;
            } else {
                logger.warn("Unset default issued for non-existent Address ID: {}", addressId);
            }
        } catch (SQLException e) {
            logger.error("Failed to unset default flag for Address ID: {}", addressId, e);
            throw new DAOException("Error unsetting default address", e);
        }
        return false;
    }

    /**
     * Unsets the default address flag for a specific address entry using its domain model representation.
     *
     * @param conn    Active database connection
     * @param address The model containing the target address's identifier
     * @return true if the address flag was successfully updated; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the address is null or does not have a valid ID
     */
    public boolean unsetDefault(Connection conn, AddressBean address) throws DAOException {
        if (address == null || address.getAddressId() == null) {
            throw new IllegalArgumentException("Attempted to unset default address");
        }
        return unsetDefault(conn, address.getAddressId());
    }

    /**
     * Retrieves any single active address belonging to a specific account, excluding a designated address.
     *
     * @param conn             Active database connection
     * @param accountId        Unique identifier of the account owner
     * @param excludeAddressId Unique identifier of the address to exclude from search
     * @return An Optional containing an alternative active address, or empty if none exist
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Account ID is null or empty
     */
    public Optional<AddressBean> findAnyActiveByAccountIdExcluding(Connection conn, String accountId,
                                                                   String excludeAddressId) throws DAOException {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty for lookup");
        }

        String sql = SELECT_BASE + "WHERE account_id = ? AND is_active = TRUE AND address_id <> ? LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId.trim());
            ps.setString(2, excludeAddressId != null ? excludeAddressId.trim() : "");

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching active address for Account ID: {} excluding Address ID: {}",
                    accountId, excludeAddressId, e);
            throw new DAOException("Error fetching active address", e);
        }
        return Optional.empty();
    }

    /**
     * Maps a database row from a {@link ResultSet} into an {@link AddressBean}.
     */
    private AddressBean mapRow(ResultSet rs) throws SQLException {
        AddressBean address = new AddressBean();
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
