package com.xyra.schemecraft.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.xyra.schemecraft.dao.ProductCategoryDAO;
import com.xyra.schemecraft.model.ProductCategoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyra.schemecraft.connection.ConnectionPool;
import com.xyra.schemecraft.dao.CategoryDAO;
import com.xyra.schemecraft.dto.CategoryRequest;
import com.xyra.schemecraft.exception.DAOException;
import com.xyra.schemecraft.exception.ServiceException;
import com.xyra.schemecraft.model.CategoryBean;

public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    private static final int MAX_HIERARCHY_DEPTH = 50;

    private final ProductCategoryDAO productCategoryDAO;
    private final CategoryDAO categoryDAO;
    private final EntityValidator entityValidator;

    public CategoryService() {
        this.categoryDAO = new CategoryDAO();
        this.entityValidator = new EntityValidator();
        this.productCategoryDAO = new ProductCategoryDAO();
    }

    public CategoryBean createCategory(CategoryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request cannot be null");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            if (request.parentCategoryId() != null) {
                entityValidator.validateActiveCategory(conn, request.parentCategoryId());
            }

            String categoryId = UUID.randomUUID().toString();
            CategoryBean category = new CategoryBean(categoryId, request.categoryName(),
                    request.parentCategoryId(), request.description());

            categoryDAO.insert(conn, category);
            logger.info("Category {} created with name '{}'", categoryId, request.categoryName());

            return category;
        } catch (DAOException | SQLException e) {
            logger.error("Database connection error while creating category", e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    public void updateCategory(CategoryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request cannot be null");
        }
        if (request.categoryId() == null || request.categoryId().isBlank()) {
            throw new IllegalArgumentException("categoryId cannot be null or blank for category updates");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            entityValidator.validateActiveCategory(conn, request.categoryId());

            if (request.parentCategoryId() != null) {
                if (request.parentCategoryId().equals(request.categoryId())) {
                    throw new IllegalArgumentException("A category cannot be its own parent");
                }

                entityValidator.validateActiveCategory(conn, request.parentCategoryId());

                if (wouldCreateCycle(conn, request.categoryId(), request.parentCategoryId())) {
                    throw new IllegalArgumentException(
                            "Cannot assign parent category: this would create a cyclic hierarchy");
                }
            }

            CategoryBean category = new CategoryBean(request.categoryId(), request.categoryName(),
                    request.parentCategoryId(), request.description());

            categoryDAO.update(conn, request.categoryId(), category);
            logger.info("Category {} updated", request.categoryId());

        } catch (DAOException | SQLException e) {
            logger.error("Database connection error while updating category", e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    private boolean wouldCreateCycle(Connection conn, String categoryId, String proposedParentId) throws SQLException {
        String currentId = proposedParentId;
        int hops = 0;

        while (currentId != null) {
            if (currentId.equals(categoryId)) {
                return true;
            }

            if (++hops > MAX_HIERARCHY_DEPTH) {
                logger.warn("Category hierarchy depth exceeded {} hops while checking for cycles starting at {}; "
                        + "aborting cycle check, possible pre-existing data corruption", MAX_HIERARCHY_DEPTH, proposedParentId);
                return true;
            }

            Optional<CategoryBean> current = categoryDAO.findById(conn, currentId);
            if (current.isEmpty()) {
                return false;
            }
            currentId = current.get().getParentCategoryId();
        }

        return false;
    }

    public void deleteCategory(String rawCategoryId) {
        String categoryId = rawCategoryId == null ? null : rawCategoryId.trim();

        if (categoryId == null || categoryId.isBlank()) {
            throw new IllegalArgumentException("categoryId cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            entityValidator.validateActiveCategory(conn, categoryId);
            categoryDAO.delete(conn, categoryId);
            logger.info("Category {} deleted", categoryId);
        } catch (DAOException | SQLException e) {
            logger.error("Database connection error while deleting category", e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    public CategoryBean getCategoryById(String rawCategoryId) {
        String categoryId = rawCategoryId == null ? null : rawCategoryId.trim();

        if (categoryId == null || categoryId.isBlank()) {
            throw new IllegalArgumentException("categoryId cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            return entityValidator.validateActiveCategory(conn, categoryId);
        } catch (SQLException e) {
            logger.error("Database connection error while fetching category {}", categoryId, e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    public List<CategoryBean> listRootCategories() {
        try (Connection conn = ConnectionPool.getConnection()) {
            return categoryDAO.findRootCategories(conn);
        } catch (DAOException | SQLException e) {
            logger.error("Database connection error while listing root categories", e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    public List<ProductCategoryBean> listAllCategoriesAssociated(String rawProductId) {
        String productId = rawProductId == null ? null : rawProductId.trim();
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId cannot be null or blank");
        }
        try (Connection conn = ConnectionPool.getConnection()) {
            entityValidator.validateProduct(conn, productId);
            return productCategoryDAO.findAllByProductId(conn, productId);
        } catch (DAOException | SQLException e) {
            logger.error("Database connection error while listing categories for product {}", productId, e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    public List<CategoryBean> listSubCategories(String rawParentCategoryId) {
        String parentCategoryId = rawParentCategoryId == null ? null : rawParentCategoryId.trim();

        if (parentCategoryId == null || parentCategoryId.isBlank()) {
            throw new IllegalArgumentException("parentCategoryId cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            entityValidator.validateActiveCategory(conn, parentCategoryId);
            return categoryDAO.findSubCategories(conn, parentCategoryId);
        } catch (DAOException | SQLException e) {
            logger.error("Database connection error while listing subcategories of {}", parentCategoryId, e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    public List<CategoryBean> listAllCategories() {
        try (Connection conn = ConnectionPool.getConnection()) {
            return categoryDAO.findAll(conn);
        } catch (DAOException | SQLException e) {
            logger.error("Database connection error while listing all categories", e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }
}