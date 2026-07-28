package com.xyra.schemecraft.service;

import com.xyra.schemecraft.connection.ConnectionPool;
import com.xyra.schemecraft.dao.CartDAO;
import com.xyra.schemecraft.dao.ProductDAO;
import com.xyra.schemecraft.dao.ProductImageDAO;
import com.xyra.schemecraft.dto.CartLineItem;
import com.xyra.schemecraft.exception.*;
import com.xyra.schemecraft.model.CartBean;
import com.xyra.schemecraft.model.ProductBean;
import com.xyra.schemecraft.model.ProductImageBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class CartService {
    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    private final CartDAO cartDAO;
    private final ProductDAO productDAO;

    private final EntityValidator entityValidator;
    private final ProductImageDAO productImageDAO;

    // Nel costruttore
    public CartService(){
        this.cartDAO = new CartDAO();
        this.productDAO = new ProductDAO();
        this.productImageDAO = new ProductImageDAO();
        this.entityValidator = new EntityValidator();
    }

    public void addToCart(String rawAccountId, String rawProductId) {
        String accountId = rawAccountId == null ? null : rawAccountId.trim();
        String productId = rawProductId == null ? null : rawProductId.trim();

        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("accountId cannot be null or blank");
        }
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            entityValidator.validateActiveAccount(conn, accountId);
            entityValidator.validateProduct(conn, productId);
            entityValidator.validateProductNotAlreadyOwned(conn, accountId, productId);

            entityValidator.validateProductNotInCart(conn, accountId, productId);

            CartBean cart = new CartBean(accountId, productId);
            cartDAO.insert(conn, cart);
            logger.info("Product {} added to cart for account {}", productId, accountId);

        } catch (DuplicateEntityException e) {
            throw e;
        } catch (DAOException | SQLException e) {
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


    public List<ProductBean> resolveProducts(List<String> rawProductIds) {
        if (rawProductIds == null || rawProductIds.isEmpty()) {
            return List.of();
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            return rawProductIds.stream()
                    .map(productId -> productDAO.findById(conn, productId))
                    .flatMap(Optional::stream)
                    .filter(ProductBean::isActive)
                    .toList();
        } catch (DAOException | SQLException e) {
            logger.error("Database connection error while resolving guest cart products", e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    /**
     * Returns the authenticated account's cart as line items enriched with cover images.
     */
    public List<CartLineItem> viewCart(String rawAccountId) {
        String accountId = rawAccountId == null ? null : rawAccountId.trim();

        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("accountId cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            entityValidator.validateActiveAccount(conn, accountId);
            List<CartBean> productsInCart = cartDAO.findAllByAccountId(conn, accountId);
            List<String> productIds = productsInCart.stream().map(CartBean::getProductId).toList();
            return buildLineItems(conn, productIds);
        } catch (DAOException | SQLException e) {
            logger.error("Database connection error while viewing cart", e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    /**
     * Resolves a raw list of product IDs (typically from a guest cart cookie) into
     * cart line items. Unlike viewCart, this does not touch the cart table at all —
     * it is a stateless lookup used only to render guest carts.
     */
    public List<CartLineItem> resolveCartLineItems(List<String> rawProductIds) {
        if (rawProductIds == null || rawProductIds.isEmpty()) {
            return List.of();
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            return buildLineItems(conn, rawProductIds);
        } catch (DAOException | SQLException e) {
            logger.error("Database connection error while resolving guest cart products", e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    /**
     * Shared enrichment logic: resolves each product ID to its ProductBean (filtering
     * out missing/inactive products), then attaches its lowest-display-order image as
     * the cover, if any image exists.
     */
    private List<CartLineItem> buildLineItems(Connection conn, List<String> productIds) throws SQLException {
        List<CartLineItem> lineItems = new ArrayList<>();

        for (String productId : productIds) {
            Optional<ProductBean> productOpt = productDAO.findById(conn, productId);
            if (productOpt.isEmpty() || !productOpt.get().isActive()) {
                continue;
            }

            ProductBean product = productOpt.get();
            String coverImagePath = productImageDAO.findAllByProductId(conn, productId).stream()
                    .min(Comparator.comparingInt(ProductImageBean::getDisplayOrder))
                    .map(ProductImageBean::getImagePath)
                    .orElse(null);

            lineItems.add(new CartLineItem(product, coverImagePath));
        }

        return lineItems;
    }

    public void mergeGuestCart(String rawAccountId, List<String> guestProductIds) {
        String accountId = rawAccountId == null ? null : rawAccountId.trim();
        if (accountId == null || accountId.isBlank() || guestProductIds == null || guestProductIds.isEmpty()) {
            return;
        }

        for (String productId : guestProductIds) {
            try {
                addToCart(accountId, productId);
            } catch (DuplicateEntityException | EntityNotFoundException e) {
                logger.debug("Skipped guest cart item {} during merge for account {}: {}",
                        productId, accountId, e.getMessage());
            }
        }
    }
}