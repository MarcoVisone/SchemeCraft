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

/**
 * Data Access Object (DAO) for managing persistent {@link ProductCategoryBean} entities.
 */
public class ProductCategoryDAO extends BaseDAO {

    private static final String SELECT_BASE = "SELECT product_id, category_id FROM product_category ";

    /**
     * Links a product to a category.
     *
     * @param conn        Active database connection
     * @param association The ProductCategory bean detailing the association
     * @throws DuplicateEntityException if the product is already linked to the category
     * @throws DAOException             if a generic database error occurs
     * @throws IllegalArgumentException if the association is null, or if Product ID or Category ID are null or empty
     */
    public void insert(Connection conn, ProductCategoryBean association) throws DAOException {
        if (association == null) {
            throw new IllegalArgumentException("Cannot insert a null ProductCategory association");
        }
        if (association.getProductId() == null || association.getProductId().trim().isEmpty() ||
                association.getCategoryId() == null || association.getCategoryId().trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID and Category ID must be valid and populated");
        }

        String productId = association.getProductId().trim();
        String categoryId = association.getCategoryId().trim();

        String sql = "WITH RECURSIVE category_tree AS (" +
                "    SELECT category_id, parent_category_id FROM category WHERE category_id = ? " +
                "    UNION ALL " +
                "    SELECT c.category_id, c.parent_category_id " +
                "    FROM category c " +
                "    JOIN category_tree ct ON c.category_id = ct.parent_category_id " +
                ") " +
                "INSERT IGNORE INTO product_category (product_id, category_id) " +
                "SELECT ?, category_id FROM category_tree";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoryId);
            ps.setString(2, productId);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                if (findById(conn, productId, categoryId).isPresent()) {
                    throw new DuplicateEntityException("Product is already assigned to this category");
                }
                throw new DAOException("Failed to link product to category: Specified Category ID does not exist");
            }

            logger.info("Product ID: {} successfully linked to Category ID: {} and its parent chain (Total links inserted: {})",
                    productId, categoryId, rowsAffected);

        } catch (SQLException e) {
            logger.error("Failed to link Product ID: {} with Category ID: {}", productId, categoryId, e);
            if (SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION.equals(e.getSQLState()) ||
                    e.getErrorCode() == MYSQL_ERR_DUPLICATE_KEY) {
                throw new DuplicateEntityException("Product is already assigned to this category", e);
            }
            throw new DAOException("Error occurred while linking product to category hierarchy", e);
        }
    }

    /**
     * Checks if a specific association between a product and a category exists.
     *
     * @param conn       Active database connection
     * @param productId  Unique identifier of the product
     * @param categoryId Unique identifier of the category
     * @return An Optional containing the populated association bean, or empty if not linked
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Product ID or Category ID is null or empty
     */
    public Optional<ProductCategoryBean> findById(Connection conn, String productId, String categoryId)
            throws DAOException {
        if (productId == null || productId.trim().isEmpty() || categoryId == null || categoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID and Category ID cannot be null or empty for lookup");
        }

        String sql = SELECT_BASE + "WHERE product_id = ? AND category_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId.trim());
            ps.setString(2, categoryId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while checking link between Product ID: {} and Category ID: {}",
                    productId, categoryId, e);
            throw new DAOException("Error fetching product-category association", e);
        }
        return Optional.empty();
    }

    /**
     * Retrieves all category associations configured for a specific product.
     *
     * @param conn      Active database connection
     * @param productId Unique identifier of the product
     * @return List of product-category association beans
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Product ID is null or empty
     */
    public List<ProductCategoryBean> findAllByProductId(Connection conn, String productId) throws DAOException {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty for category lookup");
        }

        String sql = SELECT_BASE + "WHERE product_id = ?";
        List<ProductCategoryBean> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving categories for Product ID: {}", productId, e);
            throw new DAOException("Error retrieving categories by Product ID", e);
        }
        return list;
    }

    /**
     * Retrieves all product associations configured for a specific category.
     *
     * @param conn       Active database connection
     * @param categoryId Unique identifier of the category
     * @return List of product-category association beans
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Category ID is null or empty
     */
    public List<ProductCategoryBean> findAllByCategoryId(Connection conn, String categoryId) throws DAOException {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Category ID cannot be null or empty for product lookup");
        }

        String sql = SELECT_BASE + "WHERE category_id = ?";
        List<ProductCategoryBean> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoryId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving products for Category ID: {}", categoryId, e);
            throw new DAOException("Error retrieving products by Category ID", e);
        }
        return list;
    }

    /**
     * Deletes a specific product-category link.
     *
     * @param conn       Active database connection
     * @param productId  Unique identifier of the product
     * @param categoryId Unique identifier of the category
     * @return true if the linkage was successfully removed; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Product ID or Category ID is null or empty
     */
    public boolean delete(Connection conn, String productId, String categoryId) throws DAOException {
        if (productId == null || productId.trim().isEmpty() || categoryId == null || categoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID and Category ID cannot be null or empty for deletion");
        }

        String sql = "DELETE FROM product_category WHERE product_id = ? AND category_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId.trim());
            ps.setString(2, categoryId.trim());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Successfully removed Product ID: {} from Category ID: {}", productId, categoryId);
                return true;
            } else {
                logger.warn("Delete issued for non-existent association between Product ID: {} and Category ID: {}",
                        productId, categoryId);
            }
        } catch (SQLException e) {
            logger.error("Failed to remove Product ID: {} from Category ID: {}", productId, categoryId, e);
            throw new DAOException("Error removing product from category", e);
        }
        return false;
    }

    /**
     * Deletes a specific product-category link using its domain model representation.
     *
     * @param conn        Active database connection
     * @param association The ProductCategory bean detailing the association to remove
     * @return true if the linkage was successfully removed; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the association is null, or if Product ID or Category ID are missing
     */
    public boolean delete(Connection conn, ProductCategoryBean association) throws DAOException {
        if (association == null) {
            throw new IllegalArgumentException("Cannot delete a null ProductCategory association");
        }
        if (association.getProductId() == null || association.getCategoryId() == null) {
            throw new IllegalArgumentException("Attempted to delete an association with missing composite keys");
        }
        return delete(conn, association.getProductId(), association.getCategoryId());
    }

    /**
     * Clears all category associations assigned to a given product.
     *
     * @param conn      Active database connection
     * @param productId Unique identifier of the product
     * @return true if associations were removed; false if none existed
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Product ID is null or empty
     */
    public boolean deleteAllByProductId(Connection conn, String productId) throws DAOException {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty for clearing associations");
        }

        String sql = "DELETE FROM product_category WHERE product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId.trim());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Successfully cleared all category associations for Product ID: {}", productId);
                return true;
            } else {
                logger.warn("Clear associations issued for Product ID: {} but no records were found", productId);
            }
        } catch (SQLException e) {
            logger.error("Failed to wipe category associations for Product ID: {}", productId, e);
            throw new DAOException("Error clearing categories for product", e);
        }
        return false;
    }

    /**
     * Maps a database row from a {@link ResultSet} into a {@link ProductCategoryBean}.
     */
    private ProductCategoryBean mapRow(ResultSet rs) throws SQLException {
        ProductCategoryBean pc = new ProductCategoryBean();
        pc.setProductId(rs.getString("product_id"));
        pc.setCategoryId(rs.getString("category_id"));
        return pc;
    }
}
