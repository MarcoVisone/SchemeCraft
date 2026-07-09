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

    public void save(Currency currency) {
        String sql = "INSERT INTO currency (currency_id, currency_name, symbol) VALUES (?, ?, ?)";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currency.getCurrencyId());
            ps.setString(2, currency.getCurrencyName());
            ps.setString(3, currency.getSymbol());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while saving currency with ID: {}", currency.getCurrencyId(), e);
        }
    }

    public Currency findById(String currencyId) {
        String sql = "SELECT * FROM currency WHERE currency_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currencyId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String currencyName = rs.getString("currency_name");
                String symbol = rs.getString("symbol");
                return new Currency(currencyId, currencyName, symbol);
            }
        } catch (SQLException e) {
            logger.error("Error occurred while finding currency with ID: {}", currencyId, e);
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
                String currencyId = rs.getString("currency_id");
                String currencyName = rs.getString("currency_name");
                String symbol = rs.getString("symbol");
                currencies.add(new Currency(currencyId, currencyName, symbol));
            }
        } catch (SQLException e) {
            logger.error("Error occurred while retrieving all currencies", e);
        }
        return currencies;
    }

    public void update(Currency currency) {
        String sql = "UPDATE currency SET currency_name = ?, symbol = ? WHERE currency_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currency.getCurrencyName());
            ps.setString(2, currency.getSymbol());
            ps.setString(3, currency.getCurrencyId());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while updating currency with ID: {}", currency.getCurrencyId(), e);
        }
    }

    public void deleteById(String currencyId) {
        String sql = "DELETE FROM currency WHERE currency_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currencyId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while deleting currency with ID: {}", currencyId, e);
        }
    }
}
