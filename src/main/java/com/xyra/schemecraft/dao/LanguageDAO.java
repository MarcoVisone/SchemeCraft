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
import com.xyra.schemecraft.model.LanguageBean;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

public class LanguageDAO extends BaseDAO {
    private static final String SELECT_BASE = "SELECT language_id, language_name FROM language";

    public void insert(Connection conn, LanguageBean language) throws DAOException {
        if (language == null) {
            throw new IllegalArgumentException("Cannot insert a null Language");
        }

        String sql = "INSERT INTO language (language_id, language_name) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, language.getLanguageId());
            ps.setString(2, language.getLanguageName());

            ps.executeUpdate();
            logger.info("Language successfully inserted with ID: {}", language.getLanguageId());
        } catch (SQLException e) {
            logger.error("Failed to insert language. ID: {}, Name: {}", language.getLanguageId(),
                    language.getLanguageName(), e);
            if (MYSQL_DUPLICATE_KEY_STATE.equals(e.getSQLState()) || e.getErrorCode() == MYSQL_DUPLICATE_KEY_CODE) {
                throw new DuplicateEntityException("Language ID or Name already exists: " +
                        language.getLanguageId(), e);
            }
            throw new DAOException("Error occurred while inserting language", e);
        }
    }

    public Optional<LanguageBean> findById(Connection conn, String languageId) throws DAOException {
        String sql = SELECT_BASE + " WHERE language_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, languageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching language with ID: {}", languageId, e);
            throw new DAOException("Error fetching language by ID", e);
        }
        return Optional.empty();
    }

    public Optional<LanguageBean> findByName(Connection conn, String languageName) throws DAOException {
        String sql = SELECT_BASE + " WHERE language_name = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, languageName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching language with Name: {}", languageName, e);
            throw new DAOException("Error fetching language by Name", e);
        }
        return Optional.empty();
    }

    public List<LanguageBean> findAll(Connection conn) throws DAOException {
        List<LanguageBean> languages = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                languages.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving all languages", e);
            throw new DAOException("Error retrieving all languages", e);
        }
        return languages;
    }

    public boolean update(Connection conn, String languageId, LanguageBean language)
            throws DAOException, IllegalArgumentException {
        if (language == null) {
            throw new IllegalArgumentException("Cannot update with a null Language object");
        }

        String sql = "UPDATE language SET language_name = ? WHERE language_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, language.getLanguageName());
            ps.setString(2, languageId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Language with ID: {} successfully updated", languageId);
                return true;
            } else {
                logger.warn("Update issued for non-existent language ID: {}", languageId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update language with ID: {}", languageId, e);
            if (MYSQL_DUPLICATE_KEY_STATE.equals(e.getSQLState()) || e.getErrorCode() == MYSQL_DUPLICATE_KEY_CODE) {
                throw new DuplicateEntityException("Language name already exists: " + language.getLanguageName(), e);
            }
            throw new DAOException("Error updating language", e);
        }
        return false;
    }

    public boolean update(Connection conn, LanguageBean language) throws DAOException {
        if (language == null || language.getLanguageId() == null) {
            throw new IllegalArgumentException("Attempted to update a null language or a language without an ID");
        }
        return update(conn, language.getLanguageId(), language);
    }

    public boolean delete(Connection conn, String languageId) throws DAOException {
        String sql = "DELETE FROM language WHERE language_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, languageId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Language with ID: {} successfully deleted from database", languageId);
                return true;
            } else {
                logger.warn("Delete issued for non-existent language ID: {}", languageId);
            }
        } catch (SQLException e) {
            logger.error("Failed to delete language with ID: {}", languageId, e);
            throw new DAOException("Error deleting language", e);
        }
        return false;
    }

    public boolean delete(Connection conn, LanguageBean language) throws DAOException {
        if (language == null || language.getLanguageId() == null) {
            throw new IllegalArgumentException("Attempted to delete a null language or a language without an ID");
        }
        return delete(conn, language.getLanguageId());
    }

    private LanguageBean mapRow(ResultSet rs) throws SQLException {
        String id = rs.getString("language_id");
        String name = rs.getString("language_name");
        return new LanguageBean(id, name);
    }
}
