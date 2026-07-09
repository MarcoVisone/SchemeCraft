package com.xyra.schemecraft.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyra.schemecraft.connection.ConnectionPool;
import com.xyra.schemecraft.model.Account;

public class AccountDAO {
    private static final  Logger logger = LoggerFactory.getLogger(AccountDAO.class);

    public void save(Account account) {
        String sql = "INSERT INTO account (account_id, username, email, password_hash, is_admin, " +
                "country_id, currency_id, language_id, bio, profile_image_path, banner_path, is_active, balance) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, account.getAccountId());
            ps.setString(2, account.getUsername());
            ps.setString(3, account.getEmail());
            ps.setString(4, account.getPasswordHash());
            ps.setBoolean(5, account.isAdmin());
            ps.setString(6, account.getCountryId());
            ps.setString(7, account.getCurrencyId());
            ps.setString(8, account.getLanguageId());
            ps.setString(9, account.getBio());
            ps.setString(10, account.getProfileImagePath());
            ps.setString(11, account.getBannerPath());
            ps.setBoolean(12, account.isActive());
            ps.setBigDecimal(13, account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while saving account with Username: {}", account.getUsername(), e);
        }
    }
}
