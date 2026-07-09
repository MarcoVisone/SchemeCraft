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

    public boolean save(Language language) {
        String sql = "INSERT INTO language (language_id, language_name) VALUES (?, ?)";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, language.getLanguageId());
            ps.setString(2, language.getLanguageName());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Language successfully saved with ID: {}", language.getLanguageId());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Failed to save language with ID: {} and Name: {}", language.getLanguageId(), language.getLanguageName(), e);
        }
        return false;
    }

    public Language findById(String languageId) {
        String sql = "SELECT * FROM language WHERE language_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            ps.setString(1, languageId);
            if (rs.next()) {
                return mapResultSetToLanguage(rs);
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching language with ID: {}", languageId, e);
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
                languages.add(mapResultSetToLanguage(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving all languages", e);
        }
        return languages;
    }

    public boolean update(String languageId, Language language) {
        String sql = "UPDATE language SET language_name = ? WHERE language_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, language.getLanguageName());
            ps.setString(2, languageId); // L'ID passato esplicitamente come filtro WHERE
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Language with ID: {} successfully updated", languageId);
                return true;
            } else {
                logger.warn("Update issued for non-existent language ID: {}", languageId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update language with ID: {}", languageId, e);
        }
        return false;
    }

    public boolean update(Language language) {
        if (language == null || language.getLanguageId() == null) {
            logger.warn("Attempted to update a null language or a language without an ID");
            return false;
        }
        return update(language.getLanguageId(), language);
    }

    public boolean delete(String languageId) {
        String sql = "DELETE FROM language WHERE language_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, languageId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Language with ID: {} successfully deleted", languageId);
                return true;
            } else {
                logger.warn("Delete issued for non-existent language ID: {}", languageId);
            }
        } catch (SQLException e) {
            logger.error("Failed to delete language with ID: {}", languageId, e);
        }
        return false;
    }

    public boolean delete(Language language) {
        if (language == null || language.getLanguageId() == null) {
            logger.warn("Attempted to delete a null language or a language without an ID");
            return false;
        }
        return delete(language.getLanguageId());
    }

    private Language mapResultSetToLanguage(ResultSet rs) throws SQLException {
        String id = rs.getString("language_id");
        String name = rs.getString("language_name");
        return new Language(id, name);
    }
}
