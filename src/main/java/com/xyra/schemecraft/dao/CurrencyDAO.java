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
import com.xyra.schemecraft.model.Currency;

public class CurrencyDAO {
    private static final Logger logger = LoggerFactory.getLogger(CurrencyDAO.class);

    public boolean save(Currency currency) {
        String sql = "INSERT INTO currency (currency_id, currency_name, symbol) VALUES (?, ?, ?)";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currency.getCurrencyId());
            ps.setString(2, currency.getCurrencyName());
            ps.setString(3, currency.getSymbol());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Currency successfully saved with ID: {}", currency.getCurrencyId());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Failed to save currency with ID: {} and Name: {}", currency.getCurrencyId(), currency.getCurrencyName(), e);
        }
        return false;
    }

    public Currency findById(String currencyId) {
        String sql = "SELECT * FROM currency WHERE currency_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            ps.setString(1, currencyId);
            if (rs.next()) {
                return mapResultSetToCurrency(rs);
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching currency with ID: {}", currencyId, e);
        }
        return null;
    }

    public List<Currency> findAll() {
        String sql = "SELECT * FROM currency";
        List<Currency> currencies = new ArrayList<>();

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                currencies.add(mapResultSetToCurrency(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving all currencies", e);
        }
        return currencies;
    }

    public boolean update(String currencyId, Currency currency) {
        String sql = "UPDATE currency SET currency_name = ?, symbol = ? WHERE currency_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currency.getCurrencyName());
            ps.setString(2, currency.getSymbol());
            ps.setString(3, currencyId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Currency with ID: {} successfully updated", currencyId);
                return true;
            } else {
                logger.warn("Update issued for non-existent currency ID: {}", currencyId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update currency with ID: {}", currencyId, e);
        }
        return false;
    }

    public boolean update(Currency currency) {
        if (currency == null || currency.getCurrencyId() == null) {
            logger.warn("Attempted to update a null currency or a currency without an ID");
            return false;
        }
        return update(currency.getCurrencyId(), currency);
    }

    public boolean delete(String currencyId) {
        String sql = "DELETE FROM currency WHERE currency_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currencyId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Currency with ID: {} successfully deleted", currencyId);
                return true;
            } else {
                logger.warn("Delete issued for non-existent currency ID: {}", currencyId);
            }
        } catch (SQLException e) {
            logger.error("Failed to delete currency with ID: {}", currencyId, e);
        }
        return false;
    }

    public boolean delete(Currency currency) {
        if (currency == null || currency.getCurrencyId() == null) {
            logger.warn("Attempted to delete a null currency or a currency without an ID");
            return false;
        }
        return delete(currency.getCurrencyId());
    }

    private Currency mapResultSetToCurrency(ResultSet rs) throws SQLException {
        String id = rs.getString("currency_id");
        String name = rs.getString("currency_name");
        String symbol = rs.getString("symbol");
        return new Currency(id, name, symbol);
    }
}
