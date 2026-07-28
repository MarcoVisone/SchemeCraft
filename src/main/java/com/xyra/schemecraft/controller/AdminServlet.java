package com.xyra.schemecraft.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import com.xyra.schemecraft.constant.ServiceConstants;
import com.xyra.schemecraft.dto.*;
import com.xyra.schemecraft.model.*;
import com.xyra.schemecraft.service.*;
import com.xyra.schemecraft.util.FileUploadUtils;
import com.xyra.schemecraft.util.ServletUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyra.schemecraft.exception.EntityNotFoundException;
import com.xyra.schemecraft.exception.ServiceException;
import com.xyra.schemecraft.exception.UnauthorizedActionException;
import com.xyra.schemecraft.util.JsonUtils;

@WebServlet(name = "AdminServlet", urlPatterns = {"/admin/*"})
@MultipartConfig(maxFileSize = 52_428_800) // 50 MB hard limit enforced by the container
public class AdminServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AdminServlet.class);

    private static final String SESSION_ATTRIBUTE = "userSession";

    private ProductService productService;
    private OrderService orderService;
    private AccountService accountService;
    private CategoryService categoryService;
    private LookupService lookupService;

    public AdminServlet() {
        super();
    }

    public AdminServlet(ProductService productService, OrderService orderService,
                        AccountService accountService, CategoryService categoryService, LookupService lookupService) {
        this.productService = productService;
        this.orderService = orderService;
        this.accountService = accountService;
        this.categoryService = categoryService;
        this.lookupService = lookupService;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        if (this.productService == null) {
            this.productService = new ProductService();
        }
        if (this.orderService == null) {
            this.orderService = new OrderService();
        }
        if (this.accountService == null) {
            this.accountService = new AccountService();
        }
        if (this.categoryService == null) {
            this.categoryService = new CategoryService();
        }
        if (this.lookupService == null) {
            this.lookupService = new LookupService();
        }
        logger.info("AdminServlet successfully initialized.");
    }

    // =========================================================================
    // ROUTING & SECURITY GUARD
    // =========================================================================

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        if (checkAdminAccess(req, resp)) {
            return;
        }

        String action = getActionPath(req);

        logger.info("AdminServlet received GET request for action: {}", action);

        switch (action) {
            case "", "/", "/products" ->
                    req.getRequestDispatcher("/WEB-INF/admin/products.jsp").forward(req, resp);
            case "/products/new" -> // <-- NUOVA ROTTA WIZARD
                    req.getRequestDispatcher("/WEB-INF/admin/product_new.jsp").forward(req, resp);
            case "/orders" ->
                    req.getRequestDispatcher("/WEB-INF/admin/orders.jsp").forward(req, resp);
            case "/users" ->
                    req.getRequestDispatcher("/WEB-INF/admin/users.jsp").forward(req, resp);

            case "/products/list" -> handleListProducts(req, resp);
            case "/orders/list" -> handleListOrders(req, resp);
            case "/users/list" -> handleListUsers(req, resp);
            case "/categories/list" -> handleListCategories(req, resp);
            case "/currencies/list" -> handleListCurrencies(req, resp);

            default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Endpoint non trovato.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        if (checkAdminAccess(req, resp)) {
            return;
        }

        String action = getActionPath(req);

        switch (action) {
            case "/products/create" -> handleCreateProduct(req, resp);
            case "/products/create-full" -> handleCreateFullProduct(req, resp);
            case "/upload/image" -> handleUploadImage(req, resp);
            case "/upload/schematic" -> handleUploadSchematic(req, resp);
            case "/products/update" -> handleUpdateProduct(req, resp);
            case "/products/delete" -> handleDeleteProduct(req, resp);
            case "/products/activate" -> handleActivateProduct(req, resp);
            case "/users/deactivate" -> handleDeactivateUser(req, resp);
            case "/users/reactivate" -> handleReactivateUser(req, resp);
            case "/categories/create" -> handleCreateCategory(req, resp);
            case "/categories/update" -> handleUpdateCategory(req, resp);
            case "/categories/delete" -> handleDeleteCategory(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "HTTP method not allowed for this admin endpoint.");
        }
    }

    // =========================================================================
    // PRODUCT HANDLERS
    // =========================================================================

    private void handleListProducts(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String keywords = req.getParameter("keywords");
        String pageParam = req.getParameter("page");
        String pageSizeParam = req.getParameter("pageSize");

        ProductSearchCriteria criteria = createCriteria(keywords, pageParam, pageSizeParam);

        try {
            List<ProductBean> products = productService.searchProducts(criteria);
            JsonUtils.sendSuccessWithData(resp, "Products retrieved successfully.", "products", products);
        } catch (ServiceException e) {
            logger.error("Error retrieving products list for admin", e);
            JsonUtils.sendError(resp, "Unable to retrieve products list.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private ProductSearchCriteria createCriteria(String keywords, String pageParam, String pageSizeParam) {
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        if (!isNullOrBlank(keywords)) {
            criteria.setKeywords(keywords);
        }

        if (!isNullOrBlank(pageParam)) {
            try {
                criteria.setPageNumber(Integer.parseInt(pageParam));
            } catch (NumberFormatException ignored) {}
        }

        if (!isNullOrBlank(pageSizeParam)) {
            try {
                criteria.setPageSize(Integer.parseInt(pageSizeParam));
            } catch (NumberFormatException ignored) {}
        }
        return criteria;
    }

    private void handleCreateProduct(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AccountBean adminAccount = getAuthenticatedAccount(req);
        if (adminAccount == null) {
            JsonUtils.sendError(resp, "Authentication required.", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String productName = req.getParameter("productName");
        String currencyId = req.getParameter("currencyId");
        String priceParam = req.getParameter("price");

        if (isNullOrBlank(productName) || isNullOrBlank(currencyId) || isNullOrBlank(priceParam)) {
            JsonUtils.sendError(resp, "Missing required parameters: productName, currencyId, and price.", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            ProductRequest productRequest = extractProductRequest(req, adminAccount.getAccountId());
            ProductBean createdProduct = productService.createProduct(productRequest);

            JsonUtils.sendSuccessWithData(resp, "Product created successfully.", "product", createdProduct);

        } catch (NumberFormatException e) {
            JsonUtils.sendError(resp, "Invalid numerical format for price, discount, or stock quantity.", HttpServletResponse.SC_BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (ServiceException e) {
            logger.error("Error creating product by admin {}", adminAccount.getAccountId(), e);
            JsonUtils.sendError(resp, "Unable to create product.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleUpdateProduct(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AccountBean adminAccount = getAuthenticatedAccount(req);
        if (adminAccount == null) {
            JsonUtils.sendError(resp, "Authentication required.", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String productId = req.getParameter("productId");
        if (isNullOrBlank(productId)) {
            JsonUtils.sendError(resp, "Parameter productId is required.", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            ProductRequest productRequest = extractProductRequest(req, adminAccount.getAccountId());
            productService.updateProduct(productId, productRequest);

            JsonUtils.sendSuccess(resp, "Product updated successfully.");

        } catch (NumberFormatException e) {
            JsonUtils.sendError(resp, "Invalid numerical format for price, discount, or stock quantity.", HttpServletResponse.SC_BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (UnauthorizedActionException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_FORBIDDEN);
        } catch (EntityNotFoundException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (ServiceException e) {
            logger.error("Error updating product {} by admin {}", productId, adminAccount.getAccountId(), e);
            JsonUtils.sendError(resp, "Unable to update product.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private ProductRequest extractProductRequest(HttpServletRequest req, String accountId) throws NumberFormatException {
        String productName = req.getParameter("productName");
        String description = req.getParameter("description");
        String currencyId = req.getParameter("currencyId");
        String priceParam = req.getParameter("price");
        String discountParam = req.getParameter("discount");
        String stockParam = req.getParameter("stockQuantity");
        boolean unlimitedStock = Boolean.parseBoolean(req.getParameter("unlimitedStock"));

        BigDecimal price = isNullOrBlank(priceParam) ? null : new BigDecimal(priceParam);
        BigDecimal discount = isNullOrBlank(discountParam) ? BigDecimal.ZERO : new BigDecimal(discountParam);
        Integer stockQuantity = isNullOrBlank(stockParam) ? 0 : Integer.parseInt(stockParam);

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

    private void handleDeleteProduct(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String productId = req.getParameter("productId");

        if (isNullOrBlank(productId)) {
            JsonUtils.sendError(resp, "Parameter productId is required.", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            productService.deactivateProduct(productId);
            JsonUtils.sendSuccess(resp, "Product deactivated/deleted successfully.");
        } catch (IllegalArgumentException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (EntityNotFoundException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (ServiceException e) {
            logger.error("Error deactivating product {}", productId, e);
            JsonUtils.sendError(resp, "Unable to deactivate product.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleActivateProduct(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String productId = req.getParameter("productId");

        if (isNullOrBlank(productId)) {
            JsonUtils.sendError(resp, "Parameter productId is required.", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            productService.activateProduct(productId);
            JsonUtils.sendSuccess(resp, "Product activated successfully.");
        } catch (IllegalArgumentException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (EntityNotFoundException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (ServiceException e) {
            logger.error("Error activating product {}", productId, e);
            JsonUtils.sendError(resp, "Unable to activate product.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }


    private void handleCreateFullProduct(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AccountBean adminAccount = getAuthenticatedAccount(req);
        if (adminAccount == null) {
            JsonUtils.sendError(resp, "Authentication required.", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            JSONObject body = JsonUtils.readJsonBody(req);

            JSONObject productJson = body.getJSONObject("product");
            BigDecimal price = productJson.getBigDecimal("price");
            BigDecimal discount = productJson.has("discount") ? productJson.getBigDecimal("discount") : BigDecimal.ZERO;
            boolean unlimitedStock = productJson.optBoolean("unlimitedStock", false);
            Integer stockQuantity = productJson.has("stockQuantity") ? productJson.getInt("stockQuantity") : 0;

            ProductRequest productRequest = new ProductRequest(
                    adminAccount.getAccountId(),
                    productJson.getString("currencyId"),
                    productJson.getString("productName"),
                    productJson.optString("description", null),
                    discount,
                    price,
                    stockQuantity,
                    unlimitedStock
            );

            List<String> categoryIds = new ArrayList<>();
            for (Object categoryId : body.getJSONArray("categoryIds")) {
                categoryIds.add((String) categoryId);
            }

            List<String> imagePaths = new ArrayList<>();
            for (Object imagePath : body.getJSONArray("imagePaths")) {
                imagePaths.add((String) imagePath);
            }

            JSONObject versionJson = body.getJSONObject("version");
            ProductVersionRequest versionRequest = new ProductVersionRequest(
                    null, // productId is resolved internally by the service, since the product doesn't exist yet
                    versionJson.optString("changelog", null),
                    versionJson.getString("filePath"),
                    versionJson.optString("minecraftVersion", null),
                    versionJson.getString("version")
            );

            ProductFullRequest fullRequest = new ProductFullRequest(productRequest, categoryIds, imagePaths, versionRequest);

            ProductBean createdProduct = productService.createFullProduct(fullRequest);

            JsonUtils.sendSuccessWithData(resp, "Product fully created successfully.", "product", createdProduct);

        } catch (org.json.JSONException e) {
            JsonUtils.sendError(resp, "Invalid or incomplete request payload.", HttpServletResponse.SC_BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (ServiceException e) {
            logger.error("Error creating full product by admin {}", adminAccount.getAccountId(), e);
            JsonUtils.sendError(resp, "Unable to create product.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // =========================================================================
    // FILE UPLOAD HANDLERS
    // =========================================================================

    private void handleUploadImage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AccountBean adminAccount = getAuthenticatedAccount(req);
        if (adminAccount == null) {
            JsonUtils.sendError(resp, "Authentication required.", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            Part filePart = req.getPart("file");

            if (!isValidUpload(filePart, ServiceConstants.ALLOWED_IMAGE_EXTENSIONS,
                    ServiceConstants.ALLOWED_IMAGE_CONTENT_TYPES, ServiceConstants.MAX_IMAGE_SIZE_BYTES)) {
                JsonUtils.sendError(resp, "Invalid image file. Allowed types: PNG, JPG, JPEG, WEBP (max 50MB).",
                        HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            String savedPath = FileUploadUtils.saveUploadedFile(filePart,
                    req.getServletContext().getRealPath(""), "products");

            if (savedPath == null) {
                JsonUtils.sendError(resp, "File upload failed.", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            logger.info("Image uploaded by admin {}: {}", adminAccount.getAccountId(), savedPath);
            JsonUtils.sendSuccess(resp, "Image uploaded successfully.", "path", savedPath);

        } catch (ServletException e) {
            logger.error("Error reading multipart request for image upload", e);
            JsonUtils.sendError(resp, "Invalid upload request.", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void handleUploadSchematic(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AccountBean adminAccount = getAuthenticatedAccount(req);
        if (adminAccount == null) {
            JsonUtils.sendError(resp, "Authentication required.", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            Part filePart = req.getPart("file");

            if (!isValidUpload(filePart, ServiceConstants.ALLOWED_SCHEMATIC_EXTENSIONS,
                    null, ServiceConstants.MAX_SCHEMATIC_SIZE_BYTES)) {
                JsonUtils.sendError(resp,
                        "Invalid schematic file. Allowed types: .schematic, .schem, .litematic (max 50MB).",
                        HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            String savedPath = FileUploadUtils.saveUploadedFile(filePart,
                    req.getServletContext().getRealPath(""), "schematics");

            if (savedPath == null) {
                JsonUtils.sendError(resp, "File upload failed.", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            logger.info("Schematic uploaded by admin {}: {}", adminAccount.getAccountId(), savedPath);
            JsonUtils.sendSuccess(resp, "Schematic uploaded successfully.", "path", savedPath);

        } catch (ServletException e) {
            logger.error("Error reading multipart request for schematic upload", e);
            JsonUtils.sendError(resp, "Invalid upload request.", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Validates an uploaded file part against an extension whitelist, an optional content-type
     * whitelist, and a maximum size. The submitted file name is normalized to its final path
     * segment before checking the extension, guarding against path traversal attempts embedded
     * in the client-supplied file name.
     */
    private boolean isValidUpload(Part filePart, Set<String> allowedExtensions,
                                  Set<String> allowedContentTypes, long maxSizeBytes) {
        if (filePart == null || filePart.getSize() <= 0) {
            return false;
        }

        if (filePart.getSize() > maxSizeBytes) {
            logger.warn("Upload rejected: file size {} exceeds limit {}", filePart.getSize(), maxSizeBytes);
            return false;
        }

        String submittedFileName = filePart.getSubmittedFileName();
        if (submittedFileName == null || submittedFileName.isBlank()) {
            return false;
        }

        // Normalize to the last path segment only, discarding any directory traversal sequences
        // a malicious client might embed in the submitted file name.
        String fileName = Paths.get(submittedFileName).getFileName().toString();

        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0) {
            return false;
        }
        String extension = fileName.substring(dotIndex).toLowerCase();

        if (!allowedExtensions.contains(extension)) {
            logger.warn("Upload rejected: extension {} not allowed", extension);
            return false;
        }

        if (allowedContentTypes != null) {
            String contentType = filePart.getContentType();
            if (contentType == null || !allowedContentTypes.contains(contentType.toLowerCase())) {
                logger.warn("Upload rejected: content-type {} not allowed", contentType);
                return false;
            }
        }

        return true;
    }

    // =========================================================================
    // ORDER HANDLERS (FILTERS)
    // =========================================================================

    private void handleListOrders(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String customerId = req.getParameter("customerId");
        String customerUsername = req.getParameter("customerUsername");
        String customerEmail = req.getParameter("customerEmail");
        String dateFromParam = req.getParameter("dateFrom"); // Format: YYYY-MM-DD
        String dateToParam = req.getParameter("dateTo");     // Format: YYYY-MM-DD
        String statusParam = req.getParameter("status");

        OrderSearchCriteria criteria = new OrderSearchCriteria();

        if (!isNullOrBlank(customerId)) {
            criteria.setCustomerId(customerId.trim());
        }
        if (!isNullOrBlank(customerUsername)) {
            criteria.setCustomerUsername(customerUsername.trim());
        }
        if (!isNullOrBlank(customerEmail)) {
            criteria.setCustomerEmail(customerEmail.trim());
        }

        if (!isNullOrBlank(statusParam)) {
            try {
                criteria.setStatus(Integer.parseInt(statusParam));
            } catch (NumberFormatException e) {
                JsonUtils.sendError(resp, "Invalid status parameter.", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        }

        try {
            if (!isNullOrBlank(dateFromParam)) {
                LocalDate fromDate = LocalDate.parse(dateFromParam.trim());
                criteria.setDateFrom(fromDate.atStartOfDay());
            }
            if (!isNullOrBlank(dateToParam)) {
                LocalDate toDate = LocalDate.parse(dateToParam.trim());
                criteria.setDateTo(toDate.atTime(LocalTime.MAX));
            }
        } catch (DateTimeParseException e) {
            JsonUtils.sendError(resp, "Invalid date format. Expected format: YYYY-MM-DD.", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            List<OrderAdminView> orders = orderService.searchOrders(criteria);
            JsonUtils.sendSuccessWithData(resp, "Orders retrieved successfully.", "orders", orders);
        } catch (IllegalArgumentException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (ServiceException e) {
            logger.error("Error searching orders with filter criteria", e);
            JsonUtils.sendError(resp, "Unable to retrieve orders.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // =========================================================================
    // USER HANDLERS
    // =========================================================================

    private void handleListUsers(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<AccountAdminView> accounts = accountService.listAllAccountsForAdmin();
            JsonUtils.sendSuccessWithData(resp, "Accounts retrieved successfully.", "accounts", accounts);
        } catch (ServiceException e) {
            logger.error("Error retrieving accounts list for admin", e);
            JsonUtils.sendError(resp, "Unable to retrieve accounts list.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleDeactivateUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String accountId = req.getParameter("accountId");

        try {
            accountService.deactivateAccount(accountId);
            JsonUtils.sendSuccess(resp, "Account deactivated successfully.");
        } catch (IllegalArgumentException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (EntityNotFoundException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (ServiceException e) {
            logger.error("Error deactivating account {}", accountId, e);
            JsonUtils.sendError(resp, "Unable to deactivate account.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleReactivateUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String accountId = req.getParameter("accountId");

        try {
            accountService.reactivateAccount(accountId);
            JsonUtils.sendSuccess(resp, "Account reactivated successfully.");
        } catch (IllegalArgumentException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (EntityNotFoundException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (ServiceException e) {
            logger.error("Error reactivating account {}", accountId, e);
            JsonUtils.sendError(resp, "Unable to reactivate account.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // =========================================================================
    // CATEGORY HANDLERS
    // =========================================================================

    private void handleListCategories(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<CategoryBean> categories = categoryService.listAllCategories();
            JsonUtils.sendSuccessWithData(resp, "Categories retrieved successfully.", "categories", categories);
        } catch (ServiceException e) {
            logger.error("Error retrieving categories list for admin", e);
            JsonUtils.sendError(resp, "Unable to retrieve categories list.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleCreateCategory(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            CategoryRequest categoryRequest = extractCategoryRequest(req, null);
            CategoryBean createdCategory = categoryService.createCategory(categoryRequest);
            JsonUtils.sendSuccessWithData(resp, "Category created successfully.", "category", createdCategory);
        } catch (IllegalArgumentException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (EntityNotFoundException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (ServiceException e) {
            logger.error("Error creating category", e);
            JsonUtils.sendError(resp, "Unable to create category.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleUpdateCategory(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String categoryId = req.getParameter("categoryId");

        if (isNullOrBlank(categoryId)) {
            JsonUtils.sendError(resp, "Parameter categoryId is required.", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            CategoryRequest categoryRequest = extractCategoryRequest(req, categoryId);
            categoryService.updateCategory(categoryRequest);
            JsonUtils.sendSuccess(resp, "Category updated successfully.");
        } catch (IllegalArgumentException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (EntityNotFoundException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (ServiceException e) {
            logger.error("Error updating category {}", categoryId, e);
            JsonUtils.sendError(resp, "Unable to update category.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private CategoryRequest extractCategoryRequest(HttpServletRequest req, String categoryId) {
        String categoryName = req.getParameter("categoryName");
        String parentCategoryId = req.getParameter("parentCategoryId");
        String description = req.getParameter("description");

        return new CategoryRequest(categoryId, categoryName, parentCategoryId, description);
    }

    private void handleDeleteCategory(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String categoryId = req.getParameter("categoryId");

        if (isNullOrBlank(categoryId)) {
            JsonUtils.sendError(resp, "Parameter categoryId is required.", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            categoryService.deleteCategory(categoryId);
            JsonUtils.sendSuccess(resp, "Category deactivated/deleted successfully.");
        } catch (IllegalArgumentException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (EntityNotFoundException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (ServiceException e) {
            logger.error("Error deleting category {}", categoryId, e);
            JsonUtils.sendError(resp, "Unable to delete category.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // =========================================================================
    // CURRENCIES HANDLERS
    // =========================================================================

    private void handleListCurrencies(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<CurrencyBean> currencies = lookupService.listActiveCurrencies();
            JsonUtils.sendSuccessWithData(resp, "Currencies retrieved successfully.", "currencies", currencies);
        } catch (ServiceException e) {
            logger.error("Error retrieving active currencies list for admin wizard", e);
            JsonUtils.sendError(resp, "Unable to retrieve currencies.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // =========================================================================
    // SECURITY & UTILITY HELPER METHODS
    // =========================================================================

    private boolean checkAdminAccess(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserSession userSession = getAuthenticatedUserSession(req);

        if (userSession == null) {
            JsonUtils.sendError(resp, "Authentication required.", HttpServletResponse.SC_UNAUTHORIZED);
            return true;
        }

        if (!userSession.isAdmin()) {
            logger.warn("Unauthorized admin access attempt by user: {}",
                    userSession.getAccount() != null ? userSession.getAccount().getAccountId() : "unknown");
            JsonUtils.sendError(resp, "Forbidden: Admin privileges required.", HttpServletResponse.SC_FORBIDDEN);
            return true;
        }

        return false;
    }

    private String getActionPath(HttpServletRequest req) {
        return ServletUtils.getActionPath(req);
    }

    private boolean isNullOrBlank(String str) {
        return str == null || str.isBlank();
    }

    private UserSession getAuthenticatedUserSession(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        return (UserSession) session.getAttribute(SESSION_ATTRIBUTE);
    }

    private AccountBean getAuthenticatedAccount(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        return (AccountBean) session.getAttribute("account");
    }
}