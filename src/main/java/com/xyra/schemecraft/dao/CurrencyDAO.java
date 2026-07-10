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
import com.xyra.schemecraft.model.Currency;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

public class CurrencyDAO extends BaseDAO {
    private static final String SELECT_BASE = "SELECT currency_id, currency_name, is_active, symbol FROM currency";

    public void insert(Connection conn, Currency currency) throws DAOException {
        if (currency == null) {
            throw new IllegalArgumentException("Cannot insert a null Currency");
        }

        String sql = "INSERT INTO currency (currency_id, currency_name, is_active, symbol) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currency.getCurrencyId());
            ps.setString(2, currency.getCurrencyName());
            ps.setBoolean(3, currency.isActive());
            ps.setString(4, currency.getSymbol());

            ps.executeUpdate();
            logger.info("Currency successfully inserted with ID: {}", currency.getCurrencyId());
        } catch (SQLException e) {
            logger.error("Failed to insert currency. ID: {}, Name: {}", currency.getCurrencyId(),
                    currency.getCurrencyName(), e);
            if (MYSQL_DUPLICATE_KEY_STATE.equals(e.getSQLState()) || e.getErrorCode() == MYSQL_DUPLICATE_KEY_CODE) {
                throw new DuplicateEntityException("Currency ID or Name already exists: " +
                        currency.getCurrencyId(), e);
            }
            throw new DAOException("Error occurred while inserting currency", e);
        }
    }

    public Optional<Currency> findById(Connection conn, String currencyId) throws DAOException {
        String sql = SELECT_BASE + " WHERE currency_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currencyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching currency with ID: {}", currencyId, e);
            throw new DAOException("Error fetching currency by ID", e);
        }
        return Optional.empty();
    }

    public Optional<Currency> findByName(Connection conn, String currencyName) throws DAOException {
        String sql = SELECT_BASE + " WHERE currency_name = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currencyName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching currency with Name: {}", currencyName, e);
            throw new DAOException("Error fetching currency by Name", e);
        }
        return Optional.empty();
    }

    public List<Currency> findAll(Connection conn) throws DAOException {
        List<Currency> currencies = new ArrayList<>();

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

    public List<Currency> findAllActive(Connection conn) throws DAOException {
        String sql = SELECT_BASE + " WHERE is_active = TRUE";
        List<Currency> currencies = new ArrayList<>();

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

    public boolean update(Connection conn, String currencyId, Currency currency) throws DAOException {
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
                logger.info("Currency with ID: {} successfully updated", currencyId);
                return true;
            } else {
                logger.warn("Update issued for non-existent currency ID: {}", currencyId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update currency with ID: {}", currencyId, e);
            if (MYSQL_DUPLICATE_KEY_STATE.equals(e.getSQLState()) || e.getErrorCode() == MYSQL_DUPLICATE_KEY_CODE) {
                throw new DuplicateEntityException("Currency name already exists: " + currency.getCurrencyName(), e);
            }
            throw new DAOException("Error updating currency", e);
        }
        return false;
    }

    public boolean update(Connection conn, Currency currency) throws DAOException {
        if (currency == null || currency.getCurrencyId() == null) {
            throw new IllegalArgumentException("Attempted to update a null currency or a currency without an ID");
        }
        return update(conn, currency.getCurrencyId(), currency);
    }

    public boolean activate(Connection conn, String currencyId) throws DAOException {
        if (currencyId == null || currencyId.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency ID cannot be null or empty for activation");
        }

        String sql = "UPDATE currency SET is_active = TRUE WHERE currency_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currencyId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Currency with ID: {} successfully reactivated", currencyId);
                return true;
            } else {
                logger.warn("Activation issued for non-existent currency ID: {}", currencyId);
            }
        } catch (SQLException e) {
            logger.error("Failed to activate currency with ID: {}", currencyId, e);
            throw new DAOException("Error activating currency", e);
        }
        return false;
    }

    public boolean activate(Connection conn, Currency currency) throws DAOException {
        if (currency == null || currency.getCurrencyId() == null) {
            throw new IllegalArgumentException("Attempted to activate a null currency or a currency without an ID");
        }
        return activate(conn, currency.getCurrencyId());
    }

    public boolean deactivate(Connection conn, String currencyId) throws DAOException {
        String sql = "UPDATE currency SET is_active = FALSE WHERE currency_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currencyId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Currency with ID: {} successfully deactivated", currencyId);
                return true;
            } else {
                logger.warn("Deactivation issued for non-existent currency ID: {}", currencyId);
            }
        } catch (SQLException e) {
            logger.error("Failed to deactivate currency with ID: {}", currencyId, e);
            throw new DAOException("Error deactivating currency", e);
        }
        return false;
    }

    public boolean deactivate(Connection conn, Currency currency) throws DAOException {
        if (currency == null || currency.getCurrencyId() == null) {
            throw new IllegalArgumentException("Attempted to deactivate a null currency or a currency without an ID");
        }
        return deactivate(conn, currency.getCurrencyId());
    }

    public boolean forceDelete(Connection conn, String currencyId) throws DAOException {
        if (currencyId == null || currencyId.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency ID cannot be null or empty for force delete");
        }

        String sql = "DELETE FROM currency WHERE currency_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currencyId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("CRITICAL: Currency with ID: {} has been " +
                        "PERMANENTLY deleted from the database", currencyId);
                return true;
            } else {
                logger.warn("Force delete issued for non-existent currency ID: {}", currencyId);
            }
        } catch (SQLException e) {
            logger.error("Failed to force delete currency with ID: {}", currencyId, e);
            throw new DAOException("Error permanently deleting currency", e);
        }
        return false;
    }

    public boolean forceDelete(Connection conn, Currency currency) throws DAOException {
        if (currency == null || currency.getCurrencyId() == null) {
            throw new IllegalArgumentException("Attempted to force delete a null currency " +
                    "or a currency without an ID");
        }
        return forceDelete(conn, currency.getCurrencyId());
    }

    private Currency mapRow(ResultSet rs) throws SQLException {
        String id = rs.getString("currency_id");
        String name = rs.getString("currency_name");
        String symbol = rs.getString("symbol");
        boolean isActive = rs.getBoolean("is_active");

        return new Currency(id, name, isActive,symbol);
    }
}
