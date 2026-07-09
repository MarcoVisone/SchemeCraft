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

    public void delete(String favoriteId) {
        String query = "DELETE FROM favorite WHERE favorite_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, favoriteId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while deleting favorite with ID: {}", favoriteId, e);
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

    public boolean isFavorite(String accountId, String productId) {
        String sql = "SELECT 1 FROM favorite WHERE account_id = ? AND product_id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountId);
            ps.setString(2, productId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Error checking favorite status for account {} and product {}", accountId, productId, e);
            return false;
        }
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
            logger.error("Error occurred while fetching favorites for account ID: {}", accountId, e);
        }
        return favorites;
    }

    private Favorite mapResultSetToFavorite(ResultSet rs) throws SQLException {
        Favorite favorite = new Favorite();
        favorite.setFavoriteId(rs.getString("favorite_id"));
        favorite.setAccountId(rs.getString("account_id"));
        favorite.setProductId(rs.getString("product_id"));
        return favorite;
    }
}
