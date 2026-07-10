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
import com.xyra.schemecraft.model.Review;

import static com.xyra.schemecraft.constant.DatabaseConstants.*;

public class ReviewDAO extends BaseDAO {
    private static final String SELECT_BASE = "SELECT account_id, product_id, comment, created_at, " +
            "is_verified_purchase, rating FROM review";

    public void insert(Connection conn, Review review) throws DAOException {
        if (review == null) {
            throw new IllegalArgumentException("Cannot insert a null Review");
        }

        String sql = "INSERT INTO review (account_id, product_id, comment, is_verified_purchase, rating) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, review.getAccountId());
            ps.setString(2, review.getProductId());
            ps.setString(3, review.getComment());
            ps.setBoolean(4, review.isVerifiedPurchase());
            ps.setInt(5, review.getRating());

            ps.executeUpdate();
            logger.info("Review successfully created by Account: {} for Product: {}",
                    review.getAccountId(), review.getProductId());
        } catch (SQLException e) {
            logger.error("Failed to insert review for product {} by account {}", review.getProductId(),
                    review.getAccountId(), e);
            if (MYSQL_DUPLICATE_KEY_STATE.equals(e.getSQLState()) || e.getErrorCode() == MYSQL_DUPLICATE_KEY_CODE) {
                throw new DuplicateEntityException("This account has already reviewed this product", e);
            }
            throw new DAOException("Error occurred while saving the review", e);
        }
    }

    public Optional<Review> findById(Connection conn, String productId, String accountId) throws DAOException {
        String sql = SELECT_BASE + " WHERE product_id = ? AND account_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            ps.setString(2, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while fetching review for product {} by account {}", productId, accountId, e);
            throw new DAOException("Error fetching review", e);
        }
        return Optional.empty();
    }

    public List<Review> findByProductId(Connection conn, String productId, int pageNumber, int pageSize)
            throws DAOException {
        String sql = SELECT_BASE + " WHERE product_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<Review> reviews = new ArrayList<>();

        int limit = pageSize < 1 ? 10 : pageSize;
        int offset = (pageNumber < 1 ? 0 : pageNumber - 1) * limit;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reviews.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving reviews for product ID: {}", productId, e);
            throw new DAOException("Error retrieving reviews by product ID", e);
        }
        return reviews;
    }

    public List<Review> findAllByAccountId(Connection conn, String accountId) throws DAOException {
        String sql = SELECT_BASE + " WHERE account_id = ? ORDER BY created_at DESC";
        List<Review> reviews = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reviews.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while retrieving reviews for account ID: {}", accountId, e);
            throw new DAOException("Error retrieving reviews by account ID", e);
        }
        return reviews;
    }

    public List<Review> findByProductAndRatingRange(Connection conn, String productId, int minRating,
                                                    int maxRating, int pageNumber, int pageSize) throws DAOException {

        if (minRating < 1 || maxRating > 5 || minRating > maxRating) {
            throw new IllegalArgumentException("Invalid rating range. Must be between 1 and 5, and min <= max.");
        }

        String sql = SELECT_BASE + " WHERE product_id = ? AND rating BETWEEN ? AND ? " +
                "ORDER BY created_at DESC LIMIT ? OFFSET ?";

        List<Review> reviews = new ArrayList<>();

        int limit = pageSize < 1 ? 10 : pageSize;
        int offset = (pageNumber < 1 ? 0 : pageNumber - 1) * limit;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
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
            logger.error("Database error while retrieving reviews for product ID: {} in rating range: {}-{}",
                    productId, minRating, maxRating, e);
            throw new DAOException("Error retrieving reviews filtered by rating", e);
        }
        return reviews;
    }

    public boolean update(Connection conn, String productId, String accountId, Review review) throws DAOException {
        if (review == null) {
            throw new IllegalArgumentException("Cannot update with a null Review object");
        }

        String sql = "UPDATE review SET comment = ?, is_verified_purchase = ?, rating = ? " +
                "WHERE product_id = ? AND account_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, review.getComment());
            ps.setBoolean(2, review.isVerifiedPurchase());
            ps.setInt(3, review.getRating());
            ps.setString(4, productId);
            ps.setString(5, accountId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Review for product {} by account {} successfully updated", productId, accountId);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Failed to update review for product {} by account {}", productId, accountId, e);
            throw new DAOException("Error updating review", e);
        }
        return false;
    }

    public boolean update(Connection conn, Review review) throws DAOException {
        if (review == null || review.getProductId() == null || review.getAccountId() == null) {
            throw new IllegalArgumentException("Attempted to update a null review " +
                    "or a review with missing composite keys");
        }
        return update(conn, review.getProductId(), review.getAccountId(), review);
    }

    public boolean delete(Connection conn, String productId, String accountId) throws DAOException {
        String sql = "DELETE FROM review WHERE product_id = ? AND account_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            ps.setString(2, accountId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Failed to delete review for product {} by account {}", productId, accountId, e);
            throw new DAOException("Error deleting review", e);
        }
    }

    public boolean delete(Connection conn, Review review) throws DAOException {
        if (review == null || review.getProductId() == null || review.getAccountId() == null) {
            throw new IllegalArgumentException("Attempted to delete a null review " +
                    "or a review with missing composite keys");
        }
        return delete(conn, review.getProductId(), review.getAccountId());
    }

    private Review mapRow(ResultSet rs) throws SQLException {
        Review review = new Review();
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
