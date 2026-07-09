package com.xyra.schemecraft.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyra.schemecraft.connection.ConnectionPool;
import com.xyra.schemecraft.model.Product;


public class ProductDAO {
    private static final Logger logger = LoggerFactory.getLogger(ProductDAO.class);

    public void save(Product product) {
        String sql = "INSERT INTO product (product_id, account_id, currency_id, product_name, " +
                "discount, description, price, stock_quantity, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, product.getProductId());
            ps.setString(2, product.getAccountId());
            ps.setString(3, product.getCurrencyId());
            ps.setString(4, product.getProductName());
            ps.setBigDecimal(5, product.getDiscount());
            ps.setString(6, product.getDescription());
            ps.setBigDecimal(7, product.getPrice());

            if (product.getStockQuantity() != null) {
                ps.setInt(8, product.getStockQuantity());
            } else {
                ps.setNull(8, java.sql.Types.INTEGER);
            }

            ps.setBoolean(9, product.isActive());

            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while saving product: {}", product.getProductName(), e);
        }
    }

    public Product findById(String productId) {
        String sql = "SELECT * FROM product WHERE product_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduct(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error occurred while searching for product with ID: {}", productId, e);
        }
        return null;
    }

    public List<Product> searchProducts(String keyword) {
        String sql = "SELECT * FROM product WHERE MATCH(product_name, description) " +
                "AGAINST(? IN NATURAL LANGUAGE MODE) AND is_active = TRUE";
        List<Product> products = new ArrayList<>();

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, keyword);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error occurred during full-text search with keyword: {}", keyword, e);
        }
        return products;
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getString("product_id"));
        product.setAccountId(rs.getString("account_id"));
        product.setCurrencyId(rs.getString("currency_id"));
        product.setProductName(rs.getString("product_name"));

        BigDecimal discount = rs.getBigDecimal("discount");
        product.setDiscount(discount != null ? discount : BigDecimal.ZERO);

        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getBigDecimal("price"));

        int stock = rs.getInt("stock_quantity");
        product.setStockQuantity(rs.wasNull() ? null : stock);

        product.setActive(rs.getBoolean("is_active"));

        Timestamp latestUpdateTs = rs.getTimestamp("latest_update");
        if (latestUpdateTs != null) {
            product.setLatestUpdate(latestUpdateTs.toLocalDateTime());
        }

        Timestamp createdAtTs = rs.getTimestamp("created_at");
        if (createdAtTs != null) {
            product.setCreatedAt(createdAtTs.toLocalDateTime());
        }

        return product;
    }
}
