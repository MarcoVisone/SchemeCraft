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
import com.xyra.schemecraft.model.ProductImage;

public class ProductImageDAO {
    private static final Logger logger = LoggerFactory.getLogger(ProductImageDAO.class);

    public void save(ProductImage productImage) {
        String sql = "INSERT INTO product_image (image_id, product_id, image_path) VALUES (?, ?, ?)";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productImage.getImageId());
            ps.setString(2, productImage.getProductId());
            ps.setString(3, productImage.getImagePath());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while saving product image with ID: {}", productImage.getImageId(), e);
        }
    }

    public ProductImage findById(String imageId) {
        String sql = "SELECT * FROM product_image WHERE image_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, imageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProductImage(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error occurred while searching for product image with ID: {}", imageId, e);
        }
        return null;
    }

    public List<ProductImage> findByProductId(String productId) {
        String sql = "SELECT * FROM product_image WHERE product_id = ?";
        List<ProductImage> productImages = new ArrayList<>();

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productImages.add(mapResultSetToProductImage(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error occurred while fetching product images for product ID: {}", productId, e);
        }
        return productImages;
    }

    public List<ProductImage> findAll() {
        String sql = "SELECT * FROM product_image";
        List<ProductImage> productImages = new ArrayList<>();

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                productImages.add(mapResultSetToProductImage(rs));
            }
        } catch (SQLException e) {
            logger.error("Error occurred while fetching all product images", e);
        }
        return productImages;
    }

    public void update(ProductImage productImage) {
        String sql = "UPDATE product_image SET product_id = ?, image_path = ? WHERE image_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productImage.getProductId());
            ps.setString(2, productImage.getImagePath());
            ps.setString(3, productImage.getImageId());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while updating product image with ID: {}", productImage.getImageId(), e);
        }
    }

    public void deleteById(String imageId) {
        String sql = "DELETE FROM product_image WHERE image_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, imageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while deleting product image with ID: {}", imageId, e);
        }
    }

    private ProductImage mapResultSetToProductImage(ResultSet rs) throws SQLException {
        ProductImage productImage = new ProductImage();
        productImage.setImageId(rs.getString("image_id"));
        productImage.setProductId(rs.getString("product_id"));
        productImage.setImagePath(rs.getString("image_path"));
        return productImage;
    }
}
