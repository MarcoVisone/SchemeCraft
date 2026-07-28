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
import com.xyra.schemecraft.model.ProductImageBean;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

/**
 * Data Access Object (DAO) for managing persistent {@link ProductImageBean} entities.
 */
public class ProductImageDAO extends BaseDAO {

    private static final String SELECT_BASE = "SELECT image_id, product_id, image_path FROM product_image ";

    /**
     * Registers a new product image configuration in the database.
     *
     * @param conn  Active database connection
     * @param image The ProductImage bean to insert
     * @throws DuplicateEntityException if the Image ID already exists in the database
     * @throws DAOException             if a generic database error occurs
     * @throws IllegalArgumentException if the image object is null, or if Image ID, Product ID,
     * or Image Path are null or empty
     */
    public void insert(Connection conn, ProductImageBean image) throws DAOException {
        if (image == null) {
            throw new IllegalArgumentException("Cannot insert a null ProductImage");
        }
        if (image.getImageId() == null || image.getImageId().trim().isEmpty() ||
                image.getProductId() == null || image.getProductId().trim().isEmpty() ||
                image.getImagePath() == null || image.getImagePath().trim().isEmpty()) {
            throw new IllegalArgumentException("Image ID, Product ID, and Image Path must be valid and populated");
        }

        String sql = "INSERT INTO product_image (image_id, product_id, image_path, display_order) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, image.getImageId().trim());
            ps.setString(2, image.getProductId().trim());
            ps.setString(3, image.getImagePath().trim());
            ps.setInt(4, image.getDisplayOrder());

            ps.executeUpdate();
            logger.info("Product image successfully registered with Image ID: {} for Product ID: {} at display order: {}",
                    image.getImageId(), image.getProductId(), image.getDisplayOrder());
        } catch (SQLException e) {
            logger.error("Failed to insert product image for Product ID: {}", image.getProductId(), e);
            if (SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION.equals(e.getSQLState()) ||
                    e.getErrorCode() == MYSQL_ERR_DUPLICATE_KEY) {
                throw new DuplicateEntityException("Image ID already exists: " + image.getImageId(), e);
            }
            throw new DAOException("Error occurred while saving product image", e);
        }
    }

    /**
     * Retrieves a single product image by its unique identifier.
     *
     * @param conn    Active database connection
     * @param imageId Unique identifier of the target image
     * @return An Optional containing the populated bean, or empty if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Image ID is null or empty
     */
    public Optional<ProductImageBean> findById(Connection conn, String imageId) throws DAOException {
        if (imageId == null || imageId.trim().isEmpty()) {
            throw new IllegalArgumentException("Image ID cannot be null or empty for lookup");
        }

        String sql = SELECT_BASE + "WHERE image_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, imageId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching image with Image ID: {}", imageId, e);
            throw new DAOException("Error fetching image by Image ID", e);
        }
        return Optional.empty();
    }

    /**
     * Retrieves all images registered for a specific product.
     *
     * @param conn      Active database connection
     * @param productId Unique identifier of the product
     * @return List of associated product images
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Product ID is null or empty
     */
    public List<ProductImageBean> findAllByProductId(Connection conn, String productId) throws DAOException {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty for images lookup");
        }

        String sql = SELECT_BASE + "WHERE product_id = ?";
        List<ProductImageBean> images = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    images.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving images for Product ID: {}", productId, e);
            throw new DAOException("Error retrieving images by Product ID", e);
        }
        return images;
    }

    /**
     * Updates an existing product image configuration.
     *
     * @param conn    Active database connection
     * @param imageId Unique identifier of the image record to update
     * @param image   ProductImage bean containing new values
     * @return true if the row was successfully updated; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Image ID is null or empty, if the image object is null,
     * or if parameters inside the bean are invalid
     */
    public boolean update(Connection conn, String imageId, ProductImageBean image) throws DAOException {
        if (imageId == null || imageId.trim().isEmpty()) {
            throw new IllegalArgumentException("Image ID cannot be null or empty for updates");
        }
        if (image == null) {
            throw new IllegalArgumentException("Cannot update with a null ProductImage object");
        }
        if (image.getProductId() == null || image.getProductId().trim().isEmpty() ||
                image.getImagePath() == null || image.getImagePath().trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID and Image Path must be valid and populated for updates");
        }

        String sql = "UPDATE product_image SET product_id = ?, image_path = ? WHERE image_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, image.getProductId().trim());
            ps.setString(2, image.getImagePath().trim());
            ps.setString(3, imageId.trim());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Product image with Image ID: {} successfully updated", imageId);
                return true;
            } else {
                logger.warn("Update issued for non-existent Image ID: {}", imageId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update product image with Image ID: {}", imageId, e);
            throw new DAOException("Error updating product image", e);
        }
        return false;
    }

    /**
     * Updates an existing product image configuration using its domain model representation.
     *
     * @param conn  Active database connection
     * @param image ProductImage bean containing updated parameters, including its unique Image ID
     * @return true if the row was successfully updated; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the image object is null, or if its Image ID is missing
     */
    public boolean update(Connection conn, ProductImageBean image) throws DAOException {
        if (image == null || image.getImageId() == null) {
            throw new IllegalArgumentException("Attempted to update a null ProductImage or " +
                    "an image without an Image ID");
        }
        return update(conn, image.getImageId(), image);
    }

    /**
     * Deletes a single product image by its unique identifier.
     *
     * @param conn    Active database connection
     * @param imageId Unique identifier of the image to remove
     * @return true if the record was successfully deleted; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Image ID is null or empty
     */
    public boolean delete(Connection conn, String imageId) throws DAOException {
        if (imageId == null || imageId.trim().isEmpty()) {
            throw new IllegalArgumentException("Image ID cannot be null or empty for deletion");
        }

        String sql = "DELETE FROM product_image WHERE image_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, imageId.trim());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Successfully deleted product image with Image ID: {}", imageId);
                return true;
            } else {
                logger.warn("Delete issued for non-existent Image ID: {}", imageId);
            }
        } catch (SQLException e) {
            logger.error("Failed to delete product image with Image ID: {}", imageId, e);
            throw new DAOException("Error deleting product image", e);
        }
        return false;
    }

    /**
     * Deletes a single product image using its domain model representation.
     *
     * @param conn  Active database connection
     * @param image ProductImage bean containing the unique Image ID to delete
     * @return true if the record was successfully deleted; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the image object is null, or if its Image ID is missing
     */
    public boolean delete(Connection conn, ProductImageBean image) throws DAOException {
        if (image == null || image.getImageId() == null) {
            throw new IllegalArgumentException("Attempted to delete a null ProductImage or " +
                    "an image without an Image ID");
        }
        return delete(conn, image.getImageId());
    }

    /**
     * Deletes all images registered under a specific product ID.
     *
     * @param conn      Active database connection
     * @param productId Unique identifier of the parent product
     * @return true if associations were deleted; false if none existed
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Product ID is null or empty
     */
    public boolean deleteAllByProductId(Connection conn, String productId) throws DAOException {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty for clearing images");
        }

        String sql = "DELETE FROM product_image WHERE product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId.trim());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Successfully cleared all images for Product ID: {}", productId);
                return true;
            } else {
                logger.warn("Clear images issued for Product ID: {} but no records were found", productId);
            }
        } catch (SQLException e) {
            logger.error("Failed to clear images for Product ID: {}", productId, e);
            throw new DAOException("Error clearing images for Product ID", e);
        }
        return false;
    }

    /**
     * Maps a database row from a {@link ResultSet} into a {@link ProductImageBean}.
     */
    private ProductImageBean mapRow(ResultSet rs) throws SQLException {
        ProductImageBean image = new ProductImageBean();
        image.setImageId(rs.getString("image_id"));
        image.setProductId(rs.getString("product_id"));
        image.setImagePath(rs.getString("image_path"));
        return image;
    }
}
