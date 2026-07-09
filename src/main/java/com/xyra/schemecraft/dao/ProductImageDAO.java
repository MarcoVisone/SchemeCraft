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
        List<ProductImage> images = new ArrayList<>();

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    images.add(mapResultSetToProductImage(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error occurred while fetching images for product ID: {}", productId, e);
        }
        return images;
    }

    private ProductImage mapResultSetToProductImage(ResultSet rs) throws SQLException {
        ProductImage image = new ProductImage();
        image.setImageId(rs.getString("image_id"));
        image.setProductId(rs.getString("product_id"));
        image.setImagePath(rs.getString("image_path"));
        return image;
    }
}
