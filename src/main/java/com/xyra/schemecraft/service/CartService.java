package com.xyra.schemecraft.service;

import com.xyra.schemecraft.connection.ConnectionPool;
import com.xyra.schemecraft.dao.CartDAO;
import com.xyra.schemecraft.exception.*;
import com.xyra.schemecraft.model.CartBean;
import com.xyra.schemecraft.model.ProductBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class CartService {
    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    private static CartDAO cartDAO;
    private static EntityValidator entityValidator;
    CartService(){
        cartDAO = new CartDAO();
        entityValidator = new EntityValidator();
    }

    public void addToCart(String rawAccountId, String rawProductId) {
        String accountId = rawAccountId == null ? null : rawAccountId.trim();
        String productId = rawProductId == null ? null : rawProductId.trim();

        if(accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("accountId cannot be null or blank");
        }
        if(productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId cannot be null or blank");
        }

        try(Connection conn = ConnectionPool.getConnection()) {
            entityValidator.validateActiveAccount(conn, accountId);
            entityValidator.validateProduct(conn, productId);
            entityValidator.validateProductNotAlreadyOwned(conn, accountId, productId);
            CartBean cart = new CartBean(accountId, productId);
            cartDAO.insert(conn, cart);
            logger.info("Product {} added to cart for account {}", productId, accountId);
        } catch(DAOException | SQLException e) {
            logger.error("Database connection error while adding product to cart", e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    public void removeFromCart(String rawAccountId, String rawProductId) {
        String accountId = rawAccountId == null ? null : rawAccountId.trim();
        String productId = rawProductId == null ? null : rawProductId.trim();

        if(accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("accountId cannot be null or blank");
        }
        if(productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId cannot be null or blank");
        }

        try(Connection conn = ConnectionPool.getConnection()) {
            entityValidator.validateActiveAccount(conn, accountId);
            boolean check = cartDAO.delete(conn, accountId, productId);
            if(check) {
                logger.info("Product {} removed from cart for account {}", productId, accountId);
            } else {
                logger.warn("Product {} not found in cart for account {}", productId, accountId);
                throw new EntityNotFoundException("Product not found in cart for the given account",
                        EntityNotFoundException.EntityType.PRODUCT);
            }
        } catch(DAOException | SQLException e) {
            logger.error("Database connection error while removing product from cart", e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    public List<CartBean> viewCart(String rawAccountId) {
        String accountId = rawAccountId == null ? null : rawAccountId.trim();

        if(accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("accountId cannot be null or blank");
        }

        try(Connection conn = ConnectionPool.getConnection()) {
            entityValidator.validateActiveAccount(conn, accountId);
            List<CartBean> productsInCart = cartDAO.findAllByAccountId(conn, accountId);
            logger.info("Retrieved {} products in cart for account {}", productsInCart.size(), accountId);
            return productsInCart;
        } catch(DAOException | SQLException e) {
            logger.error("Database connection error while viewing cart", e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    public void clearCart(String rawAccountId) {
        String accountId = rawAccountId == null ? null : rawAccountId.trim();

        if(accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("accountId cannot be null or blank");
        }

        try(Connection conn = ConnectionPool.getConnection()) {
            entityValidator.validateActiveAccount(conn, accountId);
            boolean check = cartDAO.deleteAllByAccountId(conn, accountId);
            if(check) {
                logger.info("Cart cleared for account {}", accountId);
            } else {
                logger.warn("Failed to clear cart for account {}", accountId);
                throw new ServiceException("Failed to clear cart for the given account");
            }
        } catch(DAOException | SQLException e) {
            logger.error("Database connection error while clearing cart", e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }
}