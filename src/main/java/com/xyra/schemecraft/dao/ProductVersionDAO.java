package com.xyra.schemecraft.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.xyra.schemecraft.exception.DAOException;
import com.xyra.schemecraft.exception.DuplicateEntityException;
import com.xyra.schemecraft.model.ProductVersionBean;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

/**
 * Data Access Object (DAO) for managing persistent {@link ProductVersionBean} entities.
 */
public class ProductVersionDAO extends BaseDAO {

    private static final String SELECT_BASE = "SELECT version_id, product_id, changelog, created_at, " +
            "download_count, file_path, minecraft_version, version FROM product_version ";

    /**
     * Persists a new product version release.
     *
     * @param conn Active database connection
     * @param pv   The ProductVersion bean containing release metadata
     * @throws DuplicateEntityException if the Version ID already exists in the database
     * @throws DAOException             if a generic database error occurs
     * @throws IllegalArgumentException if the product version object is null, or if Version ID, Product ID, File Path,
     * or Version String are null or empty
     */
    public void insert(Connection conn, ProductVersionBean pv) throws DAOException {
        if (pv == null) {
            throw new IllegalArgumentException("Cannot insert a null ProductVersion");
        }
        if (pv.getVersionId() == null || pv.getVersionId().trim().isEmpty() ||
                pv.getProductId() == null || pv.getProductId().trim().isEmpty() ||
                pv.getFilePath() == null || pv.getFilePath().trim().isEmpty() ||
                pv.getVersion() == null || pv.getVersion().trim().isEmpty()) {
            throw new IllegalArgumentException("Version ID, Product ID, File Path, " +
                    "and Version String must be valid and populated");
        }

        String sql = "INSERT INTO product_version (version_id, product_id, changelog, download_count, " +
                "file_path, minecraft_version, version) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pv.getVersionId().trim());
            ps.setString(2, pv.getProductId().trim());
            ps.setString(3, pv.getChangelog());
            ps.setInt(4, pv.getDownloadCount());
            ps.setString(5, pv.getFilePath().trim());
            ps.setString(6, pv.getMinecraftVersion() != null ? pv.getMinecraftVersion().trim() : null);
            ps.setString(7, pv.getVersion().trim());

            ps.executeUpdate();
            logger.info("Product version successfully created: {} (Version ID: {}) for Product ID: {}",
                    pv.getVersion(), pv.getVersionId(), pv.getProductId());
        } catch (SQLException e) {
            logger.error("Failed to insert product version {} for Product ID: {}", pv.getVersion(),
                    pv.getProductId(), e);
            if (SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION.equals(e.getSQLState()) ||
                    e.getErrorCode() == MYSQL_ERR_DUPLICATE_KEY) {
                throw new DuplicateEntityException("Version ID already exists: " + pv.getVersionId(), e);
            }
            throw new DAOException("Error occurred while inserting product version", e);
        }
    }

    /**
     * Retrieves a single product version by its unique ID.
     *
     * @param conn      Active database connection
     * @param versionId Unique identifier of the target release version
     * @return An Optional containing the populated bean, or empty if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Version ID is null or empty
     */
    public Optional<ProductVersionBean> findById(Connection conn, String versionId) throws DAOException {
        if (versionId == null || versionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Version ID cannot be null or empty for lookup");
        }

        String sql = SELECT_BASE + "WHERE version_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, versionId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching version with Version ID: {}", versionId, e);
            throw new DAOException("Error fetching product version by Version ID", e);
        }
        return Optional.empty();
    }

    /**
     * Retrieves all release versions belonging to a specific product, sorted chronologically.
     *
     * @param conn      Active database connection
     * @param productId Unique identifier of the parent product
     * @return List of associated product versions ordered by newest first
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Product ID is null or empty
     */
    public List<ProductVersionBean> findAllByProductId(Connection conn, String productId) throws DAOException {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty for versions lookup");
        }

        String sql = SELECT_BASE + "WHERE product_id = ? ORDER BY created_at DESC";
        List<ProductVersionBean> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving versions for Product ID: {}", productId, e);
            throw new DAOException("Error retrieving versions by Product ID", e);
        }
        return list;
    }

    /**
     * Updates details of an existing product version configuration.
     *
     * @param conn      Active database connection
     * @param versionId Unique identifier of the release version to update
     * @param pv        ProductVersion bean containing new parameters
     * @return true if the row was successfully updated; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Version ID is null or empty, if the product version object is null,
     * or if parameters inside the bean are invalid
     */
    public boolean update(Connection conn, String versionId, ProductVersionBean pv) throws DAOException {
        if (versionId == null || versionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Version ID cannot be null or empty for updates");
        }
        if (pv == null) {
            throw new IllegalArgumentException("Cannot update with a null ProductVersion object");
        }
        if (pv.getProductId() == null || pv.getProductId().trim().isEmpty() ||
                pv.getFilePath() == null || pv.getFilePath().trim().isEmpty() ||
                pv.getVersion() == null || pv.getVersion().trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID, File Path, " +
                    "and Version String must be valid and populated for updates");
        }

        String sql = "UPDATE product_version SET product_id = ?, changelog = ?, file_path = ?, " +
                "minecraft_version = ?, version = ? WHERE version_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pv.getProductId().trim());
            ps.setString(2, pv.getChangelog());
            ps.setString(3, pv.getFilePath().trim());
            ps.setString(4, pv.getMinecraftVersion() != null ? pv.getMinecraftVersion().trim() : null);
            ps.setString(5, pv.getVersion().trim());
            ps.setString(6, versionId.trim());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Product version with Version ID: {} successfully updated", versionId);
                return true;
            } else {
                logger.warn("Update issued for non-existent Version ID: {}", versionId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update product version with Version ID: {}", versionId, e);
            throw new DAOException("Error updating product version", e);
        }
        return false;
    }

    /**
     * Updates details of an existing product version configuration using its domain model representation.
     *
     * @param conn Active database connection
     * @param pv   ProductVersion bean containing updated parameters, including its unique Version ID
     * @return true if the row was successfully updated; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the product version object is null, or if its Version ID is missing
     */
    public boolean update(Connection conn, ProductVersionBean pv) throws DAOException {
        if (pv == null || pv.getVersionId() == null) {
            throw new IllegalArgumentException("Attempted to update a null ProductVersion or " +
                    "a version without a Version ID");
        }
        return update(conn, pv.getVersionId(), pv);
    }

    /**
     * Atomically increments the download counter of a specific version by 1.
     *
     * @param conn      Active database connection
     * @param versionId Unique identifier of the downloaded version
     * @return true if the counter was successfully updated; false if the version was not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Version ID is null or empty
     */
    public boolean incrementDownloadCount(Connection conn, String versionId) throws DAOException {
        if (versionId == null || versionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Version ID cannot be null or empty for download tracking");
        }

        String sql = "UPDATE product_version SET download_count = download_count + 1 WHERE version_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, versionId.trim());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Download count successfully incremented for Version ID: {}", versionId);
                return true;
            } else {
                logger.warn("Download increment issued for non-existent Version ID: {}", versionId);
            }
        } catch (SQLException e) {
            logger.error("Failed to increment download count for Version ID: {}", versionId, e);
            throw new DAOException("Error incrementing download counter", e);
        }
        return false;
    }

    /**
     * Deletes a single product version release by its ID.
     *
     * @param conn      Active database connection
     * @param versionId Unique identifier of the version to remove
     * @return true if the record was successfully deleted; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Version ID is null or empty
     */
    public boolean delete(Connection conn, String versionId) throws DAOException {
        if (versionId == null || versionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Version ID cannot be null or empty for deletion");
        }

        String sql = "DELETE FROM product_version WHERE version_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, versionId.trim());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Successfully deleted product version with Version ID: {}", versionId);
                return true;
            } else {
                logger.warn("Delete issued for non-existent Version ID: {}", versionId);
            }
        } catch (SQLException e) {
            logger.error("Failed to delete product version with Version ID: {}", versionId, e);
            throw new DAOException("Error deleting product version", e);
        }
        return false;
    }

    /**
     * Deletes a single product version release using its domain model representation.
     *
     * @param conn Active database connection
     * @param pv   ProductVersion bean containing the unique Version ID to delete
     * @return true if the record was successfully deleted; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the product version object is null, or if its Version ID is missing
     */
    public boolean delete(Connection conn, ProductVersionBean pv) throws DAOException {
        if (pv == null || pv.getVersionId() == null) {
            throw new IllegalArgumentException("Attempted to delete a null ProductVersion or " +
                    "a version without a Version ID");
        }
        return delete(conn, pv.getVersionId());
    }

    /**
     * Maps a database row from a {@link ResultSet} into a {@link ProductVersionBean}.
     */
    private ProductVersionBean mapRow(ResultSet rs) throws SQLException {
        ProductVersionBean pv = new ProductVersionBean();
        pv.setVersionId(rs.getString("version_id"));
        pv.setProductId(rs.getString("product_id"));
        pv.setChangelog(rs.getString("changelog"));
        pv.setDownloadCount(rs.getInt("download_count"));
        pv.setFilePath(rs.getString("file_path"));
        pv.setMinecraftVersion(rs.getString("minecraft_version"));
        pv.setVersion(rs.getString("version"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            pv.setCreatedAt(createdAt.toLocalDateTime());
        }
        return pv;
    }
}
