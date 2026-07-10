package com.xyra.schemecraft.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.xyra.schemecraft.exception.DAOException;
import com.xyra.schemecraft.exception.DuplicateEntityException;
import com.xyra.schemecraft.model.Product;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

public class ProductDAO extends BaseDAO {
    private static final String SELECT_BASE = "SELECT product_id, account_id, currency_id, average_rating, " +
            "created_at, discount, description, is_active, latest_update, price, product_name, stock_quantity, " +
            "total_downloads, total_reviews FROM product";

    public void insert(Connection conn, Product product) throws DAOException {
        if (product == null) {
            throw new IllegalArgumentException("Cannot insert a null Product");
        }

        String sql = "INSERT INTO product (product_id, account_id, currency_id, average_rating, discount, " +
                "description, is_active, price, product_name, stock_quantity, total_downloads, total_reviews) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getProductId());
            ps.setString(2, product.getAccountId());
            ps.setString(3, product.getCurrencyId());
            ps.setBigDecimal(4, product.getAverageRating());
            ps.setBigDecimal(5, product.getDiscount());
            ps.setString(6, product.getDescription());
            ps.setBoolean(7, product.isActive());
            ps.setBigDecimal(8, product.getPrice());
            ps.setString(9, product.getProductName());

            if (product.getStockQuantity() != null) {
                ps.setInt(10, product.getStockQuantity());
            } else {
                ps.setNull(10, java.sql.Types.INTEGER);
            }

            ps.setInt(11, product.getTotalDownloads());
            ps.setInt(12, product.getTotalReviews());

            ps.executeUpdate();
            logger.info("Product successfully inserted with ID: {} by Account: {}", product.getProductId(),
                    product.getAccountId());
        } catch (SQLException e) {
            logger.error("Failed to insert product with Name: {}", product.getProductName(), e);
            if (MYSQL_DUPLICATE_KEY_STATE.equals(e.getSQLState()) || e.getErrorCode() == MYSQL_DUPLICATE_KEY_CODE) {
                throw new DuplicateEntityException("Product ID already exists: " + product.getProductId(), e);
            }
            throw new DAOException("Error occurred while inserting product", e);
        }
    }

    public Optional<Product> findById(Connection conn, String productId) throws DAOException {
        String sql = SELECT_BASE + " WHERE product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching product with ID: {}", productId, e);
            throw new DAOException("Error fetching product by ID", e);
        }
        return Optional.empty();
    }

    public List<Product> findAllByAccountId(Connection conn, String accountId) throws DAOException {
        String sql = SELECT_BASE + " WHERE account_id = ?";
        List<Product> products = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving products for account ID: {}", accountId, e);
            throw new DAOException("Error retrieving products by account ID", e);
        }
        return products;
    }

    public List<Product> searchProducts(Connection conn, ProductSearchCriteria criteria) throws DAOException {
        if (criteria == null) {
            throw new IllegalArgumentException("Search criteria cannot be null");
        }

        StringBuilder sql = new StringBuilder();

        if (criteria.getMinecraftVersion() != null && !criteria.getMinecraftVersion().trim().isEmpty()) {
            sql.append("SELECT DISTINCT p.* FROM product p ");
            sql.append("JOIN product_version pv ON p.product_id = pv.product_id ");
            sql.append("WHERE p.is_active = TRUE");
        } else {
            sql.append(SELECT_BASE).append(" WHERE is_active = TRUE");
        }

        List<Object> queryParams = new ArrayList<>();

        if (criteria.getKeywords() != null && !criteria.getKeywords().trim().isEmpty()) {
            sql.append(" AND MATCH(p.product_name, p.description) AGAINST(? IN NATURAL LANGUAGE MODE)");
            queryParams.add(criteria.getKeywords().trim());
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

        if (criteria.getMinecraftVersion() != null && !criteria.getMinecraftVersion().trim().isEmpty()) {
            sql.append(" AND pv.minecraft_version = ?");
            queryParams.add(criteria.getMinecraftVersion().trim());
        }

        if (criteria.getOrderByColumn() != null) {
            String safeOrderBy;
            switch (criteria.getOrderByColumn().toLowerCase().trim()) {
                case "price":           safeOrderBy = "price"; break;
                case "average_rating":  safeOrderBy = "average_rating"; break;
                case "total_reviews":   safeOrderBy = "total_reviews"; break;
                case "total_downloads": safeOrderBy = "total_downloads"; break;
                default:
                    throw new IllegalArgumentException("Invalid sort column: " + criteria.getOrderByColumn());
            }
            String direction = (criteria.getAscending() != null && !criteria.getAscending()) ? "DESC" : "ASC";
            sql.append(" ORDER BY p.").append(safeOrderBy).append(" ").append(direction);
        }

        sql.append(" LIMIT ? OFFSET ?");

        int limit = criteria.getPageSize();
        int offset = (criteria.getPageNumber() - 1) * limit;

        List<Product> products = new ArrayList<>();

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

        return products;
    }

    public List<Product> findAll(Connection conn) throws DAOException {
        List<Product> products = new ArrayList<>();

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

    public boolean update(Connection conn, String productId, Product product) throws DAOException {
        if (product == null) {
            throw new IllegalArgumentException("Cannot update with a null Product object");
        }

        String sql = "UPDATE product SET currency_id = ?, average_rating = ?, discount = ?, description = ?, " +
                "is_active = ?, price = ?, product_name = ?, stock_quantity = ?, total_downloads = ?, " +
                "total_reviews = ? WHERE product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getCurrencyId());
            ps.setBigDecimal(2, product.getAverageRating());
            ps.setBigDecimal(3, product.getDiscount());
            ps.setString(4, product.getDescription());
            ps.setBoolean(5, product.isActive());
            ps.setBigDecimal(6, product.getPrice());
            ps.setString(7, product.getProductName());

            if (product.getStockQuantity() != null) {
                ps.setInt(8, product.getStockQuantity());
            } else {
                ps.setNull(8, java.sql.Types.INTEGER);
            }

            ps.setInt(9, product.getTotalDownloads());
            ps.setInt(10, product.getTotalReviews());
            ps.setString(11, productId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Product with ID: {} successfully updated", productId);
                return true;
            } else {
                logger.warn("Update issued for non-existent product ID: {}", productId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update product with ID: {}", productId, e);
            throw new DAOException("Error updating product", e);
        }
        return false;
    }

    public boolean update(Connection conn, Product product) throws DAOException {
        if (product == null || product.getProductId() == null) {
            throw new IllegalArgumentException("Attempted to update a null product or a product without an ID");
        }
        return update(conn, product.getProductId(), product);
    }

    public boolean activate(Connection conn, String productId) throws DAOException {
        String sql = "UPDATE product SET is_active = TRUE WHERE product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Failed to activate product with ID: {}", productId, e);
            throw new DAOException("Error activating product", e);
        }
    }

    public boolean activate(Connection conn, Product product) throws DAOException {
        if (product == null || product.getProductId() == null) {
            throw new IllegalArgumentException("Attempted to activate a null product or a product without an ID");
        }
        return activate(conn, product.getProductId());
    }

    public boolean deactivate(Connection conn, String productId) throws DAOException {
        String sql = "UPDATE product SET is_active = FALSE WHERE product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Failed to deactivate product with ID: {}", productId, e);
            throw new DAOException("Error deactivating product", e);
        }
    }

    public boolean deactivate(Connection conn, Product product) throws DAOException {
        if (product == null || product.getProductId() == null) {
            throw new IllegalArgumentException("Attempted to deactivate a null product or a product without an ID");
        }
        return deactivate(conn, product.getProductId());
    }

    public boolean forceDelete(Connection conn, String productId) throws DAOException {
        String sql = "DELETE FROM product WHERE product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Failed to force delete product with ID: {}", productId, e);
            throw new DAOException("Error force deleting product", e);
        }
    }

    public boolean forceDelete(Connection conn, Product product) throws DAOException {
        if (product == null || product.getProductId() == null) {
            throw new IllegalArgumentException("Attempted to force delete a null product or a product without an ID");
        }
        return forceDelete(conn, product.getProductId());
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product product = new Product();
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
