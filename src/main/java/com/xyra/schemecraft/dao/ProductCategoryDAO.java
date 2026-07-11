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
import com.xyra.schemecraft.model.ProductCategoryBean;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

public class ProductCategoryDAO extends BaseDAO {
    private static final String SELECT_BASE = "SELECT product_id, category_id FROM product_category";

    public void insert(Connection conn, ProductCategoryBean association) throws DAOException {
        if (association == null) {
            throw new IllegalArgumentException("Cannot insert a null ProductCategory association");
        }

        String sql = "INSERT INTO product_category (product_id, category_id) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, association.getProductId());
            ps.setString(2, association.getCategoryId());

            ps.executeUpdate();
            logger.info("Product ID: {} successfully linked to Category ID: {}",
                    association.getProductId(), association.getCategoryId());
        } catch (SQLException e) {
            logger.error("Failed to link product {} with category {}", association.getProductId(),
                    association.getCategoryId(), e);
            if (MYSQL_DUPLICATE_KEY_STATE.equals(e.getSQLState()) || e.getErrorCode() == MYSQL_DUPLICATE_KEY_CODE) {
                throw new DuplicateEntityException("Product is already assigned to this category", e);
            }
            throw new DAOException("Error occurred while linking product to category", e);
        }
    }

    public Optional<ProductCategoryBean> findById(Connection conn, String productId, String categoryId)
            throws DAOException {
        String sql = SELECT_BASE + " WHERE product_id = ? AND category_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            ps.setString(2, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while checking link between product {} and category {}",
                    productId, categoryId, e);
            throw new DAOException("Error fetching product-category association", e);
        }
        return Optional.empty();
    }

    public List<ProductCategoryBean> findAllByProductId(Connection conn, String productId) throws DAOException {
        String sql = SELECT_BASE + " WHERE product_id = ?";
        List<ProductCategoryBean> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving categories for product ID: {}", productId, e);
            throw new DAOException("Error retrieving categories by product ID", e);
        }
        return list;
    }

    public List<ProductCategoryBean> findAllByCategoryId(Connection conn, String categoryId) throws DAOException {
        String sql = SELECT_BASE + " WHERE category_id = ?";
        List<ProductCategoryBean> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving products for category ID: {}", categoryId, e);
            throw new DAOException("Error retrieving products by category ID", e);
        }
        return list;
    }

    public boolean delete(Connection conn, String productId, String categoryId) throws DAOException {
        String sql = "DELETE FROM product_category WHERE product_id = ? AND category_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            ps.setString(2, categoryId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Failed to remove product {} from category {}", productId, categoryId, e);
            throw new DAOException("Error removing product from category", e);
        }
    }

    public boolean delete(Connection conn, ProductCategoryBean association) throws DAOException {
        if (association == null || association.getProductId() == null || association.getCategoryId() == null) {
            throw new IllegalArgumentException("Attempted to delete a null association " +
                    "or an object with missing composite keys");
        }
        return delete(conn, association.getProductId(), association.getCategoryId());
    }

    public boolean deleteAllByProductId(Connection conn, String productId) throws DAOException {
        String sql = "DELETE FROM product_category WHERE product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Failed to wipe category associations for product ID: {}", productId, e);
            throw new DAOException("Error clearing categories for product", e);
        }
    }

    private ProductCategoryBean mapRow(ResultSet rs) throws SQLException {
        ProductCategoryBean pc = new ProductCategoryBean();
        pc.setProductId(rs.getString("product_id"));
        pc.setCategoryId(rs.getString("category_id"));
        return pc;
    }
}
