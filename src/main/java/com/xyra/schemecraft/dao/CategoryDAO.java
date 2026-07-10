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
import com.xyra.schemecraft.model.Category;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

public class CategoryDAO extends BaseDAO {
    private static final String SELECT_BASE = "SELECT category_id, category_name, parent_category_name, " +
            "description FROM category";

    public void insert(Connection conn, Category category) throws DAOException {
        if (category == null) {
            throw new IllegalArgumentException("Cannot insert a null Category");
        }

        String sql = "INSERT INTO category (category_id, category_name, parent_category_name, description) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getCategoryId());
            ps.setString(2, category.getCategoryName());
            ps.setString(3, category.getParentCategoryName());
            ps.setString(4, category.getDescription());

            ps.executeUpdate();
            logger.info("Category successfully created: {} (ID: {})", category.getCategoryName(),
                    category.getCategoryId());
        } catch (SQLException e) {
            logger.error("Failed to insert category with Name: {}", category.getCategoryName(), e);
            if (MYSQL_DUPLICATE_KEY_STATE.equals(e.getSQLState()) || e.getErrorCode() == MYSQL_DUPLICATE_KEY_CODE) {
                throw new DuplicateEntityException("Category ID or Category Name already exists", e);
            }
            throw new DAOException("Error occurred while inserting category", e);
        }
    }

    public Optional<Category> findById(Connection conn, String categoryId) throws DAOException {
        String sql = SELECT_BASE + " WHERE category_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching category with ID: {}", categoryId, e);
            throw new DAOException("Error fetching category by ID", e);
        }
        return Optional.empty();
    }

    public Optional<Category> findByName(Connection conn, String categoryName) throws DAOException {
        String sql = SELECT_BASE + " WHERE category_name = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoryName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching category with Name: {}", categoryName, e);
            throw new DAOException("Error fetching category by Name", e);
        }
        return Optional.empty();
    }

    public List<Category> findRootCategories(Connection conn) throws DAOException {
        String sql = SELECT_BASE + " WHERE parent_category_name IS NULL ORDER BY category_name";
        List<Category> categories = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categories.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving root categories", e);
            throw new DAOException("Error retrieving root categories", e);
        }
        return categories;
    }

    public List<Category> findSubCategories(Connection conn, String parentCategoryName) throws DAOException {
        String sql = SELECT_BASE + " WHERE parent_category_name = ? ORDER BY category_name";
        List<Category> categories = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, parentCategoryName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving subcategories for parent: {}", parentCategoryName, e);
            throw new DAOException("Error retrieving subcategories", e);
        }
        return categories;
    }

    public List<Category> findAll(Connection conn) throws DAOException {
        String sql = SELECT_BASE + " ORDER BY category_name";
        List<Category> categories = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categories.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving all categories", e);
            throw new DAOException("Error retrieving all categories", e);
        }
        return categories;
    }

    public boolean update(Connection conn, String categoryId, Category category) throws DAOException {
        if (category == null) {
            throw new IllegalArgumentException("Cannot update with a null Category object");
        }

        String sql = "UPDATE category SET category_name = ?, parent_category_name = ?, description = ? " +
                "WHERE category_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getCategoryName());
            ps.setString(2, category.getParentCategoryName());
            ps.setString(3, category.getDescription());
            ps.setString(4, categoryId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Category with ID: {} successfully updated", categoryId);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Failed to update category with ID: {}", categoryId, e);
            throw new DAOException("Error updating category", e);
        }
        return false;
    }

    public boolean update(Connection conn, Category category) throws DAOException {
        if (category == null || category.getCategoryId() == null) {
            throw new IllegalArgumentException("Attempted to update a null category or a category without an ID");
        }
        return update(conn, category.getCategoryId(), category);
    }

    public boolean delete(Connection conn, String categoryId) throws DAOException {
        String sql = "DELETE FROM category WHERE category_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoryId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Failed to delete category with ID: {}", categoryId, e);
            throw new DAOException("Error deleting category", e);
        }
    }

    public boolean delete(Connection conn, Category category) throws DAOException {
        if (category == null || category.getCategoryId() == null) {
            throw new IllegalArgumentException("Attempted to delete a null category or a category without an ID");
        }
        return delete(conn, category.getCategoryId());
    }

    private Category mapRow(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setCategoryId(rs.getString("category_id"));
        category.setCategoryName(rs.getString("category_name"));
        category.setParentCategoryName(rs.getString("parent_category_name"));
        category.setDescription(rs.getString("description"));
        return category;
    }
}
