package com.xyra.schemecraft.controller;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import com.xyra.schemecraft.dto.*;
import com.xyra.schemecraft.exception.*;
import com.xyra.schemecraft.model.*;
import com.xyra.schemecraft.service.*;
import com.xyra.schemecraft.util.FileUploadUtils;
import com.xyra.schemecraft.util.JsonUtils;
import com.xyra.schemecraft.util.ServletUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet(name = "ProductServlet", urlPatterns = {"/product/*"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2,
        maxFileSize = 1024 * 1024 * 10,
        maxRequestSize = 1024 * 1024 * 50
)
public class ProductServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ProductServlet.class);

    private ProductService productService;
    private CategoryService categoryService;
    private OrderService orderService;

    public ProductServlet() {
        super();
    }

    public ProductServlet(ProductService productService, CategoryService categoryService, OrderService orderService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderService = orderService;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        if (this.productService == null) {
            this.productService = new ProductService();
        }
        if (this.categoryService == null) {
            this.categoryService = new CategoryService();
        }
        if (this.orderService == null) {
            this.orderService = new OrderService();
        }
        logger.info("ProductServlet successfully initialized.");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        configureEncoding(req, resp);

        String action = getActionPath(req);

        switch (action) {
            case "", "/", "/catalog" -> showCatalog(req, resp);
            case "/search", "/search/" -> handleSearchProducts(req, resp);
            case "/detail", "/get" -> handleGetProductDetail(req, resp);
            case "/images" -> handleListImages(req, resp);
            case "/versions" -> handleListVersions(req, resp);
            case "/version" -> handleGetVersion(req, resp);
            case "/check-ownership" -> handleCheckOwnership(req, resp);
            case "/owned" -> handleListOwnedProducts(req, resp);
            case "/download" -> handleDownloadVersion(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND, "The requested resource was not found.");
        }
    }

    private void showCatalog(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            List<CategoryBean> categories = categoryService.listAllCategories();

            req.setAttribute("categories", categories);

        } catch (ServiceException e) {
            logger.error("Unable to load categories for the catalog.", e);
        }

        req.getRequestDispatcher("/WEB-INF/catalog/catalog.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        configureEncoding(req, resp);

        String action = getActionPath(req);

        switch (action) {
            case "/register-download" -> handleRegisterDownload(req, resp);
            case "/buy" -> handleBuyNow(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND, "The requested resource was not found.");
        }
    }

    // =========================================================================
    // GET HANDLERS
    // =========================================================================

    private void handleGetProductDetail(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String productId = req.getParameter("id");
        String format = req.getParameter("format");

        try {
            ProductBean product = productService.getProductById(productId);

            if (product == null || !product.isActive()) {
                throw new EntityNotFoundException("Product not found or inactive.");
            }

            List<ProductImageBean> images = new ArrayList<>();
            try {
                images = productService.listImages(productId);
            } catch (Exception e) {
                logger.warn("Unable to fetch images for product ID: {}", productId, e);
            }

            AccountBean creator = null;
            try {
                AccountService accountService = new AccountService();
                creator = accountService.getAccountById(product.getAccountId());
                if (creator != null) {
                    creator.applyDefaultsIfMissing();
                }
            } catch (Exception e) {
                logger.warn("Unable to fetch creator for account ID: {}", product.getAccountId(), e);
            }

            List<CategoryBean> categories = new ArrayList<>();
            JSONArray categoriesJsonArray = new JSONArray();
            try {
                List<ProductCategoryBean> associations = categoryService.listAllCategoriesAssociated(productId);
                if (associations != null) {
                    for (ProductCategoryBean assoc : associations) {
                        CategoryBean cat = categoryService.getCategoryById(assoc.getCategoryId());
                        if (cat != null) {
                            categories.add(cat);
                            categoriesJsonArray.put(cat.getCategoryName());
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("Unable to fetch categories for product ID: {}", productId, e);
            }

            List<ProductVersionBean> versions = new ArrayList<>();
            try {
                ProductService productService = new ProductService();
                versions = productService.listVersions(productId);
            } catch (Exception e) {
                logger.warn("Unable to fetch versions for product ID: {}", productId, e);
            }

            List<ReviewView> reviews = new ArrayList<>();
            ReviewBean userReview = null;

            try {
                ReviewService reviewService = new ReviewService();
                AccountService accountService = new AccountService();

                List<ReviewBean> rawReviews = reviewService.listProductReviews(productId, 1);
                if (rawReviews != null) {
                    for (ReviewBean rev : rawReviews) {
                        AccountBean author = null;
                        try {
                            author = accountService.getAccountById(rev.getAccountId());
                            if (author != null) {
                                author.applyDefaultsIfMissing();
                            }
                        } catch (Exception ex) {
                            logger.warn("Unable to fetch author for account ID: {}", rev.getAccountId(), ex);
                        }

                        reviews.add(new ReviewView(rev, author));
                    }
                }
            } catch (Exception e) {
                logger.warn("Unable to fetch reviews for product ID: {}", productId, e);
            }

            boolean isPurchased = false;
            boolean isWishlisted = false;

            AccountBean sessionUser = getAuthenticatedAccount(req);
            if (sessionUser != null) {
                try {
                    boolean isCreator = sessionUser.getAccountId().equals(product.getAccountId());
                    boolean hasPurchased = productService.ownsProduct(sessionUser.getAccountId(), productId);

                    isPurchased = isCreator || hasPurchased;
                    FavoriteService favoriteService = new FavoriteService();
                    isWishlisted = favoriteService.isFavorite(sessionUser.getAccountId(), productId);

                    if (reviews != null && !reviews.isEmpty()) {
                        userReview = reviews.stream()
                                .map(ReviewView::getReview)
                                .filter(r -> sessionUser.getAccountId().equals(r.getAccountId()))
                                .findFirst()
                                .orElse(null);
                    }
                } catch (Exception e) {
                    logger.warn("Unable to check purchase/wishlist/review status for user: {}", sessionUser.getAccountId(), e);
                }
            }

            if ("json".equalsIgnoreCase(format) || isAjaxRequest(req)) {
                resp.setContentType("application/json");
                JSONObject jsonResponse = new JSONObject();

                JSONObject productJson = JsonUtils.serializeProduct(product);
                productJson.put("categories", categoriesJsonArray);

                JSONArray imagesJsonArray = new JSONArray();
                if (images != null) {
                    for (ProductImageBean img : images) {
                        JSONObject imgObj = new JSONObject();
                        imgObj.put("imageId", img.getImageId());
                        imgObj.put("imagePath", img.getImagePath());
                        imagesJsonArray.put(imgObj);
                    }
                }
                productJson.put("images", imagesJsonArray);
                if (!images.isEmpty()) {
                    productJson.put("coverImagePath", images.get(0).getImagePath());
                }

                if (creator != null) {
                    productJson.put("creatorName", creator.getUsername());
                    productJson.put("creatorAvatarPath", creator.getProfileImagePath());
                }

                JSONArray reviewsJsonArray = new JSONArray();
                for (ReviewView revView : reviews) {
                    ReviewBean rev = revView.getReview();
                    JSONObject revObj = new JSONObject();
                    revObj.put("accountId", rev.getAccountId());
                    revObj.put("rating", rev.getRating());
                    revObj.put("comment", rev.getComment());
                    revObj.put("isVerifiedPurchase", rev.isVerifiedPurchase());
                    revObj.put("createdAt", rev.getCreatedAt() != null ? rev.getCreatedAt().toString() : null);
                    revObj.put("authorUsername", revView.getAuthorUsername());
                    revObj.put("authorAvatar", revView.getAuthorAvatar());
                    reviewsJsonArray.put(revObj);
                }

                jsonResponse.put("success", true);
                jsonResponse.put("isPurchased", isPurchased);
                jsonResponse.put("isWishlisted", isWishlisted);
                jsonResponse.put("reviews", reviewsJsonArray);
                jsonResponse.put("product", productJson);
                resp.getWriter().print(jsonResponse.toString());

            } else {
                req.setAttribute("product", product);
                req.setAttribute("images", images);
                req.setAttribute("creator", creator);
                req.setAttribute("categories", categories);
                req.setAttribute("versions", versions);
                req.setAttribute("isPurchased", isPurchased);
                req.setAttribute("isWishlisted", isWishlisted);

                req.setAttribute("reviews", reviews);
                req.setAttribute("userReview", userReview);

                req.getRequestDispatcher("/WEB-INF/views/product-detail.jsp").forward(req, resp);
            }

        } catch (EntityNotFoundException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (ServiceException e) {
            logger.error("Error fetching product detail for ID: {}", productId, e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to fetch product details.");
        }
    }

    private void handleSearchProducts(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject jsonResponse = new JSONObject();

        try {
            ProductSearchCriteria criteria = buildSearchCriteria(req);
            logger.info("Search criteria: {}", criteria);
            List<ProductBean> products = productService.searchProducts(criteria);

            JSONArray array = new JSONArray();
            for (ProductBean product : products) {
                JSONObject productJson = JsonUtils.serializeProduct(product);

                JSONArray categoriesArray = new JSONArray();
                try {
                    List<ProductCategoryBean> associations = categoryService.listAllCategoriesAssociated(product.getProductId());

                    if (associations != null) {
                        for (ProductCategoryBean assoc : associations) {
                            CategoryBean category = categoryService.getCategoryById(assoc.getCategoryId());
                            if (category != null && category.getCategoryName() != null) {
                                categoriesArray.put(category.getCategoryName());
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Unable to retrieve categories for product ID: {}", product.getProductId(), e);
                }

                productJson.put("categories", categoriesArray);

                try {
                    List<ProductImageBean> images = productService.listImages(product.getProductId());
                    if (images != null && !images.isEmpty()) {
                        productJson.put("coverImagePath", images.get(0).getImagePath());
                    }
                } catch (Exception e) {
                    logger.warn("Unable to retrieve images for product ID: {}", product.getProductId(), e);
                }

                try {
                    AccountService accountService = new AccountService();
                    AccountBean creator = accountService.getAccountById(product.getAccountId());
                    if (creator != null) {
                        productJson.put("creatorName", creator.getUsername());
                        if (creator.getProfileImagePath() != null) {
                            productJson.put("creatorAvatarPath", creator.getProfileImagePath());
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Unable to retrieve creator info for account ID: {}", product.getAccountId(), e);
                }

                array.put(productJson);
            }

            jsonResponse.put("success", true);
            jsonResponse.put("products", array);
            out.print(jsonResponse.toString());

        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (ServiceException e) {
            logger.error("Error executing product search", e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error during product search.");
        }
    }

    private void handleListImages(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject jsonResponse = new JSONObject();

        String productId = req.getParameter("productId");

        try {
            List<ProductImageBean> images = productService.listImages(productId);
            JSONArray array = new JSONArray();

            for (ProductImageBean img : images) {
                JSONObject obj = new JSONObject();
                obj.put("imageId", img.getImageId());
                obj.put("productId", img.getProductId());
                obj.put("imagePath", img.getImagePath());
                array.put(obj);
            }

            jsonResponse.put("success", true);
            jsonResponse.put("images", array);
            out.print(jsonResponse.toString());

        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (ServiceException e) {
            logger.error("Error listing images for product: {}", productId, e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to list product images.");
        }
    }

    private void handleListVersions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject jsonResponse = new JSONObject();

        String productId = req.getParameter("productId");

        try {
            List<ProductVersionBean> versions = productService.listVersions(productId);
            JSONArray array = new JSONArray();

            for (ProductVersionBean ver : versions) {
                array.put(JsonUtils.serializeVersion(ver));
            }

            jsonResponse.put("success", true);
            jsonResponse.put("versions", array);
            out.print(jsonResponse.toString());

        } catch (IllegalArgumentException | EntityNotFoundException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (ServiceException e) {
            logger.error("Error listing versions for product: {}", productId, e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to list product versions.");
        }
    }

    private void handleGetVersion(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject jsonResponse = new JSONObject();

        String versionId = req.getParameter("versionId");

        try {
            ProductVersionBean version = productService.getVersionById(versionId);
            jsonResponse.put("success", true);
            jsonResponse.put("version", JsonUtils.serializeVersion(version));
            out.print(jsonResponse.toString());

        } catch (EntityNotFoundException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (ServiceException e) {
            logger.error("Error fetching version ID: {}", versionId, e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to fetch product version.");
        }
    }

    private void handleCheckOwnership(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AccountBean currentAccount = getAuthenticatedAccount(req);

        if (currentAccount == null) {
            JsonUtils.sendSuccess(resp, null, "ownsProduct", false);
            return;
        }

        String productId = req.getParameter("productId");

        try {
            boolean owns = productService.ownsProduct(currentAccount.getAccountId(), productId);
            JsonUtils.sendSuccess(resp, null, "ownsProduct", owns);

        } catch (IllegalArgumentException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (ServiceException e) {
            logger.error("Error checking ownership for product: {}", productId, e);
            JsonUtils.sendError(resp, "Unable to check ownership.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleListOwnedProducts(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject jsonResponse = new JSONObject();

        AccountBean currentAccount = getAuthenticatedAccount(req);
        if (currentAccount == null) {
            handleUnauthorized(req, resp);
            return;
        }

        try {
            List<OwnedProductItem> items = productService.listOwnedProducts(currentAccount.getAccountId());
            JSONArray array = new JSONArray();

            for (OwnedProductItem item : items) {
                array.put(JsonUtils.serializeOwnedProductItem(item));
            }

            jsonResponse.put("success", true);
            jsonResponse.put("ownedProducts", array);
            out.print(jsonResponse.toString());

        } catch (ServiceException e) {
            logger.error("Error listing owned products for account: {}", currentAccount.getAccountId(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to list owned products.");
        }
    }

    private void handleDownloadVersion(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        AccountBean currentAccount = getAuthenticatedAccount(req);
        if (currentAccount == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You must be logged in to download files.");
            return;
        }

        String productId = req.getParameter("productId");
        String versionId = req.getParameter("versionId");

        if (isNullOrBlank(productId) || isNullOrBlank(versionId)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters: productId and versionId.");
            return;
        }

        try {
            ProductBean product = productService.getProductById(productId);
            if (product == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Product not found.");
                return;
            }

            boolean isCreator = currentAccount.getAccountId().equals(product.getAccountId());
            boolean isFree = product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) == 0;
            boolean hasPurchased = productService.ownsProduct(currentAccount.getAccountId(), productId);

            if (!isCreator && !isFree && !hasPurchased) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not own this product.");
                return;
            }

            ProductVersionBean version = productService.getVersionById(versionId);
            if (!version.getProductId().equals(productId)) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "The requested version does not belong to this product.");
                return;
            }

            String relativePath = version.getFilePath();
            String absolutePath = getServletContext().getRealPath(relativePath);

            File file = new File(absolutePath);
            if (!file.exists() || !file.isFile()) {
                logger.error("Physical file not found on disk: {}", absolutePath);
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "The requested file is unavailable on the server.");
                return;
            }

            productService.registerDownload(versionId);

            String mimeType = getServletContext().getMimeType(file.getName());
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            resp.setContentType(mimeType);
            resp.setContentLengthLong(file.length());
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");

            try (InputStream in = new FileInputStream(file);
                 OutputStream out = resp.getOutputStream()) {

                in.transferTo(out);
                out.flush();
            }

        } catch (EntityNotFoundException e) {
            logger.warn("Version or product not found: {}", e.getMessage());
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "The requested product version was not found.");

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid input for download request: {}", e.getMessage());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());

        } catch (ServiceException e) {
            logger.error("Service failure while downloading version {} for product {}", versionId, productId, e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal error occurred during file download.");
        }
    }

    // =========================================================================
    // POST HANDLERS
    // =========================================================================

    private void handleRegisterDownload(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject jsonResponse = new JSONObject();

        String versionId = req.getParameter("versionId");

        try {
            productService.registerDownload(versionId);
            jsonResponse.put("success", true);
            jsonResponse.put("message", "Download registered successfully.");
            out.print(jsonResponse.toString());

        } catch (EntityNotFoundException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (ServiceException e) {
            logger.error("Error registering download for version: {}", versionId, e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to register download.");
        }
    }

    private void handleBuyNow(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AccountBean currentAccount = getAuthenticatedAccount(req);
        if (currentAccount == null) {
            handleUnauthorized(req, resp);
            return;
        }

        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject jsonResponse = new JSONObject();

        String productId = req.getParameter("productId");

        try {
            String transactionId = orderService.placeOrderDirect(currentAccount.getAccountId(), productId);

            jsonResponse.put("success", true);
            jsonResponse.put("message", "Order placed successfully.");
            jsonResponse.put("transactionId", transactionId);
            out.print(jsonResponse.toString());

        } catch (EntityNotFoundException e) {
            // Missing default address, payment method, or invalid product
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (DuplicateEntityException e) {
            // Already owned, or an order for this product is already being processed
            sendErrorResponse(resp, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (InsufficientStockException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (PaymentDeclinedException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_PAYMENT_REQUIRED, e.getMessage());
        } catch (ServiceException e) {
            logger.error("Error processing direct purchase for account: {}, product: {}",
                    currentAccount.getAccountId(), productId, e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to complete purchase.");
        }
    }

    // =========================================================================
    // PARSING & SERIALIZATION HELPERS
    // =========================================================================

    private ProductRequest parseProductRequest(HttpServletRequest req, String accountId) {
        String currencyId = req.getParameter("currencyId");
        String productName = req.getParameter("productName");
        String description = req.getParameter("description");

        BigDecimal price = parseBigDecimal(req.getParameter("price"));
        BigDecimal discount = parseBigDecimal(req.getParameter("discount"));
        Integer stockQuantity = parseInteger(req.getParameter("stockQuantity"));
        boolean unlimitedStock = "true".equalsIgnoreCase(req.getParameter("unlimitedStock"));

        return new ProductRequest(
                accountId,
                currencyId,
                productName,
                description,
                price,
                discount,
                stockQuantity,
                unlimitedStock
        );
    }

    private ProductSearchCriteria buildSearchCriteria(HttpServletRequest req) {
        ProductSearchCriteria criteria = new ProductSearchCriteria();

        String query = req.getParameter("query");
        if (query == null || query.isBlank()) {
            query = req.getParameter("keywords");
        }
        if (query != null && !query.isBlank()) {
            criteria.setKeywords(query.trim());
        }

        BigDecimal minPrice = parseBigDecimal(req.getParameter("minPrice"));
        if (minPrice != null) {
            criteria.setMinPrice(minPrice);
        }

        BigDecimal maxPrice = parseBigDecimal(req.getParameter("maxPrice"));
        if (maxPrice != null) {
            criteria.setMaxPrice(maxPrice);
        }

        BigDecimal minRating = parseBigDecimal(req.getParameter("minRating"));
        if (minRating != null) {
            criteria.setMinRating(minRating);
        }

        BigDecimal maxRating = parseBigDecimal(req.getParameter("maxRating"));
        if (maxRating != null) {
            criteria.setMaxRating(maxRating);
        }

        String onlyWithDiscount = req.getParameter("onlyWithDiscount");
        if (onlyWithDiscount != null && !onlyWithDiscount.isBlank()) {
            criteria.setOnlyWithDiscount("true".equalsIgnoreCase(onlyWithDiscount));
        }

        String mcVersion = req.getParameter("minecraftVersion");
        if (mcVersion != null && !mcVersion.isBlank()) {
            criteria.setMinecraftVersion(mcVersion.trim());
        }

        String sortBy = req.getParameter("sortBy");
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = req.getParameter("orderByColumn");
        }
        if (sortBy != null && !sortBy.isBlank()) {
            criteria.setOrderByColumn(sortBy.trim());
        }

        String ascending = req.getParameter("ascending");
        if (ascending != null && !ascending.isBlank()) {
            criteria.setAscending("true".equalsIgnoreCase(ascending));
        }

        Integer pageNumber = parseInteger(req.getParameter("pageNumber"));
        if (pageNumber != null) {
            criteria.setPageNumber(pageNumber);
        }

        Integer pageSize = parseInteger(req.getParameter("pageSize"));
        if (pageSize != null) {
            criteria.setPageSize(pageSize);
        }

        String categoryId = req.getParameter("categoryId");
        if (categoryId != null && !categoryId.isBlank()) {
            criteria.setCategoryId(categoryId.trim());
        }

        return criteria;
    }

    // =========================================================================
    // UTILITY METHODS
    // =========================================================================

    private boolean isNullOrBlank(String str) {
        return str == null || str.isBlank();
    }

    private AccountBean getAuthenticatedAccount(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        UserSession userSession = (UserSession) session.getAttribute("userSession");
        return (userSession != null) ? userSession.getAccount() : null;
    }

    private void handleUnauthorized(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (isAjaxRequest(req)) {
            sendErrorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, "User is not authenticated");
        } else {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
        }
    }

    private boolean isAjaxRequest(HttpServletRequest req) {
        String acceptHeader = req.getHeader("Accept");
        String requestedWith = req.getHeader("X-Requested-With");
        return (acceptHeader != null && acceptHeader.contains("application/json")) ||
                "XMLHttpRequest".equals(requestedWith);
    }

    private void sendErrorResponse(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setContentType("application/json");
        resp.setStatus(status);
        JSONObject json = new JSONObject();
        json.put("success", false);
        json.put("error", message);
        resp.getWriter().print(json.toString());
    }

    private void configureEncoding(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
    }

    private String getActionPath(HttpServletRequest req) {
        return ServletUtils.getActionPath(req);
    }

    private BigDecimal parseBigDecimal(String val) {
        if (val == null || val.isBlank()) return null;
        try {
            return new BigDecimal(val.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(String val) {
        if (val == null || val.isBlank()) return null;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
