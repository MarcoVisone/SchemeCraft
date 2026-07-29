package com.xyra.schemecraft.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.xyra.schemecraft.dto.ProductSearchCriteria;
import com.xyra.schemecraft.exception.DAOException;
import com.xyra.schemecraft.exception.DuplicateEntityException;
import com.xyra.schemecraft.model.ProductBean;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

/**
 * Data Access Object (DAO) for managing persistent {@link ProductBean} entities.
 */
public class ProductDAO extends BaseDAO {

    private static final String SELECT_BASE = "SELECT product_id, account_id, currency_id, average_rating, " +
            "created_at, discount, description, is_active, latest_update, price, product_name, stock_quantity, " +
            "total_downloads, total_reviews FROM product ";

    /**
     * Persists a new Product entity into the database.
     *
     * @param conn    Active database connection
     * @param product The Product bean to insert
     * @throws DuplicateEntityException if the Product ID already exists
     * @throws DAOException             if a generic database error occurs
     * @throws IllegalArgumentException if the product is null, or if Product ID, Account ID,
     * or Product Name are null or empty
     */
    public void insert(Connection conn, ProductBean product) throws DAOException {
        if (product == null) {
            throw new IllegalArgumentException("Cannot insert a null Product");
        }
        if (product.getProductId() == null || product.getProductId().trim().isEmpty() ||
                product.getAccountId() == null || product.getAccountId().trim().isEmpty() ||
                product.getProductName() == null || product.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID, Account ID, and Product Name must be valid and populated");
        }

        String sql = "INSERT INTO product (product_id, account_id, currency_id, average_rating, discount, " +
                "description, is_active, price, product_name, stock_quantity, total_downloads, total_reviews) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getProductId().trim());
            ps.setString(2, product.getAccountId().trim());
            ps.setString(3, product.getCurrencyId() != null ? product.getCurrencyId().trim() : null);
            ps.setBigDecimal(4, product.getAverageRating());
            ps.setBigDecimal(5, product.getDiscount());
            ps.setString(6, product.getDescription());
            ps.setBoolean(7, product.isActive());
            ps.setBigDecimal(8, product.getPrice());
            ps.setString(9, product.getProductName().trim());

            if (product.getStockQuantity() != null) {
                ps.setInt(10, product.getStockQuantity());
            } else {
                ps.setNull(10, java.sql.Types.INTEGER);
            }

            ps.setInt(11, product.getTotalDownloads());
            ps.setInt(12, product.getTotalReviews());

            ps.executeUpdate();
            logger.info("Product successfully inserted with Product ID: {} by Account ID: {}", product.getProductId(),
                    product.getAccountId());
        } catch (SQLException e) {
            logger.error("Failed to insert Product with Product Name: {}", product.getProductName(), e);
            if (SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION.equals(e.getSQLState()) ||
                    e.getErrorCode() == MYSQL_ERR_DUPLICATE_KEY) {
                throw new DuplicateEntityException("Product ID already exists: " + product.getProductId(), e);
            }
            throw new DAOException("Error occurred while inserting product", e);
        }
    }

    /**
     * Retrieves a single product by its unique identifier.
     *
     * @param conn      Active database connection
     * @param productId Unique identifier of the product
     * @return An Optional containing the populated product, or empty if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Product ID is null or empty
     */
    public Optional<ProductBean> findById(Connection conn, String productId) throws DAOException {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty for lookup");
        }

        String sql = SELECT_BASE + "WHERE product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching Product with Product ID: {}", productId, e);
            throw new DAOException("Error fetching product by Product ID", e);
        }
        return Optional.empty();
    }

    /**
     * Retrieves all products published by a specific vendor account.
     *
     * @param conn      Active database connection
     * @param accountId Unique identifier of the vendor account
     * @return List of products associated with the vendor
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Account ID is null or empty
     */
    public List<ProductBean> findAllByAccountId(Connection conn, String accountId) throws DAOException {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty for lookup");
        }

        String sql = SELECT_BASE + "WHERE account_id = ?";
        List<ProductBean> products = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving products for Account ID: {}", accountId, e);
            throw new DAOException("Error retrieving products by Account ID", e);
        }
        return products;
    }

    /**
     * Performs a dynamic criteria search against the active product catalog.
     * Supporting pagination, sorting constraints, rating, and Full-Text Search.
     *
     * @param conn     Active database connection
     * @param criteria The SearchCriteria details model
     * @return List of matching active products
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the search criteria is null
     */
    public List<ProductBean> searchProducts(Connection conn, ProductSearchCriteria criteria) throws DAOException {
        if (criteria == null) {
            throw new IllegalArgumentException("Search criteria cannot be null");
        }

        StringBuilder sql = new StringBuilder();

        String mcVersion = sanitizeString(criteria.getMinecraftVersion());
        String categoryId = sanitizeString(criteria.getCategoryId());
        String keywords = sanitizeString(criteria.getKeywords());

        boolean hasVersionFilter = mcVersion != null;
        boolean hasCategoryFilter = categoryId != null;

        sql.append("SELECT DISTINCT p.* FROM product p ");

        if (hasVersionFilter) {
            sql.append("JOIN product_version pv ON p.product_id = pv.product_id ");
        }
        if (hasCategoryFilter) {
            sql.append("JOIN product_category pc ON p.product_id = pc.product_id ");
        }

        sql.append("WHERE p.is_active = TRUE");

        List<Object> queryParams = new ArrayList<>();

        if (keywords != null) {
            sql.append(" AND (LOWER(p.product_name) LIKE ? OR LOWER(p.description) LIKE ?)");
            String kwParam = "%" + keywords.toLowerCase() + "%";
            queryParams.add(kwParam);
            queryParams.add(kwParam);
        }

        if (hasCategoryFilter) {
            sql.append(" AND pc.category_id = ?");
            queryParams.add(categoryId);
        }

        if (criteria.getMinPrice() != null) {
            sql.append(" AND p.price >= ?");
            queryParams.add(criteria.getMinPrice());
        }
        if (criteria.getMaxPrice() != null) {
            sql.append(" AND p.price <= ?");
            queryParams.add(criteria.getMaxPrice());
        }

        if (criteria.getMinRating() != null) {
            sql.append(" AND p.average_rating >= ?");
            queryParams.add(criteria.getMinRating());
        }
        if (criteria.getMaxRating() != null) {
            sql.append(" AND p.average_rating <= ?");
            queryParams.add(criteria.getMaxRating());
        }

        if (criteria.getOnlyWithDiscount() != null && criteria.getOnlyWithDiscount()) {
            sql.append(" AND p.discount > 0");
        }

        if (hasVersionFilter) {
            sql.append(" AND pv.minecraft_version LIKE ?");
            queryParams.add("%" + mcVersion + "%");
        }

        String rawOrderBy = criteria.getOrderByColumn();
        String safeOrderBy = "created_at";

        if (rawOrderBy != null && !rawOrderBy.trim().isEmpty()) {
            safeOrderBy = switch (rawOrderBy.toLowerCase().trim()) {
                case "price" -> "price";
                case "average_rating" -> "average_rating";
                case "total_reviews" -> "total_reviews";
                case "total_downloads" -> "total_downloads";
                case "product_name" -> "product_name";
                case "created_at" -> "created_at";
                default -> "created_at";
            };
        }

        String direction = (criteria.getAscending() != null && criteria.getAscending()) ? "ASC" : "DESC";
        sql.append(" ORDER BY p.").append(safeOrderBy).append(" ").append(direction);

        int pageNumber = (criteria.getPageNumber() == 0 || criteria.getPageNumber() < 1) ? 1 : criteria.getPageNumber();

        int limit = (criteria.getPageSize() == 0 || criteria.getPageSize() < 1) ? 10 : criteria.getPageSize();
        int offset = (pageNumber - 1) * limit;

        sql.append(" LIMIT ? OFFSET ?");

        List<ProductBean> products = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;

            for (Object param : queryParams) {
                ps.setObject(paramIndex++, param);
            }

            ps.setInt(paramIndex++, limit);
            ps.setInt(paramIndex, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error during dynamic product search. Query: {}", sql, e);
            throw new DAOException("Error performing dynamic product search", e);
        }

        logger.info("Found products: {}", products.size());
        return products;
    }


    /**
     * Retrieves a lightweight list of active products matching the given keyword,
     * intended for search-bar autocomplete suggestions. Matches products whose name
     * starts with the keyword, or whose description contains it, returning only the
     * minimal fields needed for a suggestion preview. Name matches are ranked above
     * description-only matches, since a match on the product name is more relevant
     * to the user than one buried in the description text.
     *
     * @param conn    Active database connection
     * @param keyword Search term to match against the product name and description
     *                (already trimmed/validated by the caller)
     * @param limit   Maximum number of suggestions to return
     * @return List of matching active products, ranked by relevance, containing only id/name/description
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if keyword is null/empty or limit is not positive
     */
    public List<ProductBean> suggestByKeyword(Connection conn, String keyword, int limit) throws DAOException {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Keyword cannot be null or empty for suggestions lookup");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive for suggestions lookup");
        }

        String sql = "SELECT product_id, product_name, SUBSTRING(description, 1, 80) AS description " +
                "FROM product " +
                "WHERE is_active = TRUE AND (LOWER(product_name) LIKE ? OR LOWER(description) LIKE ?) " +
                "ORDER BY (LOWER(product_name) LIKE ?) DESC, product_name ASC " +
                "LIMIT ?";

        String normalizedKeyword = keyword.trim().toLowerCase();
        String prefixPattern = normalizedKeyword + "%";
        String containsPattern = "%" + normalizedKeyword + "%";

        List<ProductBean> suggestions = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefixPattern);
            ps.setString(2, containsPattern);
            ps.setString(3, prefixPattern);
            ps.setInt(4, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductBean product = new ProductBean();
                    product.setProductId(rs.getString("product_id"));
                    product.setProductName(rs.getString("product_name"));
                    product.setDescription(rs.getString("description"));
                    suggestions.add(product);
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching product suggestions for keyword: {}", keyword, e);
            throw new DAOException("Error fetching product suggestions", e);
        }

        return suggestions;
    }

    /**
     * Admin variant of searchProducts: supports an explicit active/inactive filter
     * instead of always restricting results to active products only.
     *
     * @param activeFilter null = both active and inactive products,
     *                      true = active products only,
     *                      false = inactive products only
     */
    public List<ProductBean> searchProductsForAdmin(Connection conn, ProductSearchCriteria criteria, Boolean activeFilter) throws DAOException {
        if (criteria == null) {
            throw new IllegalArgumentException("Search criteria cannot be null");
        }

        StringBuilder sql = new StringBuilder();

        String mcVersion = sanitizeString(criteria.getMinecraftVersion());
        String categoryId = sanitizeString(criteria.getCategoryId());
        String keywords = sanitizeString(criteria.getKeywords());

        boolean hasVersionFilter = mcVersion != null;
        boolean hasCategoryFilter = categoryId != null;

        sql.append("SELECT DISTINCT p.* FROM product p ");

        if (hasVersionFilter) {
            sql.append("JOIN product_version pv ON p.product_id = pv.product_id ");
        }
        if (hasCategoryFilter) {
            sql.append("JOIN product_category pc ON p.product_id = pc.product_id ");
        }

        sql.append("WHERE 1=1");

        List<Object> queryParams = new ArrayList<>();

        if (activeFilter != null) {
            sql.append(" AND p.is_active = ?");
            queryParams.add(activeFilter);
        }

        if (keywords != null) {
            sql.append(" AND (LOWER(p.product_name) LIKE ? OR LOWER(p.description) LIKE ?)");
            String kwParam = "%" + keywords.toLowerCase() + "%";
            queryParams.add(kwParam);
            queryParams.add(kwParam);
        }

        if (hasCategoryFilter) {
            sql.append(" AND pc.category_id = ?");
            queryParams.add(categoryId);
        }

        if (criteria.getMinPrice() != null) {
            sql.append(" AND p.price >= ?");
            queryParams.add(criteria.getMinPrice());
        }
        if (criteria.getMaxPrice() != null) {
            sql.append(" AND p.price <= ?");
            queryParams.add(criteria.getMaxPrice());
        }

        if (criteria.getMinRating() != null) {
            sql.append(" AND p.average_rating >= ?");
            queryParams.add(criteria.getMinRating());
        }
        if (criteria.getMaxRating() != null) {
            sql.append(" AND p.average_rating <= ?");
            queryParams.add(criteria.getMaxRating());
        }

        if (criteria.getOnlyWithDiscount() != null && criteria.getOnlyWithDiscount()) {
            sql.append(" AND p.discount > 0");
        }

        if (hasVersionFilter) {
            sql.append(" AND pv.minecraft_version LIKE ?");
            queryParams.add("%" + mcVersion + "%");
        }

        String rawOrderBy = criteria.getOrderByColumn();
        String safeOrderBy = "created_at";

        if (rawOrderBy != null && !rawOrderBy.trim().isEmpty()) {
            safeOrderBy = switch (rawOrderBy.toLowerCase().trim()) {
                case "price" -> "price";
                case "average_rating" -> "average_rating";
                case "total_reviews" -> "total_reviews";
                case "total_downloads" -> "total_downloads";
                case "product_name" -> "product_name";
                case "created_at" -> "created_at";
                default -> "created_at";
            };
        }

        String direction = (criteria.getAscending() != null && criteria.getAscending()) ? "ASC" : "DESC";
        sql.append(" ORDER BY p.").append(safeOrderBy).append(" ").append(direction);

        int pageNumber = (criteria.getPageNumber() == 0 || criteria.getPageNumber() < 1) ? 1 : criteria.getPageNumber();

        int limit = (criteria.getPageSize() == 0 || criteria.getPageSize() < 1) ? 10 : criteria.getPageSize();
        int offset = (pageNumber - 1) * limit;

        sql.append(" LIMIT ? OFFSET ?");

        List<ProductBean> products = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;

            for (Object param : queryParams) {
                ps.setObject(paramIndex++, param);
            }

            ps.setInt(paramIndex++, limit);
            ps.setInt(paramIndex, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error during admin product search. Query: {}", sql, e);
            throw new DAOException("Error performing admin product search", e);
        }

        logger.info("Found products (admin search): {}", products.size());
        return products;
    }

    private String sanitizeString(String input) {
        if (input == null) return null;
        String trimmed = input.trim();
        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed) || "undefined".equalsIgnoreCase(trimmed)) {
            return null;
        }
        return trimmed;
    }

    /**
     * Retrieves all products configured in the system.
     *
     * @param conn Active database connection
     * @return List of all products
     * @throws DAOException if a database error occurs
     */
    public List<ProductBean> findAll(Connection conn) throws DAOException {
        List<ProductBean> products = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                products.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving all products", e);
            throw new DAOException("Error retrieving all products", e);
        }
        return products;
    }

    /**
     * Updates details of an existing Product configuration.
     *
     * @param conn      Active database connection
     * @param productId Unique identifier of the target product
     * @param product   Product model containing new parameters
     * @return true if the row was successfully updated; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Product ID is null or empty, or if the product object is null
     */
    public boolean update(Connection conn, String productId, ProductBean product) throws DAOException {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty for updates");
        }
        if (product == null) {
            throw new IllegalArgumentException("Cannot update with a null Product object");
        }

        String sql = "UPDATE product SET currency_id = ?, average_rating = ?, discount = ?, description = ?, " +
                "is_active = ?, price = ?, product_name = ?, stock_quantity = ?, total_downloads = ?, " +
                "total_reviews = ? WHERE product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getCurrencyId() != null ? product.getCurrencyId().trim() : null);
            ps.setBigDecimal(2, product.getAverageRating());
            ps.setBigDecimal(3, product.getDiscount());
            ps.setString(4, product.getDescription());
            ps.setBoolean(5, product.isActive());
            ps.setBigDecimal(6, product.getPrice());
            ps.setString(7, product.getProductName().trim());

            if (product.getStockQuantity() != null) {
                ps.setInt(8, product.getStockQuantity());
            } else {
                ps.setNull(8, java.sql.Types.INTEGER);
            }

            ps.setInt(9, product.getTotalDownloads());
            ps.setInt(10, product.getTotalReviews());
            ps.setString(11, productId.trim());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Product with Product ID: {} successfully updated", productId);
                return true;
            } else {
                logger.warn("Update issued for non-existent Product ID: {}", productId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update Product with Product ID: {}", productId, e);
            throw new DAOException("Error updating product", e);
        }
        return false;
    }

    /**
     * Updates details of an existing Product configuration using its domain model representation.
     *
     * @param conn    Active database connection
     * @param product Product model containing updated parameters, including its unique Product ID
     * @return true if the row was successfully updated; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the product is null, or if its Product ID is missing
     */
    public boolean update(Connection conn, ProductBean product) throws DAOException {
        if (product == null || product.getProductId() == null) {
            throw new IllegalArgumentException("Attempted to update a null Product or a Product without a Product ID");
        }
        return update(conn, product.getProductId(), product);
    }

    /**
     * Safely decrements the stock quantity of a physical product by 1,
     * ensuring that decrement operations respect zero stock boundaries.
     *
     * @param conn      Active database connection
     * @param productId Unique identifier of the product
     * @return true if stock was successfully decremented; false if item is out of stock or not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Product ID is null or empty
     */
    public boolean decrementStock(Connection conn, String productId) throws DAOException {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty for stock updates");
        }

        String sql = "UPDATE product SET stock_quantity = stock_quantity - 1 " +
                "WHERE product_id = ? AND (stock_quantity IS NULL OR stock_quantity > 0)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId.trim());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Stock successfully decremented for Product ID: {}", productId);
                return true;
            } else {
                logger.warn("Stock decrement failed for Product ID: {} (out of stock or not found)", productId);
            }
        } catch (SQLException e) {
            logger.error("Failed to decrement stock for Product ID: {}", productId, e);
            throw new DAOException("Error decrementing product stock", e);
        }
        return false;
    }

    /**
     * Increments the stock quantity of a physical product by 1,
     * only if stock quantity is currently configured (not null/digital).
     *
     * @param conn      Active database connection
     * @param productId Unique identifier of the product
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Product ID is null or empty
     */
    public void incrementStock(Connection conn, String productId) throws DAOException {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty for stock updates");
        }

        String sql = "UPDATE product SET stock_quantity = stock_quantity + 1 WHERE product_id = ? " +
                "AND stock_quantity IS NOT NULL";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId.trim());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Successfully incremented stock for Product ID: {}", productId);
            } else {
                logger.warn("Stock increment issued for Product ID: {} but product not found or is a digital resource",
                        productId);
            }
        } catch (SQLException e) {
            logger.error("Failed to increment stock for Product ID: {}", productId, e);
            throw new DAOException("Error updating product stock quantity", e);
        }
    }

    /**
     * Soft-enables a product, allowing it to be visible and searchable in catalog queries.
     *
     * @param conn      Active database connection
     * @param productId Unique identifier of the product to activate
     * @return true if activated; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Product ID is null or empty
     */
    public boolean activate(Connection conn, String productId) throws DAOException {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty for activation");
        }

        String sql = "UPDATE product SET is_active = TRUE WHERE product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId.trim());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Product with Product ID: {} successfully activated", productId);
                return true;
            } else {
                logger.warn("Activation issued for non-existent Product ID: {}", productId);
            }
        } catch (SQLException e) {
            logger.error("Failed to activate Product with Product ID: {}", productId, e);
            throw new DAOException("Error activating product", e);
        }
        return false;
    }

    /**
     * Soft-enables a product using its domain model representation.
     *
     * @param conn    Active database connection
     * @param product Product model containing the unique Product ID to activate
     * @return true if activated; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the product is null, or if its Product ID is missing
     */
    public boolean activate(Connection conn, ProductBean product) throws DAOException {
        if (product == null || product.getProductId() == null) {
            throw new IllegalArgumentException("Attempted to activate a null Product or " +
                    "a Product without a Product ID");
        }
        return activate(conn, product.getProductId());
    }

    /**
     * Soft-disables a product, removing it from catalog queries without destroying related order data.
     *
     * @param conn      Active database connection
     * @param productId Unique identifier of the product to deactivate
     * @return true if deactivated; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Product ID is null or empty
     */
    public boolean deactivate(Connection conn, String productId) throws DAOException {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty for deactivation");
        }

        String sql = "UPDATE product SET is_active = FALSE WHERE product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId.trim());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Product with Product ID: {} successfully deactivated", productId);
                return true;
            } else {
                logger.warn("Deactivation issued for non-existent Product ID: {}", productId);
            }
        } catch (SQLException e) {
            logger.error("Failed to deactivate Product with Product ID: {}", productId, e);
            throw new DAOException("Error deactivating product", e);
        }
        return false;
    }

    /**
     * Soft-disables a product using its domain model representation.
     *
     * @param conn    Active database connection
     * @param product Product model containing the unique Product ID to deactivate
     * @return true if deactivated; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the product is null, or if its Product ID is missing
     */
    public boolean deactivate(Connection conn, ProductBean product) throws DAOException {
        if (product == null || product.getProductId() == null) {
            throw new IllegalArgumentException("Attempted to deactivate a null Product or " +
                    "a Product without a Product ID");
        }
        return deactivate(conn, product.getProductId());
    }

    /**
     * Hard-deletes a product entity from the schema.
     *
     * @param conn      Active database connection
     * @param productId Unique identifier of the target product
     * @return true if deleted; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Product ID is null or empty
     */
    public boolean forceDelete(Connection conn, String productId) throws DAOException {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty for physical deletion");
        }

        String sql = "DELETE FROM product WHERE product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId.trim());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Product with Product ID: {} successfully hard-deleted", productId);
                return true;
            } else {
                logger.warn("Force delete issued for non-existent Product ID: {}", productId);
            }
        } catch (SQLException e) {
            logger.error("Failed to force delete Product with Product ID: {}", productId, e);
            throw new DAOException("Error force deleting product", e);
        }
        return false;
    }

    /**
     * Hard-deletes a product entity from the schema using its domain model representation.
     *
     * @param conn    Active database connection
     * @param product Product model containing the unique Product ID to physically delete
     * @return true if deleted; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the product is null, or if its Product ID is missing
     */
    public boolean forceDelete(Connection conn, ProductBean product) throws DAOException {
        if (product == null || product.getProductId() == null) {
            throw new IllegalArgumentException("Attempted to force delete a null Product or " +
                    "a Product without a Product ID");
        }
        return forceDelete(conn, product.getProductId());
    }

    /**
     * Maps a database row from a {@link ResultSet} into a {@link ProductBean}.
     */
    private ProductBean mapRow(ResultSet rs) throws SQLException {
        ProductBean product = new ProductBean();
        product.setProductId(rs.getString("product_id"));
        product.setAccountId(rs.getString("account_id"));
        product.setCurrencyId(rs.getString("currency_id"));
        product.setAverageRating(rs.getBigDecimal("average_rating"));
        product.setDiscount(rs.getBigDecimal("discount"));
        product.setDescription(rs.getString("description"));
        product.setActive(rs.getBoolean("is_active"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setProductName(rs.getString("product_name"));

        int stock = rs.getInt("stock_quantity");
        product.setStockQuantity(rs.wasNull() ? null : stock);

        product.setTotalDownloads(rs.getInt("total_downloads"));
        product.setTotalReviews(rs.getInt("total_reviews"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) product.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp latestUpdate = rs.getTimestamp("latest_update");
        if (latestUpdate != null) product.setLatestUpdate(latestUpdate.toLocalDateTime());

        return product;
    }
}
