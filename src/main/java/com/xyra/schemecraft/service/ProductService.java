package com.xyra.schemecraft.service;

import com.xyra.schemecraft.connection.ConnectionPool;
import com.xyra.schemecraft.constant.ServiceConstants;
import com.xyra.schemecraft.dao.*;
import com.xyra.schemecraft.dto.*;
import com.xyra.schemecraft.exception.*;
import com.xyra.schemecraft.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    private final EntityValidator entityValidator;
    private final ProductDAO productDAO;
    private final ProductImageDAO productImageDAO;
    private final ProductVersionDAO productVersionDAO;
    private final ProductCategoryDAO productCategoryDAO;
    private final AccountProductDAO accountProductDAO;
    private final CategoryDAO categoryDAO;
    private final CurrencyDAO currencyDAO;

    public ProductService(){
        this.productDAO = new ProductDAO();
        this.entityValidator = new EntityValidator();
        this.productImageDAO = new ProductImageDAO();
        this.productVersionDAO = new ProductVersionDAO();
        this.productCategoryDAO = new ProductCategoryDAO();
        this.accountProductDAO = new AccountProductDAO();
        this.categoryDAO = new CategoryDAO();
        this.currencyDAO = new CurrencyDAO();
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

    public List<ProductBean> searchProducts(ProductSearchCriteria criteria, Boolean activeFilter) {
        if(criteria == null) {
            throw new IllegalArgumentException("Search criteria cannot be null");
        }
        try(Connection conn = ConnectionPool.getConnection()) {
            return productDAO.searchProductsForAdmin(conn, criteria, activeFilter);
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

    public void activateProduct(String rawProductId) {
        String id = rawProductId == null ? null : rawProductId.trim();
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            boolean activated = productDAO.activate(conn, id);

            if (!activated) {
                logger.warn("Activation issued for non-existent Product ID: {}", id);
                throw new EntityNotFoundException("Product not found for ID: " + id,
                        EntityNotFoundException.EntityType.PRODUCT);
            }

            logger.info("Product {} successfully activated", id);

        } catch (SQLException | DAOException e) {
            logger.error("Database error while activating Product: {}", id, e);
            throw new ServiceException("Error while activating product", e);
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

    public ProductImageBean addImage(String rawProductId, String imagePath, int displayOrder) {
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

            ProductImageBean image = new ProductImageBean(UUID.randomUUID().toString(), id, imagePath, displayOrder);
            productImageDAO.insert(conn, image);

            logger.info("Image {} successfully added to Product {}", image.getImageId(), id);
            return image;

        } catch (SQLException | DAOException e) {
            logger.error("Database error while adding image for Product: {}", id, e);
            throw new ServiceException("Error while adding product image", e);
        }
    }

    public ProductImageBean addImage(String rawProductId, String imagePath) {
        String id = rawProductId == null ? null : rawProductId.trim();
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            List<ProductImageBean> existingImages = productImageDAO.findAllByProductId(conn, id);

            int autoDisplayOrder = existingImages.size();

            return addImage(id, imagePath, autoDisplayOrder);

        } catch (SQLException | DAOException e) {
            logger.error("Database error while calculating order for Product: {}", id, e);
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

    public List<CategoryBean> listCategories(String rawProductId) {
        String productId = rawProductId == null ? null : rawProductId.trim();

        try (Connection conn = ConnectionPool.getConnection()) {
            return listCategories_(conn, productId);
        } catch (SQLException | DAOException e) {
            logger.error("Error retrieving categories for product {}", productId, e);
            throw new ServiceException("Unable to retrieve product categories", e);
        }
    }


    private List<CategoryBean> listCategories_(Connection conn, String rawProductId) {
        String productId = rawProductId == null ? null : rawProductId.trim();
        try {
            return categoryDAO.findCategoriesByProductId(conn, productId);
        } catch (DAOException e) {
            logger.error("Error retrieving categories for product {}", productId, e);
            throw new ServiceException("Unable to retrieve product categories", e);
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

    /**
     * Creates a fully published product in a single atomic transaction: base product data,
     * category assignments, gallery images, and the first downloadable version.
     * If any step fails, the entire operation is rolled back and nothing is persisted.
     *
     * @param fullRequest Aggregated request containing product data, categories, images, and version data
     * @return The fully created ProductBean
     * @throws IllegalArgumentException if any required argument is null, empty, or invalid
     * @throws ServiceException         if any persistence step fails; the whole transaction is rolled back
     */
    public ProductBean createFullProduct(ProductFullRequest fullRequest) {

        if (fullRequest == null) {
            throw new IllegalArgumentException("Full product request cannot be null");
        }

        ProductRequest productRequest = fullRequest.productRequest();
        List<String> categoryIds = fullRequest.categoryIds();
        List<String> imagePaths = fullRequest.imagePaths();
        ProductVersionRequest versionRequest = fullRequest.versionRequest();

        String accountId = productRequest.accountId() == null ? null : productRequest.accountId().trim();
        String currencyId = productRequest.currencyId() == null ? null : productRequest.currencyId().trim();

        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account ID cannot be null or blank");
        }
        if (currencyId == null || currencyId.isBlank()) {
            throw new IllegalArgumentException("Currency ID cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            try {
                conn.setAutoCommit(false);

                // Step 1: validate account and currency
                entityValidator.validateActiveAccount(conn, accountId);
                entityValidator.validateActiveCurrency(conn, currencyId);

                // Step 2: create the base product
                BigDecimal discount = productRequest.discount() != null ? productRequest.discount() : BigDecimal.ZERO;

                ProductBean productBean = new ProductBean();
                productBean.setProductId(UUID.randomUUID().toString());
                productBean.setAccountId(accountId);
                productBean.setCurrencyId(currencyId);
                productBean.setProductName(productRequest.productName());
                productBean.setDescription(productRequest.description());
                productBean.setPrice(productRequest.price());
                productBean.setDiscount(discount);
                productBean.setStockQuantity(productRequest.stockQuantity());
                productBean.setTotalDownloads(0);
                productBean.setTotalReviews(0);
                productBean.setAverageRating(BigDecimal.ZERO);
                productBean.setActive(true);

                productDAO.insert(conn, productBean);
                String productId = productBean.getProductId();

                // Step 3: assign categories
                for (String rawCategoryId : categoryIds) {
                    String categoryId = rawCategoryId == null ? null : rawCategoryId.trim();
                    if (categoryId == null || categoryId.isBlank()) {
                        throw new IllegalArgumentException("Category ID cannot be null or blank");
                    }
                    entityValidator.validateActiveCategory(conn, categoryId);
                    productCategoryDAO.insert(conn, new ProductCategoryBean(categoryId, productId));
                }

                // Step 4: add gallery images, preserving list order as display order
                int displayOrder = 0;
                for (String rawImagePath : imagePaths) {
                    String imagePath = rawImagePath == null ? null : rawImagePath.trim();
                    if (imagePath == null || imagePath.isBlank()) {
                        throw new IllegalArgumentException("Image path cannot be null or blank");
                    }
                    ProductImageBean image = new ProductImageBean(
                            UUID.randomUUID().toString(), productId, imagePath, displayOrder);
                    productImageDAO.insert(conn, image);
                    displayOrder++;
                }

                // Step 5: publish the first version, resolving the real productId now that it exists
                ProductVersionBean versionBean = new ProductVersionBean();
                versionBean.setVersionId(UUID.randomUUID().toString());
                versionBean.setProductId(productId);
                versionBean.setChangelog(versionRequest.changelog());
                versionBean.setFilePath(versionRequest.filePath());
                versionBean.setMinecraftVersion(versionRequest.minecraftVersion());
                versionBean.setVersion(versionRequest.version());

                productVersionDAO.insert(conn, versionBean);

                // Step 6: link the product to its creator's owned products
                accountProductDAO.insert(conn, new AccountProductBean(accountId, productId));

                conn.commit();
                logger.info("Full product successfully created with ID: {} by account {} " +
                                "({} categories, {} images, version {})",
                        productId, accountId, categoryIds.size(), imagePaths.size(), versionBean.getVersion());

                return productBean;

            } catch (SQLException | DAOException | IllegalArgumentException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("Failed to rollback full product creation transaction for account {}: {}",
                            accountId, rollbackEx.getMessage(), rollbackEx);
                    throw new ServiceException("Error while creating full product and failed to rollback", rollbackEx);
                }
                logger.error("Error while creating full product for account {}: {}", accountId, e.getMessage(), e);
                throw new ServiceException("Error while creating full product", e);
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    logger.error("Failed to reset auto-commit for account {}: {}", accountId, e.getMessage(), e);
                }
            }
        } catch (SQLException e) {
            logger.error("Database connection error while creating full product for account {}: {}",
                    accountId, e.getMessage(), e);
            throw new ServiceException("Database connection error while creating full product", e);
        }
    }


    /**
     * Updates an existing product in a single atomic operation: base product data,
     * categories (full sync), images (full sync), and versions (full sync).
     *
     * @param request Full update request containing all product data
     * @throws ServiceException if a database or system error occurs
     * @throws EntityNotFoundException if the product or any referenced entity does not exist
     * @throws IllegalArgumentException if validation fails
     */
    public void updateFullProduct(ProductFullUpdateRequest request)
            throws ServiceException, EntityNotFoundException, IllegalArgumentException {

        if (request == null) {
            throw new IllegalArgumentException("Product full update request cannot be null");
        }

        String productId = request.productId().trim();
        ProductRequest productRequest = request.product();
        List<String> categoryIds = request.categoryIds();
        List<String> imagePaths = request.imagePaths();
        List<VersionUpdateRequest> versionsRequest = request.versions();

        String currencyId = productRequest.currencyId() == null ? null : productRequest.currencyId().trim();

        if (currencyId == null || currencyId.isBlank()) {
            throw new IllegalArgumentException("Currency ID cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            try {
                conn.setAutoCommit(false);

                ProductBean existingProduct = entityValidator.rawValidateProduct(conn, productId);

                // Step 2: validate referenced entities (currency, categories, and potentially accounts)
                // Note: account ID is not being changed, so we skip account validation
                entityValidator.validateActiveCurrency(conn, currencyId);
                for (String categoryId : categoryIds) {
                    String trimmed = categoryId == null ? null : categoryId.trim();
                    if (trimmed == null || trimmed.isBlank()) {
                        throw new IllegalArgumentException("Category ID cannot be null or blank");
                    }
                    entityValidator.validateActiveCategory(conn, trimmed);
                }

                // Step 3: update base product data
                BigDecimal discount = productRequest.discount() != null ? productRequest.discount() : BigDecimal.ZERO;

                ProductBean updatedProduct = new ProductBean();
                updatedProduct.setProductId(productId);
                updatedProduct.setAccountId(existingProduct.getAccountId()); // preserve original owner
                updatedProduct.setCurrencyId(currencyId);
                updatedProduct.setProductName(productRequest.productName());
                updatedProduct.setDescription(productRequest.description());
                updatedProduct.setPrice(productRequest.price());
                updatedProduct.setDiscount(discount);
                updatedProduct.setStockQuantity(productRequest.stockQuantity());
                // Preserve aggregated fields
                updatedProduct.setTotalDownloads(existingProduct.getTotalDownloads());
                updatedProduct.setTotalReviews(existingProduct.getTotalReviews());
                updatedProduct.setAverageRating(existingProduct.getAverageRating());
                updatedProduct.setActive(existingProduct.isActive());

                productDAO.update(conn, updatedProduct);

                // Step 4: full sync of categories (delete all + re-insert)
                productCategoryDAO.deleteAllByProductId(conn, productId);
                for (String categoryId : categoryIds) {
                    String trimmed = categoryId.trim();
                    try {
                        productCategoryDAO.insert(conn, new ProductCategoryBean(trimmed, productId));
                    } catch (DuplicateEntityException e) {
                        logger.debug("Category {} already linked to product {} (likely via parent chain expansion), skipping",
                                trimmed, productId);
                    }
                }

                // Step 5: full sync of images (delete all + re-insert with display order)
                productImageDAO.deleteAllByProductId(conn, productId);
                int displayOrder = 0;
                for (String imagePath : imagePaths) {
                    String trimmed = imagePath.trim();
                    if (trimmed.isBlank()) {
                        throw new IllegalArgumentException("Image path cannot be blank");
                    }
                    ProductImageBean image = new ProductImageBean(
                            UUID.randomUUID().toString(), productId, trimmed, displayOrder);
                    productImageDAO.insert(conn, image);
                    displayOrder++;
                }

                // Step 6: full sync of versions (update/insert/delete)
                List<ProductVersionBean> existingVersions = productVersionDAO.findAllByProductId(conn, productId);

                // Build a map of existing versions by ID for quick lookup
                Map<String, ProductVersionBean> existingVersionMap = existingVersions.stream()
                        .collect(Collectors.toMap(
                                ProductVersionBean::getVersionId,
                                v -> v,
                                (v1, v2) -> v1 // In case of duplicates, keep the first
                        ));

                // Track which version IDs are kept (to identify deletions)
                Set<String> keptVersionIds = new HashSet<>();

                // Process each version from the request
                for (VersionUpdateRequest versionRequestItem : versionsRequest) {
                    String version = versionRequestItem.version() != null ? versionRequestItem.version().trim() : null;
                    String minecraftVersion = versionRequestItem.minecraftVersion() != null
                            ? versionRequestItem.minecraftVersion().trim()
                            : null;
                    String filePath = versionRequestItem.filePath() != null
                            ? versionRequestItem.filePath().trim()
                            : null;
                    String changelog = versionRequestItem.changelog() != null
                            ? versionRequestItem.changelog().trim()
                            : null;

                    if (version == null || version.isBlank()) {
                        throw new IllegalArgumentException("Version string cannot be null or blank");
                    }
                    if (filePath == null || filePath.isBlank()) {
                        throw new IllegalArgumentException("File path cannot be null or blank");
                    }

                    String versionId = versionRequestItem.versionId() != null
                            ? versionRequestItem.versionId().trim()
                            : null;

                    if (versionId == null || versionId.isBlank()) {
                        // Step 6a: new version (insert)
                        ProductVersionBean newVersion = new ProductVersionBean();
                        newVersion.setVersionId(UUID.randomUUID().toString());
                        newVersion.setProductId(productId);
                        newVersion.setVersion(version);
                        newVersion.setMinecraftVersion(minecraftVersion);
                        newVersion.setFilePath(filePath);
                        newVersion.setChangelog(changelog);
                        newVersion.setDownloadCount(0);

                        productVersionDAO.insert(conn, newVersion);
                        // No need to track in keptVersionIds since it wasn't existing
                    } else {
                        // Step 6b: existing version (update)
                        ProductVersionBean existingVersion = existingVersionMap.get(versionId);
                        if (existingVersion == null) {
                            throw new EntityNotFoundException(
                                    "Version not found with ID: " + versionId + " for product: " + productId
                            );
                        }

                        existingVersion.setVersion(version);
                        existingVersion.setMinecraftVersion(minecraftVersion);
                        existingVersion.setFilePath(filePath);
                        existingVersion.setChangelog(changelog);
                        // download_count and created_at remain unchanged

                        productVersionDAO.update(conn, existingVersion);
                        keptVersionIds.add(versionId);
                    }
                }

                // Step 6c: delete versions that were not kept
                if (versionsRequest.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Cannot remove all versions of product: " + productId + " — at least one version is required"
                    );
                }

                boolean hasDeletedVersionWithDownloads = false;
                for (ProductVersionBean existingVersion : existingVersions) {
                    if (!keptVersionIds.contains(existingVersion.getVersionId())) {
                        // If version has downloads, track for total_downloads recalculation
                        if (existingVersion.getDownloadCount() > 0) {
                            hasDeletedVersionWithDownloads = true;
                        }

                        productVersionDAO.delete(conn, existingVersion.getVersionId());
                    }
                }

                // Step 7: the account_product association remains unchanged
                // (ownership is preserved, admin is not modifying it)

                conn.commit();

                logger.info("Product successfully updated with ID: {} ({}. categories, {} images, {} versions managed)",
                        productId, categoryIds.size(), imagePaths.size(), versionsRequest.size());

            } catch (SQLException | DAOException | IllegalArgumentException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("Failed to rollback full product update transaction for product {}: {}",
                            productId, rollbackEx.getMessage(), rollbackEx);
                    throw new ServiceException("Error while updating full product and failed to rollback", rollbackEx);
                }
                logger.error("Error while updating full product {}: {}", productId, e.getMessage(), e);
                throw e;
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    logger.error("Failed to reset auto-commit for product {}: {}", productId, e.getMessage(), e);
                }
            }
        } catch (SQLException e) {
            logger.error("Database connection error while updating full product {}: {}",
                    productId, e.getMessage(), e);
            throw new ServiceException("Database connection error while updating full product", e);
        }
    }


    /**
     * Retrieves the Currency associated with a given Product, resolved via a single
     * JOIN query rather than two separate lookups.
     *
     * @param rawProductId ID of the product whose currency is being requested
     * @return The CurrencyBean linked to the product
     * @throws IllegalArgumentException if the productId is null or blank
     * @throws EntityNotFoundException  if no product exists for the given ID
     * @throws ServiceException         if a database error occurs
     */
    public CurrencyBean getProductCurrency(String rawProductId) {
        String productId = rawProductId == null ? null : rawProductId.trim();
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            return currencyDAO.findByProductId(conn, productId).orElseThrow(() -> {
                logger.warn("Currency lookup failed: no Product found for ID: {}", productId);
                return new EntityNotFoundException("Product not found for ID: " + productId,
                        EntityNotFoundException.EntityType.PRODUCT);
            });
        } catch (SQLException | DAOException e) {
            logger.error("Database error while fetching currency for Product: {}", productId, e);
            throw new ServiceException("Error while fetching product currency", e);
        }
    }


    /**
     * Retrieves a lightweight list of active product suggestions matching the given keyword,
     * intended for search-bar autocomplete. Enriches each match with its cover image path
     * (first gallery image) and returns minimal DTOs decoupled from the full product data.
     *
     * @param rawKeyword Search term to match against the product name
     * @return List of matching product suggestions, ordered by name, capped at
     *         {@link ServiceConstants#MAX_PRODUCT_SUGGESTIONS} results; empty if the keyword is blank
     * @throws ServiceException if a database error or DAO failure occurs
     */
    public List<ProductSuggestionDTO> suggestProducts(String rawKeyword) {
        String keyword = rawKeyword == null ? null : rawKeyword.trim();
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            List<ProductBean> matches = productDAO.suggestByKeyword(conn, keyword, ServiceConstants.MAX_PRODUCT_SUGGESTIONS);

            List<ProductSuggestionDTO> suggestions = new ArrayList<>();
            for (ProductBean product : matches) {
                String coverImagePath = productImageDAO.findFirstImageByProductId(conn, product.getProductId());
                suggestions.add(new ProductSuggestionDTO(
                        product.getProductId(),
                        product.getProductName(),
                        product.getDescription(),
                        coverImagePath
                ));
            }

            return suggestions;

        } catch (SQLException | DAOException e) {
            logger.error("Database error while fetching product suggestions for keyword: {}", keyword, e);
            throw new ServiceException("Error while fetching product suggestions", e);
        }
    }

    /**
     * Retrieves complete product data including categories, images, and versions.
     *
     * @param productId the product ID
     * @return ProductFullDTO containing all product data
     * @throws EntityNotFoundException if product does not exist
     * @throws ServiceException if a database error occurs
     */
    public ProductFullDTO getProductFull(String productId)
            throws ServiceException, EntityNotFoundException {

        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            try {
                conn.setAutoCommit(false);

                // Step 1: Retrieve product
                ProductBean product = entityValidator.rawValidateProduct(conn, productId);

                // Step 2: Retrieve categories
                List<CategoryBean> categories = listCategories_(conn, productId);
                if (categories == null) {
                    categories = new ArrayList<>();
                }

                // Step 3: Retrieve images
                List<ProductImageBean> images = productImageDAO.findAllByProductId(conn, productId);
                List<String> imagePaths = new ArrayList<>();
                if (images != null) {
                    // Sort by display_order (should already be sorted by DAO, but just in case)
                    images.sort((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()));
                    for (ProductImageBean image : images) {
                        imagePaths.add(image.getImagePath());
                    }
                }

                // Step 4: Retrieve versions
                List<ProductVersionBean> versions = productVersionDAO.findAllByProductId(conn, productId);
                if (versions == null) {
                    versions = new ArrayList<>();
                }

                conn.commit();

                // Build and return the full bean
                return new ProductFullDTO(product, categories, imagePaths, versions);

            } catch (SQLException | DAOException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("Failed to rollback getProductFull transaction: {}", rollbackEx.getMessage());
                }
                logger.error("Error retrieving full product data for ID {}: {}", productId, e.getMessage(), e);
                throw new ServiceException("Error retrieving product data", e);
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    logger.error("Failed to reset auto-commit: {}", e.getMessage());
                }
            }
        } catch (SQLException e) {
            logger.error("Database connection error while retrieving product {}: {}", productId, e.getMessage());
            throw new ServiceException("Database connection error", e);
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
                if (product.isActive()) {
                    String image = productImageDAO.findFirstImageByProductId(conn, product.getProductId());
                    ownedProducts.put(product.getProductId(),
                            new OwnedProductItem(product, accountId, false, true, image));
                }

            }

            List<AccountProductBean> purchasedLinks = accountProductDAO.findAllByAccountId(conn, accountId);
            for (AccountProductBean link : purchasedLinks) {
                Optional<ProductBean> product = productDAO.findById(conn, link.getProductId());
                if (product.isPresent()) {
                    if (product.get().isActive()) {
                        String image = productImageDAO.findFirstImageByProductId(conn, product.get().getProductId());
                        ownedProducts.put(link.getProductId(),
                                new OwnedProductItem(product.get(), accountId, true, false, image));
                    }
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
