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

/**
 * Data Access Object (DAO) for managing persistent {@link LanguageBean} entities.
 */
public class LanguageDAO extends BaseDAO {

    private static final String SELECT_BASE = "SELECT language_id, language_name FROM language ";

    /**
     * Inserts a new Language record into the database.
     *
     * @param conn     Active database connection
     * @param language The Language model to persist
     * @throws DuplicateEntityException if the Language ID or Language Name already exists
     * @throws DAOException             if a generic database error occurs
     * @throws IllegalArgumentException if the language is null, or if Language ID or Language Name are null or empty
     */
    public void insert(Connection conn, LanguageBean language) throws DAOException {
        if (language == null) {
            throw new IllegalArgumentException("Cannot insert a null Language");
        }
        if (language.getLanguageId() == null || language.getLanguageId().trim().isEmpty() ||
                language.getLanguageName() == null || language.getLanguageName().trim().isEmpty()) {
            throw new IllegalArgumentException("Language ID and Language Name must be valid and populated");
        }

        String sql = "INSERT INTO language (language_id, language_name) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, language.getLanguageId());
            ps.setString(2, language.getLanguageName());

            ps.executeUpdate();
            logger.info("Language successfully inserted with Language ID: {}", language.getLanguageId());
        } catch (SQLException e) {
            logger.error("Failed to insert language. Language ID: {}, Language Name: {}", language.getLanguageId(),
                    language.getLanguageName(), e);
            if (SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION.equals(e.getSQLState()) ||
                    e.getErrorCode() == MYSQL_ERR_DUPLICATE_KEY) {
                throw new DuplicateEntityException("Language ID or Language Name already exists: " +
                        language.getLanguageId(), e);
            }
            throw new DAOException("Error occurred while inserting language", e);
        }
    }

    /**
     * Finds a Language by its unique identifier.
     *
     * @param conn       Active database connection
     * @param languageId Unique identifier of the language
     * @return An Optional containing the populated bean, or empty if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the languageId is null or empty
     */
    public Optional<LanguageBean> findById(Connection conn, String languageId) throws DAOException {
        if (languageId == null || languageId.trim().isEmpty()) {
            throw new IllegalArgumentException("Language ID cannot be null or empty for lookup");
        }

        String sql = SELECT_BASE + "WHERE language_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, languageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching language with Language ID: {}", languageId, e);
            throw new DAOException("Error fetching language by ID", e);
        }
        return Optional.empty();
    }

    /**
     * Finds a Language by its unique descriptive name.
     *
     * @param conn         Active database connection
     * @param languageName Unique name of the target language
     * @return An Optional containing the populated bean, or empty if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the languageName is null or empty
     */
    public Optional<LanguageBean> findByName(Connection conn, String languageName) throws DAOException {
        if (languageName == null || languageName.trim().isEmpty()) {
            throw new IllegalArgumentException("Language Name cannot be null or empty for lookup");
        }

        String sql = SELECT_BASE + "WHERE language_name = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, languageName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching language with Language Name: {}", languageName, e);
            throw new DAOException("Error fetching language by Name", e);
        }
        return Optional.empty();
    }

    /**
     * Retrieves all registered languages in the system.
     *
     * @param conn Active database connection
     * @return List of all languages
     * @throws DAOException if a database error occurs
     */
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

    /**
     * Updates an existing Language's descriptive name using its unique ID.
     *
     * @param conn       Active database connection
     * @param languageId Unique identifier of the language to update
     * @param language   Language model containing the updated name
     * @return true if the row was updated; false if the ID was not found
     * @throws DuplicateEntityException if the new language name already exists
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the languageId is null or empty, or if the language is null
     */
    public boolean update(Connection conn, String languageId, LanguageBean language) throws DAOException {
        if (languageId == null || languageId.trim().isEmpty()) {
            throw new IllegalArgumentException("Language ID cannot be null or empty for update");
        }
        if (language == null) {
            throw new IllegalArgumentException("Cannot update with a null Language object");
        }

        String sql = "UPDATE language SET language_name = ? WHERE language_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, language.getLanguageName());
            ps.setString(2, languageId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Language with Language ID: {} successfully updated", languageId);
                return true;
            } else {
                logger.warn("Update issued for non-existent Language ID: {}", languageId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update language with Language ID: {}", languageId, e);
            if (SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION.equals(e.getSQLState()) ||
                    e.getErrorCode() == MYSQL_ERR_DUPLICATE_KEY) {
                throw new DuplicateEntityException("Language Name already exists: " + language.getLanguageName(), e);
            }
            throw new DAOException("Error updating language", e);
        }
        return false;
    }

    /**
     * Updates an existing Language's descriptive name using its domain model representation.
     *
     * @param conn     Active database connection
     * @param language Language model containing updated details and unique identifier
     * @return true if the row was updated; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the language is null or does not have a valid ID
     */
    public boolean update(Connection conn, LanguageBean language) throws DAOException {
        if (language == null || language.getLanguageId() == null) {
            throw new IllegalArgumentException("Attempted to update a null language or a language without an ID");
        }
        return update(conn, language.getLanguageId(), language);
    }

    /**
     * Deletes a language record from the database using its unique ID.
     *
     * @param conn       Active database connection
     * @param languageId Unique identifier of the language to delete
     * @return true if the record was successfully deleted; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the languageId is null or empty
     */
    public boolean delete(Connection conn, String languageId) throws DAOException {
        if (languageId == null || languageId.trim().isEmpty()) {
            throw new IllegalArgumentException("Language ID cannot be null or empty for deletion");
        }

        String sql = "DELETE FROM language WHERE language_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, languageId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Language with Language ID: {} successfully deleted from database", languageId);
                return true;
            } else {
                logger.warn("Delete issued for non-existent Language ID: {}", languageId);
            }
        } catch (SQLException e) {
            logger.error("Failed to delete language with Language ID: {}", languageId, e);
            throw new DAOException("Error deleting language", e);
        }
        return false;
    }

    /**
     * Deletes a language record from the database using its domain model representation.
     *
     * @param conn     Active database connection
     * @param language Language model containing the identifier of the language to delete
     * @return true if the record was successfully deleted; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the language is null or does not have a valid ID
     */
    public boolean delete(Connection conn, LanguageBean language) throws DAOException {
        if (language == null || language.getLanguageId() == null) {
            throw new IllegalArgumentException("Attempted to delete a null language or a language without an ID");
        }
        return delete(conn, language.getLanguageId());
    }

    /**
     * Maps a database row from a {@link ResultSet} into a {@link LanguageBean}.
     */
    private LanguageBean mapRow(ResultSet rs) throws SQLException {
        String id = rs.getString("language_id");
        String name = rs.getString("language_name");
        return new LanguageBean(id, name);
    }
}
