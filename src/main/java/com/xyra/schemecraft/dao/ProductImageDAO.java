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

public class ProductImageDAO extends BaseDAO {
    private static final String SELECT_BASE = "SELECT image_id, product_id, image_path FROM product_image";

    public void insert(Connection conn, ProductImageBean image) throws DAOException {
        if (image == null) {
            throw new IllegalArgumentException("Cannot insert a null ProductImage");
        }

        String sql = "INSERT INTO product_image (image_id, product_id, image_path) VALUES (?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, image.getImageId());
            ps.setString(2, image.getProductId());
            ps.setString(3, image.getImagePath());

            ps.executeUpdate();
            logger.info("Product image successfully registered with ID: {} for Product: {}", image.getImageId(),
                    image.getProductId());
        } catch (SQLException e) {
            logger.error("Failed to insert product image for product ID: {}", image.getProductId(), e);
            if (MYSQL_DUPLICATE_KEY_STATE.equals(e.getSQLState()) || e.getErrorCode() == MYSQL_DUPLICATE_KEY_CODE) {
                throw new DuplicateEntityException("Image ID already exists: " + image.getImageId(), e);
            }
            throw new DAOException("Error occurred while saving product image", e);
        }
    }

    public Optional<ProductImageBean> findById(Connection conn, String imageId) throws DAOException {
        String sql = SELECT_BASE + " WHERE image_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, imageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching image with ID: {}", imageId, e);
            throw new DAOException("Error fetching image by ID", e);
        }
        return Optional.empty();
    }

    public List<ProductImageBean> findAllByProductId(Connection conn, String productId) throws DAOException {
        String sql = SELECT_BASE + " WHERE product_id = ?";
        List<ProductImageBean> images = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    images.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving images for product ID: {}", productId, e);
            throw new DAOException("Error retrieving images by product ID", e);
        }
        return images;
    }

    public boolean update(Connection conn, String imageId, ProductImageBean image) throws DAOException {
        if (image == null) {
            throw new IllegalArgumentException("Cannot update with a null ProductImage object");
        }

        String sql = "UPDATE product_image SET product_id = ?, image_path = ? WHERE image_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, image.getProductId());
            ps.setString(2, image.getImagePath());
            ps.setString(3, imageId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Product image with ID: {} successfully updated", imageId);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Failed to update product image with ID: {}", imageId, e);
            throw new DAOException("Error updating product image", e);
        }
        return false;
    }

    public boolean update(Connection conn, ProductImageBean image) throws DAOException {
        if (image == null || image.getImageId() == null) {
            throw new IllegalArgumentException("Attempted to update a null image or an image without an ID");
        }
        return update(conn, image.getImageId(), image);
    }

    public boolean delete(Connection conn, String imageId) throws DAOException {
        String sql = "DELETE FROM product_image WHERE image_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, imageId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Failed to delete product image with ID: {}", imageId, e);
            throw new DAOException("Error deleting product image", e);
        }
    }

    public boolean delete(Connection conn, ProductImageBean image) throws DAOException {
        if (image == null || image.getImageId() == null) {
            throw new IllegalArgumentException("Attempted to delete a null image or an image without an ID");
        }
        return delete(conn, image.getImageId());
    }

    public boolean deleteAllByProductId(Connection conn, String productId) throws DAOException {
        String sql = "DELETE FROM product_image WHERE product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Failed to clear images for product ID: {}", productId, e);
            throw new DAOException("Error clearing images for product", e);
        }
    }

    private ProductImageBean mapRow(ResultSet rs) throws SQLException {
        ProductImageBean image = new ProductImageBean();
        image.setImageId(rs.getString("image_id"));
        image.setProductId(rs.getString("product_id"));
        image.setImagePath(rs.getString("image_path"));
        return image;
    }
}
