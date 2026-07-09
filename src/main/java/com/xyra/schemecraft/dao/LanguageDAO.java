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
import com.xyra.schemecraft.model.Language;

public class LanguageDAO {
    private static final Logger logger = LoggerFactory.getLogger(LanguageDAO.class);

    public void save(Language language) {
        String sql = "INSERT INTO language (language_id, language_name) VALUES (?, ?)";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, language.getLanguageId());
            ps.setString(2, language.getLanguageName());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while saving language with ID: {}", language.getLanguageId(), e);
        }
    }

    public Language findById(String languageId) {
        String sql = "SELECT * FROM language WHERE language_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, languageId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String languageName = rs.getString("language_name");
                return new Language(languageId, languageName);
            }
        } catch (SQLException e) {
            logger.error("Error occurred while searching for language with ID: {}", languageId, e);
        }
        return null;
    }

    public List<Language> findAll() {
        String sql = "SELECT * FROM language";
        List<Language> languages = new ArrayList<>();

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String languageId = rs.getString("language_id");
                String languageName = rs.getString("language_name");
                languages.add(new Language(languageId, languageName));
            }
        } catch (SQLException e) {
            logger.error("Error occurred while retrieving all languages", e);
        }
        return languages;
    }

    public void update(Language language) {
        String sql = "UPDATE language SET language_name = ? WHERE language_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, language.getLanguageName());
            ps.setString(2, language.getLanguageId());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while updating language with ID: {}", language.getLanguageId(), e);
        }
    }

    public void deleteById(String languageId) {
        String sql = "DELETE FROM language WHERE language_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, languageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while deleting language with ID: {}", languageId, e);
        }
    }
}
