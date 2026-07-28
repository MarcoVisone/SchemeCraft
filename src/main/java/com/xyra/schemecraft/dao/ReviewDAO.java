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
import com.xyra.schemecraft.model.ReviewBean;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

/**
 * Data Access Object (DAO) for managing persistent {@link ReviewBean} entities.
 */
public class ReviewDAO extends BaseDAO {

    private static final String SELECT_BASE = "SELECT account_id, product_id, comment, created_at, " +
            "is_verified_purchase, rating FROM review ";

    /**
     * Inserts a new product review.
     *
     * @param conn   Active database connection
     * @param review The Review bean to persist
     * @throws DuplicateEntityException if the account has already reviewed this product
     * @throws DAOException             if a generic database error occurs
     * @throws IllegalArgumentException if the review object is null, if Account ID or Product ID are null or empty,
     * or if Rating is out of bounds (not 1-5)
     */
    public void insert(Connection conn, ReviewBean review) throws DAOException {
        if (review == null) {
            throw new IllegalArgumentException("Cannot insert a null Review");
        }
        if (review.getAccountId() == null || review.getAccountId().trim().isEmpty() ||
                review.getProductId() == null || review.getProductId().trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID and Product ID must be valid and populated");
        }
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        String sql = "INSERT INTO review (account_id, product_id, comment, is_verified_purchase, rating) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, review.getAccountId().trim());
            ps.setString(2, review.getProductId().trim());
            ps.setString(3, review.getComment());
            ps.setBoolean(4, review.isVerifiedPurchase());
            ps.setInt(5, review.getRating());

            ps.executeUpdate();
            logger.info("Review successfully created by Account ID: {} for Product ID: {}",
                    review.getAccountId(), review.getProductId());
        } catch (SQLException e) {
            logger.error("Failed to insert review for Product ID: {} by Account ID: {}", review.getProductId(),
                    review.getAccountId(), e);
            if (SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION.equals(e.getSQLState()) ||
                    e.getErrorCode() == MYSQL_ERR_DUPLICATE_KEY) {
                throw new DuplicateEntityException("Review already exists for Account ID: " + review.getAccountId() +
                        " and Product ID: " + review.getProductId(), e);
            }
            throw new DAOException("Error occurred while saving the review", e);
        }
    }

    /**
     * Retrieves a single product review by its composite key.
     *
     * @param conn      Active database connection
     * @param productId Unique identifier of the reviewed product
     * @param accountId Unique identifier of the author account
     * @return An Optional containing the populated review, or empty if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Product ID or Account ID are null or empty
     */
    public Optional<ReviewBean> findById(Connection conn, String productId, String accountId) throws DAOException {
        if (productId == null || productId.trim().isEmpty() || accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID and Account ID cannot be null or empty for lookup");
        }

        String sql = SELECT_BASE + "WHERE product_id = ? AND account_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId.trim());
            ps.setString(2, accountId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching review for Product ID: {} and Account ID: {}",
                    productId, accountId, e);
            throw new DAOException("Error fetching review", e);
        }
        return Optional.empty();
    }

    /**
     * Retrieves a paginated list of reviews written for a specific product, sorted by creation date.
     *
     * @param conn       Active database connection
     * @param productId  Unique identifier of the product
     * @param pageNumber Pagination page number
     * @param pageSize   Number of items per page
     * @return List of product reviews
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Product ID is null or empty
     */
    public List<ReviewBean> findByProductId(Connection conn, String productId, int pageNumber, int pageSize)
            throws DAOException {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty for reviews lookup");
        }

        String sql = SELECT_BASE + "WHERE product_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<ReviewBean> reviews = new ArrayList<>();

        int limit = pageSize < 1 ? 10 : pageSize;
        int offset = (pageNumber < 1 ? 0 : pageNumber - 1) * limit;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId.trim());
            ps.setInt(2, limit);
            ps.setInt(3, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reviews.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving reviews for Product ID: {}", productId, e);
            throw new DAOException("Error retrieving reviews by Product ID", e);
        }
        return reviews;
    }

    /**
     * Checks if an account has already written a review for a specific product.
     *
     * @param conn      Active database connection
     * @param accountId Unique identifier of the account
     * @param productId Unique identifier of the product
     * @return true if a review exists, false otherwise
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Account ID or Product ID is null or empty
     */
    public boolean existsByAccountAndProduct(Connection conn, String accountId, String productId) throws DAOException {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty for review lookup");
        }
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty for review lookup");
        }

        String sql = "SELECT COUNT(*) FROM review WHERE account_id = ? AND product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId.trim());
            ps.setString(2, productId.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while checking existing review for Account ID: {} and Product ID: {}", accountId, productId, e);
            throw new DAOException("Error checking existing review by account and product", e);
        }
        return false;
    }

    /**
     * Retrieves all reviews authored by a specific user account.
     *
     * @param conn      Active database connection
     * @param accountId Unique identifier of the author account
     * @return List of user reviews
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Account ID is null or empty
     */
    public List<ReviewBean> findAllByAccountId(Connection conn, String accountId) throws DAOException {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty for user reviews lookup");
        }

        String sql = SELECT_BASE + "WHERE account_id = ? ORDER BY created_at DESC";
        List<ReviewBean> reviews = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reviews.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving reviews for Account ID: {}", accountId, e);
            throw new DAOException("Error retrieving reviews by Account ID", e);
        }
        return reviews;
    }

    /**
     * Retrieves reviews for a product filtered by a specific rating range, supporting pagination.
     *
     * @param conn       Active database connection
     * @param productId  Unique identifier of the product
     * @param minRating  Minimum rating limit (1-5)
     * @param maxRating  Maximum rating limit (1-5)
     * @param pageNumber Pagination page number
     * @param pageSize   Number of items per page
     * @return List of filtered reviews
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Product ID is null or empty, or if rating boundaries are invalid
     */
    public List<ReviewBean> findByProductAndRatingRange(Connection conn, String productId, int minRating,
                                                        int maxRating, int pageNumber, int pageSize)
            throws DAOException {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty for filtered lookup");
        }
        if (minRating < 1 || maxRating > 5 || minRating > maxRating) {
            throw new IllegalArgumentException("Invalid rating range. Must be between 1 and 5, and min <= max.");
        }

        String sql = SELECT_BASE + "WHERE product_id = ? AND rating BETWEEN ? AND ? " +
                "ORDER BY created_at DESC LIMIT ? OFFSET ?";

        List<ReviewBean> reviews = new ArrayList<>();

        int limit = pageSize < 1 ? 10 : pageSize;
        int offset = (pageNumber < 1 ? 0 : pageNumber - 1) * limit;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId.trim());
            ps.setInt(2, minRating);
            ps.setInt(3, maxRating);
            ps.setInt(4, limit);
            ps.setInt(5, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reviews.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving reviews for Product ID: {} within Rating range: {}-{}",
                    productId, minRating, maxRating, e);
            throw new DAOException("Error retrieving reviews filtered by Rating", e);
        }
        return reviews;
    }

    /**
     * Updates comment and score details of an existing product review.
     *
     * @param conn      Active database connection
     * @param productId Unique identifier of the product
     * @param accountId Unique identifier of the author account
     * @param review    Review bean containing updated values
     * @return true if the row was successfully updated; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Product ID or Account ID are null or empty,
     * if the review object is null, or if Rating is out of bounds (not 1-5)
     */
    public boolean update(Connection conn, String productId, String accountId, ReviewBean review) throws DAOException {
        if (productId == null || productId.trim().isEmpty() || accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID and Account ID cannot be null or empty for updates");
        }
        if (review == null) {
            throw new IllegalArgumentException("Cannot update with a null Review object");
        }
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        String sql = "UPDATE review SET comment = ?, is_verified_purchase = ?, rating = ? " +
                "WHERE product_id = ? AND account_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, review.getComment());
            ps.setBoolean(2, review.isVerifiedPurchase());
            ps.setInt(3, review.getRating());
            ps.setString(4, productId.trim());
            ps.setString(5, accountId.trim());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Review for Product ID: {} by Account ID: {} successfully updated", productId, accountId);
                return true;
            } else {
                logger.warn("Update issued for non-existent review for Product ID: {} and Account ID: {}",
                        productId, accountId);
            }
        } catch (SQLException e) {
            logger.error("Failed to update review for Product ID: {} and Account ID: {}", productId, accountId, e);
            throw new DAOException("Error updating review", e);
        }
        return false;
    }

    /**
     * Updates comment and score details of an existing product review using its domain model representation.
     *
     * @param conn   Active database connection
     * @param review Review bean containing updated parameters, including its unique Product ID and Account ID keys
     * @return true if the row was successfully updated; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the review object is null,
     * or if either its Product ID or Account ID are missing
     */
    public boolean update(Connection conn, ReviewBean review) throws DAOException {
        if (review == null || review.getProductId() == null || review.getAccountId() == null) {
            throw new IllegalArgumentException("Attempted to update a null Review or " +
                    "a review with missing composite keys");
        }
        return update(conn, review.getProductId(), review.getAccountId(), review);
    }

    /**
     * Saves a new review or updates it if it already exists for the same product and account.
     *
     * @param conn   Active database connection
     * @param review Review bean to save or update
     * @throws DAOException if a database error occurs
     * @throws IllegalArgumentException if the review object is null
     */
    public void saveOrUpdate(Connection conn, ReviewBean review) throws DAOException {
        if (review == null || review.getProductId() == null || review.getAccountId() == null) {
            throw new IllegalArgumentException("Review and its keys cannot be null for saveOrUpdate");
        }

        String sql = "INSERT INTO review (product_id, account_id, rating, comment, is_verified_purchase) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE rating = VALUES(rating), comment = VALUES(comment), is_verified_purchase = VALUES(is_verified_purchase)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, review.getProductId().trim());
            ps.setString(2, review.getAccountId().trim());
            ps.setInt(3, review.getRating());
            ps.setString(4, review.getComment());
            ps.setBoolean(5, review.isVerifiedPurchase());

            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Database error while saving/updating review for Product ID: {} and Account ID: {}",
                    review.getProductId(), review.getAccountId(), e);
            throw new DAOException("Error saving or updating review", e);
        }
    }

    /**
     * Deletes a product review.
     *
     * @param conn      Active database connection
     * @param productId Unique identifier of the product
     * @param accountId Unique identifier of the author account
     * @return true if the record was successfully deleted; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if Product ID or Account ID are null or empty
     */
    public boolean delete(Connection conn, String productId, String accountId) throws DAOException {
        if (productId == null || productId.trim().isEmpty() || accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID and Account ID cannot be null or empty for deletion");
        }

        String sql = "DELETE FROM review WHERE product_id = ? AND account_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId.trim());
            ps.setString(2, accountId.trim());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Successfully deleted review for Product ID: {} and Account ID: {}", productId, accountId);
                return true;
            } else {
                logger.warn("Delete issued for non-existent review for Product ID: {} and Account ID: {}",
                        productId, accountId);
            }
        } catch (SQLException e) {
            logger.error("Failed to delete review for Product ID: {} and Account ID: {}", productId, accountId, e);
            throw new DAOException("Error deleting review", e);
        }
        return false;
    }

    /**
     * Deletes a product review using its domain model representation.
     *
     * @param conn   Active database connection
     * @param review Review bean containing the unique Product ID and Account ID keys to delete
     * @return true if the record was successfully deleted; false if not found
     * @throws DAOException             if a database error occurs
     * @throws IllegalArgumentException if the review object is null,
     * or if either its Product ID or Account ID are missing
     */
    public boolean delete(Connection conn, ReviewBean review) throws DAOException {
        if (review == null || review.getProductId() == null || review.getAccountId() == null) {
            throw new IllegalArgumentException("Attempted to delete a null Review or " +
                    "a review with missing composite keys");
        }
        return delete(conn, review.getProductId(), review.getAccountId());
    }

    /**
     * Maps a database row from a {@link ResultSet} into a {@link ReviewBean}.
     */
    private ReviewBean mapRow(ResultSet rs) throws SQLException {
        ReviewBean review = new ReviewBean();
        review.setAccountId(rs.getString("account_id"));
        review.setProductId(rs.getString("product_id"));
        review.setComment(rs.getString("comment"));
        review.setVerifiedPurchase(rs.getBoolean("is_verified_purchase"));
        review.setRating(rs.getInt("rating"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            review.setCreatedAt(createdAt.toLocalDateTime());
        }
        return review;
    }
}
