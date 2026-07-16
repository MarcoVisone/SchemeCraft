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
import com.xyra.schemecraft.model.CategoryBean;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

/**
 * Data Access Object (DAO) for managing persistent {@link CategoryBean} entities.
 */
public class CategoryDAO extends BaseDAO {

    private static final String SELECT_BASE = "SELECT category_id, category_name, parent_category_id, " +
            "description FROM category ";

    /**
     * Inserts a new Category record into the database.
     *
     * @param conn     Active database connection
     * @param category The Category bean to persist
     * @throws DuplicateEntityException if the Category ID or Category Name already exists
     * @throws DAOException             if a generic database error occurs
     * @throws IllegalArgumentException if the category is null or lacks a valid Category ID or Category Name
     */
    public void insert(Connection conn, CategoryBean category) throws DAOException {
        if (category == null) {
            throw new IllegalArgumentException("Cannot insert a null Category");
        }
        if (category.getCategoryId() == null || category.getCategoryId().trim().isEmpty() ||
                category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category ID and Category Name must be valid and populated");
        }

        String sql = "INSERT INTO category (category_id, category_name, parent_category_id, description) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getCategoryId());
            ps.setString(2, category.getCategoryName());
            ps.setString(3, category.getParentCategoryId());
            ps.setString(4, category.getDescription());

            ps.executeUpdate();
            logger.info("Category successfully created: {} (ID: {})", category.getCategoryName(),
                    category.getCategoryId());
        } catch (SQLException e) {
            logger.error("Failed to insert category with Category Name: {}", category.getCategoryName(), e);
            if (SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION.equals(e.getSQLState()) ||
                    e.getErrorCode() == MYSQL_ERR_DUPLICATE_KEY) {
                throw new DuplicateEntityException("Category ID or Category Name already exists", e);
            }
            throw new DAOException("Error occurred while inserting category", e);
        }
    }

    /**
     * Finds a Category by its unique identifier.
     *
     * @param conn       Active database connection
     * @param categoryId Unique identifier of the target category
     * @return An Optional containing the populated bean, or empty if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the categoryId is null or empty
     */
    public Optional<CategoryBean> findById(Connection conn, String categoryId) throws DAOException {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Category ID cannot be null or empty for lookup");
        }

        String sql = SELECT_BASE + "WHERE category_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching category with Category ID: {}", categoryId, e);
            throw new DAOException("Error fetching category by ID", e);
        }
        return Optional.empty();
    }

    /**
     * Finds a Category by its unique name.
     *
     * @param conn         Active database connection
     * @param categoryName Unique name of the target category
     * @return An Optional containing the populated bean, or empty if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the categoryName is null or empty
     */
    public Optional<CategoryBean> findByName(Connection conn, String categoryName) throws DAOException {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("Category Name cannot be null or empty for lookup");
        }

        String sql = SELECT_BASE + "WHERE category_name = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoryName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching category with Category Name: {}", categoryName, e);
            throw new DAOException("Error fetching category by Name", e);
        }
        return Optional.empty();
    }

    /**
     * Retrieves all root categories.
     *
     * @param conn Active database connection
     * @return List of root categories, ordered alphabetically by name
     * @throws DAOException if a database error occurs
     */
    public List<CategoryBean> findRootCategories(Connection conn) throws DAOException {
        String sql = SELECT_BASE + "WHERE parent_category_id IS NULL ORDER BY category_name";
        List<CategoryBean> categories = new ArrayList<>();

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

    /**
     * Retrieves all direct subcategories belonging to a given parent category.
     *
     * @param conn               Active database connection
     * @param parentCategoryId Unique identifier of the parent category
     * @return List of subcategories, ordered alphabetically by name
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the parentCategoryId is null or empty
     */
    public List<CategoryBean> findSubCategories(Connection conn, String parentCategoryId) throws DAOException {
        if (parentCategoryId == null || parentCategoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Parent Category ID cannot be null or empty for subcategory lookup");
        }

        String sql = SELECT_BASE + "WHERE parent_category_id = ? ORDER BY category_name";
        List<CategoryBean> categories = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, parentCategoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving subcategories for Parent Category ID: {}",
                    parentCategoryId, e);
            throw new DAOException("Error retrieving subcategories", e);
        }
        return categories;
    }

    /**
     * Retrieves all categories, ordered alphabetically by name.
     *
     * @param conn Active database connection
     * @return List of all categories
     * @throws DAOException if a database error occurs
     */
    public List<CategoryBean> findAll(Connection conn) throws DAOException {
        String sql = SELECT_BASE + "ORDER BY category_name";
        List<CategoryBean> categories = new ArrayList<>();

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

    /**
     * Updates an existing Category with new property values using its unique ID.
     *
     * @param conn       Active database connection
     * @param categoryId Unique identifier of the category to be updated
     * @param category   The model containing the updated details
     * @return true if the row was successfully updated; false otherwise
     * @throws DuplicateEntityException if the update violates unique index constraints (category name)
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the categoryId is null or empty, or if the category is null
     */
    public boolean update(Connection conn, String categoryId, CategoryBean category) throws DAOException {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Category ID cannot be null or empty for updates");
        }
        if (category == null) {
            throw new IllegalArgumentException("Cannot update with a null Category object");
        }

        String sql = "UPDATE category SET category_name = ?, parent_category_id = ?, description = ? " +
                "WHERE category_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getCategoryName());
            ps.setString(2, category.getParentCategoryId());
            ps.setString(3, category.getDescription());
            ps.setString(4, categoryId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Category with ID: {} successfully updated", categoryId);
                return true;
            } else {
                logger.warn("Update issued for non-existent category ID: {}", categoryId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update category with Category ID: {}", categoryId, e);
            if (SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION.equals(e.getSQLState()) ||
                    e.getErrorCode() == MYSQL_ERR_DUPLICATE_KEY) {
                throw new DuplicateEntityException("Category name already exists for update: " +
                        category.getCategoryName(), e);
            }
            throw new DAOException("Error updating category", e);
        }
        return false;
    }

    /**
     * Updates an existing Category with new property values using its domain model representation.
     *
     * @param conn     Active database connection
     * @param category The model containing the updated details
     * @return true if the row was successfully updated; false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the category is null or does not have a valid ID
     */
    public boolean update(Connection conn, CategoryBean category) throws DAOException {
        if (category == null || category.getCategoryId() == null) {
            throw new IllegalArgumentException("Attempted to update a null category or a category without an ID");
        }
        return update(conn, category.getCategoryId(), category);
    }

    /**
     * Deletes a category record from the database using its unique ID.
     *
     * @param conn       Active database connection
     * @param categoryId Unique identifier of the category to delete
     * @return true if the category was deleted; false if the ID was not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the categoryId is null or empty
     */
    public boolean delete(Connection conn, String categoryId) throws DAOException {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Category ID cannot be null or empty for deletion");
        }

        String sql = "DELETE FROM category WHERE category_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoryId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Category with ID: {} successfully deleted", categoryId);
                return true;
            } else {
                logger.warn("Delete issued for non-existent category ID: {}", categoryId);
            }
        } catch (SQLException e) {
            logger.error("Failed to delete category with Category ID: {}", categoryId, e);
            throw new DAOException("Error deleting category", e);
        }
        return false;
    }

    /**
     * Deletes a category record from the database using its domain model representation.
     *
     * @param conn     Active database connection
     * @param category The model containing the target category's identifier
     * @return true if the category was deleted; false if the ID was not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the category is null or does not have a valid ID
     */
    public boolean delete(Connection conn, CategoryBean category) throws DAOException {
        if (category == null || category.getCategoryId() == null) {
            throw new IllegalArgumentException("Attempted to delete a null category or a category without an ID");
        }
        return delete(conn, category.getCategoryId());
    }

    /**
     * Maps a database row from a {@link ResultSet} into a {@link CategoryBean}.
     */
    private CategoryBean mapRow(ResultSet rs) throws SQLException {
        CategoryBean category = new CategoryBean();
        category.setCategoryId(rs.getString("category_id"));
        category.setCategoryName(rs.getString("category_name"));
        category.setParentCategoryId(rs.getString("parent_category_id"));
        category.setDescription(rs.getString("description"));
        return category;
    }
}
