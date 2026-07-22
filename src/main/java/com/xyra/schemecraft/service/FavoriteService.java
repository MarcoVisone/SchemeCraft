package com.xyra.schemecraft.service;

import com.xyra.schemecraft.connection.ConnectionPool;
import com.xyra.schemecraft.dao.FavoriteDAO;
import com.xyra.schemecraft.dao.ProductDAO;
import com.xyra.schemecraft.exception.DAOException;
import com.xyra.schemecraft.exception.ServiceException;
import com.xyra.schemecraft.model.FavoriteBean;
import com.xyra.schemecraft.model.ProductBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FavoriteService {
    private static final Logger logger = LoggerFactory.getLogger(FavoriteService.class);

    private final EntityValidator entityValidator;

    private final FavoriteDAO favoriteDAO;
    private final ProductDAO productDAO;

    public FavoriteService(){
        this.favoriteDAO = new FavoriteDAO();
        this.entityValidator = new EntityValidator();
        this.productDAO = new ProductDAO();
    }

    public void addFavorite(String rawAccountId, String rawProductId) {
        String accountId = rawAccountId == null ? null : rawAccountId.trim();
        String productId = rawProductId == null ? null : rawProductId.trim();
        if (accountId == null || accountId.isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty");
        }
        if (productId == null || productId.isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        try (Connection conn = ConnectionPool.getConnection()) {
            entityValidator.validateActiveProduct(conn, productId);
            entityValidator.validateActiveAccount(conn, accountId);
            FavoriteBean favorite = new FavoriteBean();
            favorite.setAccountId(accountId);
            favorite.setProductId(productId);
            favoriteDAO.insert(conn, favorite);
            logger.info("Product {} added to favorites for account {}", productId, accountId);
        } catch (SQLException e) {
            logger.error("Database connection error while adding favorite for accountId: {} and productId: {}", accountId, productId, e);
            throw new ServiceException("Database connection error while adding favorite", e);
        } catch (DAOException e) {
            logger.error("DAO error while adding favorite for accountId: {} and productId: {}", accountId, productId, e);
            throw new ServiceException("DAO error while adding favorite", e);
        }
    }

    public void removeFavorite(String rawAccountId, String rawProductId) {
        String accountId = rawAccountId == null ? null : rawAccountId.trim();
        String productId = rawProductId == null ? null : rawProductId.trim();
        if (accountId == null || accountId.isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty");
        }
        if(productId == null || productId.isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }

        try(Connection conn = ConnectionPool.getConnection()) {
            entityValidator.validateActiveProduct(conn, productId);
            entityValidator.validateActiveAccount(conn, accountId);
            favoriteDAO.delete(conn, accountId, productId);
            logger.info("Product {} removed from favorites for account {}", productId, accountId);
        } catch (SQLException e) {
            logger.error("Database connection error while removing favorite for accountId: {} and productId: {}", accountId, productId, e);
            throw new ServiceException("Database connection error while removing favorite", e);
        } catch (DAOException e) {
            logger.error("DAO error while removing favorite for accountId: {} and productId: {}", accountId, productId, e);
            throw new ServiceException("DAO error while removing favorite", e);
        }
    }
    public List<ProductBean> listFavorites(String rawAccountId) {
        String accountId = rawAccountId == null ? null : rawAccountId.trim();
        if (accountId == null || accountId.isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty");
        }
        try (Connection conn = ConnectionPool.getConnection()) {
            return favoriteDAO.findAllByAccountId(conn, accountId).stream()
                    .map(favorite -> productDAO.findById(conn, favorite.getProductId()))
                    .flatMap(Optional::stream)
                    .filter(ProductBean::isActive)
                    .toList();
        } catch (SQLException e) {
            logger.error("Database connection error while listing favorites for accountId: {}", accountId, e);
            throw new ServiceException("Database connection error while listing favorites", e);
        } catch (DAOException e) {
            logger.error("DAO error while listing favorites for accountId: {}", accountId, e);
            throw new ServiceException("DAO error while listing favorites", e);
        }
    }
    public boolean isFavorite(String rawAccountId, String rawProductId) {
        String accountId = rawAccountId == null ? null : rawAccountId.trim();
        String productId = rawProductId == null ? null : rawProductId.trim();
        if (accountId == null || accountId.isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty");
        }
        if(productId == null || productId.isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }

        try(Connection conn = ConnectionPool.getConnection()) {
            entityValidator.validateActiveProduct(conn, productId);
            entityValidator.validateActiveAccount(conn, accountId);
            return favoriteDAO.findById(conn, accountId, productId).isPresent();
        }catch (SQLException e) {
            logger.error("Database connection error while checking favorite for accountId: {} and productId: {}", accountId, productId, e);
            throw new ServiceException("Database connection error while checking favorite", e);
        } catch (DAOException e) {
            logger.error("DAO error while checking favorite for accountId: {} and productId: {}", accountId, productId, e);
            throw new ServiceException("DAO error while checking favorite", e);
        }
    }
}
