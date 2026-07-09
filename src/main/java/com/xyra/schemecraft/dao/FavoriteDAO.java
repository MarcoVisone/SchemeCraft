package com.xyra.schemecraft.dao;

import com.xyra.schemecraft.connection.ConnectionPool;
import com.xyra.schemecraft.model.Favorite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavoriteDAO {
    private static final Logger logger = LoggerFactory.getLogger(FavoriteDAO.class);

    public void save(Favorite favorite) {
        String sql = "INSERT INTO favorite (favorite_id, account_id, product_id) VALUES (?, ?, ?)";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, favorite.getFavoriteId());
            ps.setString(2, favorite.getAccountId());
            ps.setString(3, favorite.getProductId());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while adding product {} to favorites for account {}",
                    favorite.getProductId(), favorite.getAccountId(), e);
        }
    }

    public Favorite findById(String favoriteId) {
        String sql = "SELECT * FROM favorite WHERE favorite_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, favoriteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToFavorite(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error occurred while searching for favorite with ID: {}", favoriteId, e);
        }
        return null;
    }

    public List<Favorite> findByAccountId(String accountId) {
        String sql = "SELECT * FROM favorite WHERE account_id = ?";
        List<Favorite> favorites = new ArrayList<>();

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    favorites.add(mapResultSetToFavorite(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error occurred while searching for favorites for account ID: {}", accountId, e);
        }
        return favorites;
    }

    public List<Favorite> findByProductId(String productId) {
        String sql = "SELECT * FROM favorite WHERE product_id = ?";
        List<Favorite> favorites = new ArrayList<>();

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    favorites.add(mapResultSetToFavorite(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error occurred while searching for favorites for product ID: {}", productId, e);
        }
        return favorites;
    }

    public List<Favorite> findAll() {
        String sql = "SELECT * FROM favorite";
        List<Favorite> favorites = new ArrayList<>();

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                favorites.add(mapResultSetToFavorite(rs));
            }
        } catch (SQLException e) {
            logger.error("Error occurred while fetching all favorites", e);
        }
        return favorites;
    }

    public void update(Favorite favorite) {
        String sql = "UPDATE favorite SET account_id = ?, product_id = ? WHERE favorite_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, favorite.getAccountId());
            ps.setString(2, favorite.getProductId());
            ps.setString(3, favorite.getFavoriteId());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while updating favorite with ID: {}", favorite.getFavoriteId(), e);
        }
    }

    public void deleteById(String favoriteId) {
        String sql = "DELETE FROM favorite WHERE favorite_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, favoriteId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while deleting favorite with ID: {}", favoriteId, e);
        }
    }

    private Favorite mapResultSetToFavorite(ResultSet rs) throws SQLException {
        return new Favorite(
                rs.getString("favorite_id"),
                rs.getString("account_id"),
                rs.getString("product_id")
        );
    }
}
