package com.xyra.schemecraft.service;

import com.xyra.schemecraft.connection.ConnectionPool;
import com.xyra.schemecraft.constant.ServiceConstants;
import com.xyra.schemecraft.dao.*;
import com.xyra.schemecraft.exception.DAOException;
import com.xyra.schemecraft.exception.ServiceException;
import com.xyra.schemecraft.exception.UnauthorizedActionException;
import com.xyra.schemecraft.model.*;
import com.xyra.schemecraft.exception.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.sql.Connection;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.util.*;

public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    private final EntityValidator entityValidator;
    private final ProductDAO productDAO;
    private final ProductImageDAO productImageDAO;
    private final ProductVersionDAO productVersionDAO;
    private final ProductCategoryDAO productCategoryDAO;
    private final AccountProductDAO accountProductDAO;

    public ProductService(){
        this.productDAO = new ProductDAO();
        this.entityValidator = new EntityValidator();
        this.productImageDAO = new ProductImageDAO();
        this.productVersionDAO = new ProductVersionDAO();
        this.productCategoryDAO = new ProductCategoryDAO();
        this.accountProductDAO = new AccountProductDAO();
    }

    public ProductBean getProductById(String rawProductId) {
        String productId = rawProductId == null ? null : rawProductId.trim();
        if(productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId cannot be null or blank");
        }

        try(Connection conn = ConnectionPool.getConnection()) {
            return productDAO.findById(conn, productId).orElseThrow(() -> {
                logger.error("Product not found for ID: {}", productId);
                return new EntityNotFoundException("Product not found for ID: " + productId,
                        EntityNotFoundException.EntityType.PRODUCT);
            });
        } catch (SQLException e) {
            throw new ServiceException("Error while fetching product by ID", e);
        } catch (DAOException e) {
            throw new EntityNotFoundException("Product not found for ID: "
                    + productId, EntityNotFoundException.EntityType.PRODUCT);
        }
    }

    public List<ProductBean> searchProducts(ProductSearchCriteria criteria) {
        if(criteria == null) {
            throw new IllegalArgumentException("Search criteria cannot be null");
        }
        try(Connection conn = ConnectionPool.getConnection()) {
            return productDAO.searchProducts(conn, criteria);
        } catch (SQLException e) {
            throw new ServiceException("Error while searching products", e);
        } catch (DAOException e) {
            throw new EntityNotFoundException("Product not found for search criteria"
                    , EntityNotFoundException.EntityType.PRODUCT);
        }
    }

    public ProductBean createProduct(ProductRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Product request cannot be null");
        }

        String accountId = request.accountId() == null ? null : request.accountId().trim();
        String currencyId = request.currencyId() == null ? null : request.currencyId().trim();

        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account ID cannot be null or blank");
        }

        if (currencyId == null || currencyId.isBlank()) {
            throw new IllegalArgumentException("Currency ID cannot be null or blank");
        }
        try(Connection conn = ConnectionPool.getConnection()){
            try  {
                conn.setAutoCommit(false);
                entityValidator.validateActiveAccount(conn, accountId);
                entityValidator.validateActiveCurrency(conn, currencyId);

                BigDecimal discount = request.discount() != null ? request.discount() : BigDecimal.ZERO;

                ProductBean productBean = new ProductBean();
                productBean.setProductId(UUID.randomUUID().toString());
                productBean.setAccountId(accountId);
                productBean.setCurrencyId(currencyId);
                productBean.setProductName(request.productName());
                productBean.setDescription(request.description());
                productBean.setPrice(request.price());
                productBean.setDiscount(discount);
                productBean.setStockQuantity(request.stockQuantity());
                productBean.setActive(true);

                productDAO.insert(conn, productBean);
                accountProductDAO.insert(conn, new AccountProductBean(accountId, productBean.getProductId()));
                conn.commit();
                logger.info("Product created with ID: {} by account {}", productBean.getProductId(), accountId);
                return productBean;

            } catch (SQLException | DAOException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("Failed to rollback transaction for account {}: {}"
                            , accountId, rollbackEx.getMessage(), rollbackEx);
                    throw new ServiceException("Error while creating product and failed to rollback", rollbackEx);
                }
                logger.error("Database error while creating product for account {}: {}", accountId, e.getMessage(), e);
                throw new ServiceException("Error while creating product", e);
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    logger.error("Failed to reset auto-commit for account {}: {}", accountId, e.getMessage(), e);
                }
            }
        }catch (SQLException e) {
            logger.error("Database connection error while creating product for account {}: {}", accountId, e.getMessage(), e);
            throw new ServiceException("Database connection error while creating product", e);
        }
    }

    public void updateProduct(String rawProductId, ProductRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Product request cannot be null");
        }

        String productId = rawProductId == null ? null : rawProductId.trim();
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be null or blank");
        }

        String requestingAccountId = request.accountId() == null ? null : request.accountId().trim();
        if (requestingAccountId == null || requestingAccountId.isBlank()) {
            throw new IllegalArgumentException("Account ID cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            ProductBean existingProduct = entityValidator.validateActiveProduct(conn, productId);

            if (!existingProduct.getAccountId().equals(requestingAccountId)) {
                logger.warn("Account {} attempted to update Product {} owned by {}",
                        requestingAccountId, productId, existingProduct.getAccountId());
                throw new UnauthorizedActionException("Account does not own this product");
            }

            if (request.currencyId() != null && !request.currencyId().isBlank()) {
                entityValidator.validateActiveCurrency(conn, request.currencyId());
                existingProduct.setCurrencyId(request.currencyId());
            }

            if (request.productName() != null && !request.productName().isBlank()) {
                existingProduct.setProductName(request.productName());
            }

            if (request.description() != null) {
                existingProduct.setDescription(request.description().isBlank() ? null : request.description());
            }

            if (request.price() != null) {
                existingProduct.setPrice(request.price());
            }

            if (request.discount() != null) {
                existingProduct.setDiscount(request.discount());
            }

            if (request.stockQuantity() != null && !request.unlimitedStock()) {
                existingProduct.setStockQuantity(request.stockQuantity());
            }

            boolean check = productDAO.update(conn, productId, existingProduct);

            if(check) {
                logger.info("Product {} successfully updated by account {}", productId, requestingAccountId);
            } else {
                logger.warn("Product {} update failed for account {}", productId, requestingAccountId);
                throw new ServiceException("Failed to update product");
            }
        } catch (SQLException | DAOException e) {
            logger.error("Database error while updating Product: {}", productId, e);
            throw new ServiceException("Error while updating product", e);
        }
    }

    public void deactivateProduct(String rawProductId) {
        String id = rawProductId == null ? null : rawProductId.trim();
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            boolean deactivated = productDAO.deactivate(conn, id);

            if (!deactivated) {
                logger.warn("Deactivation issued for non-existent Product ID: {}", id);
                throw new EntityNotFoundException("Product not found for ID: " + id,
                        EntityNotFoundException.EntityType.PRODUCT);
            }

            logger.info("Product {} successfully deactivated", id);

        } catch (SQLException | DAOException e) {
            logger.error("Database error while deactivating Product: {}", id, e);
            throw new ServiceException("Error while deactivating product", e);
        }
    }

    public List<ProductImageBean> listImages(String rawProductId) {
        String productId = rawProductId == null ? null : rawProductId.trim();
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            return productImageDAO.findAllByProductId(conn, productId);
        } catch (SQLException | DAOException e) {
            logger.error("Database error while listing images for Product: {}", productId, e);
            throw new ServiceException("Error while listing product images", e);
        }
    }

    public ProductImageBean addImage(String rawProductId, String imagePath) {
        String id = rawProductId == null ? null : rawProductId.trim();
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be null or blank");
        }

        if (imagePath == null || imagePath.isBlank()) {
            throw new IllegalArgumentException("Image path cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            entityValidator.validateActiveProduct(conn, id);

            List<ProductImageBean> existingImages = productImageDAO.findAllByProductId(conn, id);
            if (existingImages.size() >= ServiceConstants.MAX_PRODUCT_IMAGES) {
                logger.warn("Product {} reached the maximum number of images ({})", id,
                        ServiceConstants.MAX_PRODUCT_IMAGES);
                throw new ServiceException("Product already has the maximum number of images allowed ("
                        + ServiceConstants.MAX_PRODUCT_IMAGES + ")");
            }

            ProductImageBean image = new ProductImageBean(UUID.randomUUID().toString(), id, imagePath);
            productImageDAO.insert(conn, image);

            logger.info("Image {} successfully added to Product {}", image.getImageId(), id);
            return image;

        } catch (SQLException | DAOException e) {
            logger.error("Database error while adding image for Product: {}", id, e);
            throw new ServiceException("Error while adding product image", e);
        }
    }

    public void removeImage(String imageId) {
        String id = imageId == null ? null : imageId.trim();
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Image ID cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            boolean deleted = productImageDAO.delete(conn, id);

            if (!deleted) {
                logger.warn("Removal issued for non-existent Image ID: {}", id);
                throw new EntityNotFoundException("Image not found for ID: " + id,
                        EntityNotFoundException.EntityType.IMAGE);
            }

            logger.info("Image {} successfully removed", id);

        } catch (SQLException | DAOException e) {
            logger.error("Database error while removing Image: {}", id, e);
            throw new ServiceException("Error while removing product image", e);
        }
    }

    public ProductVersionBean getVersionById(String versionId) {
        String id = versionId == null ? null : versionId.trim();
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Version ID cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            return productVersionDAO.findById(conn, id)
                    .orElseThrow(() -> {
                        logger.warn("Version not found for ID: {}", id);
                        return new EntityNotFoundException("Version not found for ID: " + id,
                                EntityNotFoundException.EntityType.PRODUCT_VERSION);
                    });
        } catch (SQLException | DAOException e) {
            logger.error("Database error while fetching Version: {}", id, e);
            throw new ServiceException("Error while fetching product version", e);
        }
    }

    public List<ProductVersionBean> listVersions(String productId) {
        String id = productId == null ? null : productId.trim();
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            entityValidator.validateProduct(conn, id);
            return productVersionDAO.findAllByProductId(conn, id);
        } catch (SQLException | DAOException e) {
            logger.error("Database error while listing versions for Product: {}", id, e);
            throw new ServiceException("Error while listing product versions", e);
        }
    }

    public ProductVersionBean publishVersion(ProductVersionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Version request cannot be null");
        }

        String productId = request.productId() == null ? null : request.productId().trim();
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            entityValidator.validateActiveProduct(conn, productId);

            ProductVersionBean version = new ProductVersionBean();
            version.setVersionId(UUID.randomUUID().toString());
            version.setProductId(productId);
            version.setChangelog(request.changelog());
            version.setFilePath(request.filePath());
            version.setMinecraftVersion(request.minecraftVersion());
            version.setVersion(request.version());

            productVersionDAO.insert(conn, version);

            logger.info("Version {} successfully published for Product {}", version.getVersionId(), productId);
            return version;

        } catch (SQLException | DAOException e) {
            logger.error("Database error while publishing version for Product: {}", productId, e);
            throw new ServiceException("Error while publishing product version", e);
        }
    }

    public void registerDownload(String versionId) {
        String id = versionId == null ? null : versionId.trim();
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Version ID cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            boolean incremented = productVersionDAO.incrementDownloadCount(conn, id);

            if (!incremented) {
                logger.warn("Download registration issued for non-existent Version ID: {}", id);
                throw new EntityNotFoundException("Version not found for ID: " + id,
                        EntityNotFoundException.EntityType.PRODUCT_VERSION);
            }

            logger.info("Download registered for Version: {}", id);

        } catch (SQLException | DAOException e) {
            logger.error("Database error while registering download for Version: {}", id, e);
            throw new ServiceException("Error while registering download", e);
        }
    }

    public void assignCategory(String rawProductId, String rawCategoryId) {
        String productId = rawProductId == null ? null : rawProductId.trim();
        String categoryId = rawCategoryId == null ? null : rawCategoryId.trim();

        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be null or blank");
        }
        if (categoryId == null || categoryId.isBlank()) {
            throw new IllegalArgumentException("Category ID cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            entityValidator.validateActiveProduct(conn, productId);
            entityValidator.validateActiveCategory(conn, categoryId);

            ProductCategoryBean association = new ProductCategoryBean(productId, categoryId);
            productCategoryDAO.insert(conn, association);

            logger.info("Category {} successfully assigned to Product {}", categoryId, productId);

        } catch (SQLException | DAOException e) {
            logger.error("Database error while assigning Category {} to Product {}", categoryId, productId, e);
            throw new ServiceException("Error while assigning category to product", e);
        }
    }

    public void removeCategory(String rawProductId, String rawCategoryId) {
        String productId = rawProductId == null ? null : rawProductId.trim();
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be null or blank");
        }

        String categoryId = rawCategoryId == null ? null : rawCategoryId.trim();
        if (categoryId == null || categoryId.isBlank()) {
            throw new IllegalArgumentException("Category ID cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            entityValidator.validateActiveProduct(conn, productId);
            entityValidator.validateActiveCategory(conn, categoryId);

            boolean removed = productCategoryDAO.delete(conn, productId, categoryId);
            if (!removed) {
                logger.warn("Attempted to remove non-existent link between Product {} and Category {}",
                        productId, categoryId);
                throw new EntityNotFoundException("Product " + productId + " is not assigned to Category " + categoryId);
            }

            logger.info("Category {} successfully removed from Product {}", categoryId, productId);

        } catch (SQLException | DAOException e) {
            logger.error("Database error while removing Category {} from Product {}", categoryId, productId, e);
            throw new ServiceException("Error while removing category from product", e);
        }
    }

    public boolean ownsProduct(String rawAccountId, String rawProductId) {
        String accountId = rawAccountId == null ? null : rawAccountId.trim();
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account ID cannot be null or blank");
        }

        String productId = rawProductId == null ? null : rawProductId.trim();
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be null or blank");
        }
        try (Connection conn = ConnectionPool.getConnection()) {
            return accountProductDAO.findById(conn, accountId, productId).isPresent();
        } catch (SQLException | DAOException e) {
            logger.error("Database error while checking ownership of Product {} for Account {}", productId, accountId, e);
            throw new ServiceException("Error while checking product ownership", e);
        }
    }

    public List<OwnedProductItem> listOwnedProducts(String rawAccountId) {
        String accountId = rawAccountId == null ? null : rawAccountId.trim();
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account ID cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            Map<String, OwnedProductItem> ownedProducts = new LinkedHashMap<>();
            List<ProductBean> createdProducts = productDAO.findAllByAccountId(conn, accountId);
            for (ProductBean product : createdProducts) {
                ownedProducts.put(product.getProductId(),
                        new OwnedProductItem(product, accountId, false, true));
            }

            List<AccountProductBean> purchasedLinks = accountProductDAO.findAllByAccountId(conn, accountId);
            for (AccountProductBean link : purchasedLinks) {
                Optional<ProductBean> product = productDAO.findById(conn, link.getProductId());
                if (product.isPresent()) {
                    ownedProducts.put(link.getProductId(),
                            new OwnedProductItem(product.get(), accountId, true, false));
                } else {
                    logger.warn("Account {} has a purchase link to Product {} which no longer exists",
                            accountId, link.getProductId());
                }
            }

            return List.copyOf(ownedProducts.values());

        } catch (SQLException | DAOException e) {
            logger.error("Database error while listing owned products for Account {}", accountId, e);
            throw new ServiceException("Error while listing owned products", e);
        }
    }
}