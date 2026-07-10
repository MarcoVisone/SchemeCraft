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
import com.xyra.schemecraft.model.ProductVersion;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

public class ProductVersionDAO extends BaseDAO {
    private static final String SELECT_BASE = "SELECT version_id, product_id, changelog, created_at, " +
            "download_count, file_path, minecraft_version, version FROM product_version";

    public void insert(Connection conn, ProductVersion pv) throws DAOException {
        if (pv == null) {
            throw new IllegalArgumentException("Cannot insert a null ProductVersion");
        }

        String sql = "INSERT INTO product_version (version_id, product_id, changelog, download_count, " +
                "file_path, minecraft_version, version) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pv.getVersionId());
            ps.setString(2, pv.getProductId());
            ps.setString(3, pv.getChangelog());
            ps.setInt(4, pv.getDownloadCount());
            ps.setString(5, pv.getFilePath());
            ps.setString(6, pv.getMinecraftVersion());
            ps.setString(7, pv.getVersion());

            ps.executeUpdate();
            logger.info("Product version successfully created: {} (ID: {}) for Product: {}",
                    pv.getVersion(), pv.getVersionId(), pv.getProductId());
        } catch (SQLException e) {
            logger.error("Failed to insert product version {} for product ID: {}", pv.getVersion(),
                    pv.getProductId(), e);
            if (MYSQL_DUPLICATE_KEY_STATE.equals(e.getSQLState()) || e.getErrorCode() == MYSQL_DUPLICATE_KEY_CODE) {
                throw new DuplicateEntityException("Version ID already exists", e);
            }
            throw new DAOException("Error occurred while inserting product version", e);
        }
    }

    public Optional<ProductVersion> findById(Connection conn, String versionId) throws DAOException {
        String sql = SELECT_BASE + " WHERE version_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, versionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching version with ID: {}", versionId, e);
            throw new DAOException("Error fetching product version by ID", e);
        }
        return Optional.empty();
    }

    public List<ProductVersion> findAllByProductId(Connection conn, String productId) throws DAOException {
        String sql = SELECT_BASE + " WHERE product_id = ? ORDER BY created_at DESC";
        List<ProductVersion> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving versions for product ID: {}", productId, e);
            throw new DAOException("Error retrieving versions by product ID", e);
        }
        return list;
    }

    public boolean update(Connection conn, String versionId, ProductVersion pv) throws DAOException {
        if (pv == null) {
            throw new IllegalArgumentException("Cannot update with a null ProductVersion object");
        }

        String sql = "UPDATE product_version SET product_id = ?, changelog = ?, file_path = ?, " +
                "minecraft_version = ?, version = ? WHERE version_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pv.getProductId());
            ps.setString(2, pv.getChangelog());
            ps.setString(3, pv.getFilePath());
            ps.setString(4, pv.getMinecraftVersion());
            ps.setString(5, pv.getVersion());
            ps.setString(6, versionId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Product version with ID: {} successfully updated", versionId);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Failed to update product version with ID: {}", versionId, e);
            throw new DAOException("Error updating product version", e);
        }
        return false;
    }

    public boolean update(Connection conn, ProductVersion pv) throws DAOException {
        if (pv == null || pv.getVersionId() == null) {
            throw new IllegalArgumentException("Attempted to update a null version or a version without an ID");
        }
        return update(conn, pv.getVersionId(), pv);
    }

    public boolean incrementDownloadCount(Connection conn, String versionId) throws DAOException {
        String sql = "UPDATE product_version SET download_count = download_count + 1 WHERE version_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, versionId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Failed to increment download count for version ID: {}", versionId, e);
            throw new DAOException("Error incrementing download counter", e);
        }
    }

    public boolean delete(Connection conn, String versionId) throws DAOException {
        String sql = "DELETE FROM product_version WHERE version_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, versionId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Failed to delete product version with ID: {}", versionId, e);
            throw new DAOException("Error deleting product version", e);
        }
    }

    public boolean delete(Connection conn, ProductVersion pv) throws DAOException {
        if (pv == null || pv.getVersionId() == null) {
            throw new IllegalArgumentException("Attempted to delete a null version or a version without an ID");
        }
        return delete(conn, pv.getVersionId());
    }

    private ProductVersion mapRow(ResultSet rs) throws SQLException {
        ProductVersion pv = new ProductVersion();
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
