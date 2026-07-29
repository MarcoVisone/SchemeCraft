package com.xyra.schemecraft.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.xyra.schemecraft.exception.DAOException;
import com.xyra.schemecraft.exception.DuplicateEntityException;
import com.xyra.schemecraft.model.CurrencyBean;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

/**
 * Data Access Object (DAO) for managing persistent {@link CurrencyBean} entities.
 */
public class CurrencyDAO extends BaseDAO {

    private static final String SELECT_BASE = "SELECT currency_id, currency_name, is_active, symbol FROM currency ";

    /**
     * Inserts a new Currency record into the database.
     *
     * @param conn     Active database connection
     * @param currency The Currency model to persist
     * @throws DuplicateEntityException if the Currency ID (ISO) or Currency Name already exists
     * @throws DAOException             if a generic database error occurs
     * @throws IllegalArgumentException if the currency is null, or if Currency ID or Currency Name are null or empty
     */
    public void insert(Connection conn, CurrencyBean currency) throws DAOException {
        if (currency == null) {
            throw new IllegalArgumentException("Cannot insert a null Currency");
        }
        if (currency.getCurrencyId() == null || currency.getCurrencyId().trim().isEmpty() ||
                currency.getCurrencyName() == null || currency.getCurrencyName().trim().isEmpty()) {
            throw new IllegalArgumentException("Currency ID and Currency Name must be valid and populated");
        }

        String sql = "INSERT INTO currency (currency_id, currency_name, is_active, symbol) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currency.getCurrencyId());
            ps.setString(2, currency.getCurrencyName());
            ps.setBoolean(3, currency.isActive());
            ps.setString(4, currency.getSymbol());

            ps.executeUpdate();
            logger.info("Currency successfully inserted with Currency ID: {}", currency.getCurrencyId());
        } catch (SQLException e) {
            logger.error("Failed to insert currency. Currency ID: {}, Currency Name: {}", currency.getCurrencyId(),
                    currency.getCurrencyName(), e);
            if (SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION.equals(e.getSQLState()) ||
                    e.getErrorCode() == MYSQL_ERR_DUPLICATE_KEY) {
                throw new DuplicateEntityException("Currency ID or Currency Name already exists: " +
                        currency.getCurrencyId(), e);
            }
            throw new DAOException("Error occurred while inserting currency", e);
        }
    }

    /**
     * Finds a Currency by its unique ISO ID.
     *
     * @param conn       Active database connection
     * @param currencyId Unique identifier of the currency
     * @return An Optional containing the populated bean, or empty if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the currencyId is null or empty
     */
    public Optional<CurrencyBean> findById(Connection conn, String currencyId) throws DAOException {
        if (currencyId == null || currencyId.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency ID cannot be null or empty for lookup");
        }

        String sql = SELECT_BASE + "WHERE currency_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currencyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching currency with Currency ID: {}", currencyId, e);
            throw new DAOException("Error fetching currency by ID", e);
        }
        return Optional.empty();
    }

    /**
     * Finds a Currency by its unique descriptive name.
     *
     * @param conn         Active database connection
     * @param currencyName Name of the currency
     * @return An Optional containing the populated bean, or empty if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the currencyName is null or empty
     */
    public Optional<CurrencyBean> findByName(Connection conn, String currencyName) throws DAOException {
        if (currencyName == null || currencyName.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency Name cannot be null or empty for lookup");
        }

        String sql = SELECT_BASE + "WHERE currency_name = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currencyName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching currency with Currency Name: {}", currencyName, e);
            throw new DAOException("Error fetching currency by Name", e);
        }
        return Optional.empty();
    }

    /**
     * Finds the Currency associated with a given Product, resolved via a single JOIN
     * query on {@code product.currency_id = currency.currency_id}.
     *
     * @param conn      Active database connection
     * @param productId Unique identifier of the product
     * @return An Optional containing the populated bean, or empty if the product does not exist
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the productId is null or empty
     */
    public Optional<CurrencyBean> findByProductId(Connection conn, String productId) throws DAOException {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty for lookup");
        }

        String sql = "SELECT c.currency_id, c.currency_name, c.is_active, c.symbol " +
                "FROM product p JOIN currency c ON p.currency_id = c.currency_id " +
                "WHERE p.product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching currency for Product ID: {}", productId, e);
            throw new DAOException("Error fetching currency by product ID", e);
        }
        return Optional.empty();
    }

    /**
     * Retrieves all currencies present in the system database.
     *
     * @param conn Active database connection
     * @return List of all currencies
     * @throws DAOException if a database error occurs
     */
    public List<CurrencyBean> findAll(Connection conn) throws DAOException {
        List<CurrencyBean> currencies = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                currencies.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving all currencies", e);
            throw new DAOException("Error retrieving all currencies", e);
        }
        return currencies;
    }

    /**
     * Retrieves all active currencies configured for customer billing.
     *
     * @param conn Active database connection
     * @return List of active currencies
     * @throws DAOException if a database error occurs
     */
    public List<CurrencyBean> findAllActive(Connection conn) throws DAOException {
        String sql = SELECT_BASE + "WHERE is_active = TRUE";
        List<CurrencyBean> currencies = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                currencies.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving all active currencies", e);
            throw new DAOException("Error retrieving all active currencies", e);
        }
        return currencies;
    }

    /**
     * Updates details of an existing Currency using its unique ID.
     *
     * @param conn       Active database connection
     * @param currencyId Unique identifier of the currency to update
     * @param currency   Currency model with updated fields
     * @return true if the currency was updated; false if not found
     * @throws DuplicateEntityException if the update conflicts with an existing currency name
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the currencyId is null or empty, or if the currency is null
     */
    public boolean update(Connection conn, String currencyId, CurrencyBean currency) throws DAOException {
        if (currencyId == null || currencyId.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency ID cannot be null or empty for update");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Cannot update with a null Currency object");
        }

        String sql = "UPDATE currency SET currency_name = ?, is_active = ?, symbol = ? WHERE currency_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currency.getCurrencyName());
            ps.setBoolean(2, currency.isActive());
            ps.setString(3, currency.getSymbol());
            ps.setString(4, currencyId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Currency with Currency ID: {} successfully updated", currencyId);
                return true;
            } else {
                logger.warn("Update issued for non-existent Currency ID: {}", currencyId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update currency with Currency ID: {}", currencyId, e);
            if (SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION.equals(e.getSQLState()) ||
                    e.getErrorCode() == MYSQL_ERR_DUPLICATE_KEY) {
                throw new DuplicateEntityException("Currency Name already exists: " + currency.getCurrencyName(), e);
            }
            throw new DAOException("Error updating currency", e);
        }
        return false;
    }

    /**
     * Updates details of an existing Currency using its domain model representation.
     *
     * @param conn     Active database connection
     * @param currency Currency model containing updated details and unique identifier
     * @return true if the currency was updated; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the currency is null or does not have a valid ID
     */
    public boolean update(Connection conn, CurrencyBean currency) throws DAOException {
        if (currency == null || currency.getCurrencyId() == null) {
            throw new IllegalArgumentException("Attempted to update a null currency or a currency without an ID");
        }
        return update(conn, currency.getCurrencyId(), currency);
    }

    /**
     * Activates a currency using its unique ID.
     *
     * @param conn       Active database connection
     * @param currencyId Unique identifier of the target currency
     * @return true if status was updated; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the currencyId is null or empty
     */
    public boolean activate(Connection conn, String currencyId) throws DAOException {
        if (currencyId == null || currencyId.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency ID cannot be null or empty for activation");
        }

        String sql = "UPDATE currency SET is_active = TRUE WHERE currency_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currencyId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Currency with Currency ID: {} successfully reactivated", currencyId);
                return true;
            } else {
                logger.warn("Activation issued for non-existent Currency ID: {}", currencyId);
            }
        } catch (SQLException e) {
            logger.error("Failed to activate currency with Currency ID: {}", currencyId, e);
            throw new DAOException("Error activating currency", e);
        }
        return false;
    }

    /**
     * Activates a currency using its domain model representation.
     *
     * @param conn     Active database connection
     * @param currency Currency model containing the identifier of the currency to activate
     * @return true if status was updated; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the currency is null or does not have a valid ID
     */
    public boolean activate(Connection conn, CurrencyBean currency) throws DAOException {
        if (currency == null || currency.getCurrencyId() == null) {
            throw new IllegalArgumentException("Attempted to activate a null currency or a currency without an ID");
        }
        return activate(conn, currency.getCurrencyId());
    }

    /**
     * Deactivates a currency using its unique ID.
     *
     * @param conn       Active database connection
     * @param currencyId Unique identifier of the target currency
     * @return true if status was updated; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the currencyId is null or empty
     */
    public boolean deactivate(Connection conn, String currencyId) throws DAOException {
        if (currencyId == null || currencyId.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency ID cannot be null or empty for deactivation");
        }

        String sql = "UPDATE currency SET is_active = FALSE WHERE currency_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currencyId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Currency with Currency ID: {} successfully deactivated", currencyId);
                return true;
            } else {
                logger.warn("Deactivation issued for non-existent Currency ID: {}", currencyId);
            }
        } catch (SQLException e) {
            logger.error("Failed to deactivate currency with Currency ID: {}", currencyId, e);
            throw new DAOException("Error deactivating currency", e);
        }
        return false;
    }

    /**
     * Deactivates a currency using its domain model representation.
     *
     * @param conn     Active database connection
     * @param currency Currency model containing the identifier of the currency to deactivate
     * @return true if status was updated; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the currency is null or does not have a valid ID
     */
    public boolean deactivate(Connection conn, CurrencyBean currency) throws DAOException {
        if (currency == null || currency.getCurrencyId() == null) {
            throw new IllegalArgumentException("Attempted to deactivate a null currency or a currency without an ID");
        }
        return deactivate(conn, currency.getCurrencyId());
    }

    /**
     * Physically deletes a currency record from the database using its unique ID.
     * Note: This might cause referential integrity errors if other tables link to this currency.
     *
     * @param conn       Active database connection
     * @param currencyId Unique identifier of the currency to delete
     * @return true if the record was removed; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the currencyId is null or empty
     */
    public boolean forceDelete(Connection conn, String currencyId) throws DAOException {
        if (currencyId == null || currencyId.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency ID cannot be null or empty for force delete");
        }

        String sql = "DELETE FROM currency WHERE currency_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currencyId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("CRITICAL: Currency with Currency ID: {} has been PERMANENTLY deleted from the database",
                        currencyId);
                return true;
            } else {
                logger.warn("Force delete issued for non-existent Currency ID: {}", currencyId);
            }
        } catch (SQLException e) {
            logger.error("Failed to force delete currency with Currency ID: {}", currencyId, e);
            throw new DAOException("Error permanently deleting currency", e);
        }
        return false;
    }

    /**
     * Physically deletes a currency record from the database using its domain model representation.
     *
     * @param conn     Active database connection
     * @param currency Currency model containing the identifier of the currency to delete
     * @return true if the record was removed; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the currency is null or does not have a valid ID
     */
    public boolean forceDelete(Connection conn, CurrencyBean currency) throws DAOException {
        if (currency == null || currency.getCurrencyId() == null) {
            throw new IllegalArgumentException("Attempted to force delete a null currency or a currency without an ID");
        }
        return forceDelete(conn, currency.getCurrencyId());
    }

    /**
     * Maps a database row from a {@link ResultSet} into a {@link CurrencyBean}.
     */
    private CurrencyBean mapRow(ResultSet rs) throws SQLException {
        String id = rs.getString("currency_id");
        String name = rs.getString("currency_name");
        String symbol = rs.getString("symbol");
        boolean isActive = rs.getBoolean("is_active");

        return new CurrencyBean(id, name, isActive, symbol);
    }
}
