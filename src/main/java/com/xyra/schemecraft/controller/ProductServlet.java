package com.xyra.schemecraft.controller;

import java.io.*;
import java.math.BigDecimal;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import com.xyra.schemecraft.util.FileUploadUtils;
import com.xyra.schemecraft.util.JsonUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyra.schemecraft.dto.OwnedProductItem;
import com.xyra.schemecraft.dto.ProductRequest;
import com.xyra.schemecraft.dto.ProductSearchCriteria;
import com.xyra.schemecraft.dto.ProductVersionRequest;
import com.xyra.schemecraft.exception.EntityNotFoundException;
import com.xyra.schemecraft.exception.ServiceException;
import com.xyra.schemecraft.exception.UnauthorizedActionException;
import com.xyra.schemecraft.model.AccountBean;
import com.xyra.schemecraft.model.ProductBean;
import com.xyra.schemecraft.model.ProductImageBean;
import com.xyra.schemecraft.model.ProductVersionBean;
import com.xyra.schemecraft.model.UserSession;
import com.xyra.schemecraft.service.ProductService;

@WebServlet(name = "ProductServlet", urlPatterns = {"/product/*"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2,
        maxFileSize = 1024 * 1024 * 10,
        maxRequestSize = 1024 * 1024 * 50
)
public class ProductServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ProductServlet.class);

    private ProductService productService;

    public ProductServlet() {
        super();
    }

    public ProductServlet(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        if (this.productService == null) {
            this.productService = new ProductService();
        }
        logger.info("ProductServlet successfully initialized.");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        configureEncoding(req, resp);

        String action = getActionPath(req);

        switch (action) {
            case "", "/", "/search" -> handleSearchProducts(req, resp);
            case "/detail", "/get" -> handleGetProductDetail(req, resp);
            case "/images" -> handleListImages(req, resp);
            case "/versions" -> handleListVersions(req, resp);
            case "/version" -> handleGetVersion(req, resp);
            case "/check-ownership" -> handleCheckOwnership(req, resp);
            case "/owned" -> handleListOwnedProducts(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND, "The requested resource was not found.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        configureEncoding(req, resp);

        String action = getActionPath(req);

        if ("/register-download".equals(action)) {
            handleRegisterDownload(req, resp);
            return;
        }

        AccountBean currentAccount = getAuthenticatedAccount(req);
        if (currentAccount == null) {
            handleUnauthorized(req, resp);
            return;
        }

        switch (action) {
            case "/create" -> handleCreateProduct(req, resp, currentAccount.getAccountId());
            case "/update" -> handleUpdateProduct(req, resp, currentAccount.getAccountId());
            case "/deactivate" -> handleDeactivateProduct(req, resp);
            case "/add-image" -> handleAddImage(req, resp);
            case "/remove-image" -> handleRemoveImage(req, resp);
            case "/publish-version" -> handlePublishVersion(req, resp);
            case "/assign-category" -> handleAssignCategory(req, resp);
            case "/remove-category" -> handleRemoveCategory(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "HTTP method not allowed for this endpoint.");
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

            if ("json".equalsIgnoreCase(format) || isAjaxRequest(req)) {
                resp.setContentType("application/json");
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("success", true);
                // Uso di JsonUtils qui
                jsonResponse.put("product", JsonUtils.serializeProduct(product));
                resp.getWriter().print(jsonResponse.toString());
            } else {
                req.setAttribute("product", product);
                req.getRequestDispatcher("/product-detail.jsp").forward(req, resp);
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
            List<ProductBean> products = productService.searchProducts(criteria);

            JSONArray array = new JSONArray();
            for (ProductBean product : products) {
                // Uso di JsonUtils qui
                array.put(JsonUtils.serializeProduct(product));
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

        // 1. Authenticate user
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
            boolean owns = productService.ownsProduct(currentAccount.getAccountId(), productId);
            if (!owns) {
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

    private void handleCreateProduct(HttpServletRequest req, HttpServletResponse resp, String accountId) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject jsonResponse = new JSONObject();

        try {
            ProductRequest request = parseProductRequest(req, accountId);
            ProductBean createdProduct = productService.createProduct(request);

            jsonResponse.put("success", true);
            jsonResponse.put("message", "Product created successfully.");
            // Uso di JsonUtils qui
            jsonResponse.put("product", JsonUtils.serializeProduct(createdProduct));
            out.print(jsonResponse.toString());

        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (ServiceException e) {
            logger.error("Error creating product for account: {}", accountId, e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to create product.");
        }
    }

    private void handleUpdateProduct(HttpServletRequest req, HttpServletResponse resp, String accountId) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject jsonResponse = new JSONObject();

        String productId = req.getParameter("productId");

        try {
            ProductRequest request = parseProductRequest(req, accountId);
            productService.updateProduct(productId, request);

            jsonResponse.put("success", true);
            jsonResponse.put("message", "Product updated successfully.");
            out.print(jsonResponse.toString());

        } catch (UnauthorizedActionException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (ServiceException e) {
            logger.error("Error updating product: {}", productId, e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to update product.");
        }
    }

    private void handleDeactivateProduct(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject jsonResponse = new JSONObject();

        String productId = req.getParameter("productId");

        try {
            productService.deactivateProduct(productId);
            jsonResponse.put("success", true);
            jsonResponse.put("message", "Product deactivated successfully.");
            out.print(jsonResponse.toString());

        } catch (EntityNotFoundException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (ServiceException e) {
            logger.error("Error deactivating product: {}", productId, e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to deactivate product.");
        }
    }

    private void handleAddImage(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject jsonResponse = new JSONObject();

        String productId = req.getParameter("productId");

        try {
            String imagePath = FileUploadUtils.saveUploadedFile(req, "imageFile", "products");

            if (imagePath == null || imagePath.isBlank()) {
                imagePath = req.getParameter("imagePath");
            }

            if (imagePath == null || imagePath.isBlank()) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "No image file or path provided.");
                return;
            }

            Integer displayOrderParam = parseInteger(req.getParameter("displayOrder"));

            ProductImageBean imageBean;
            if (displayOrderParam != null) {
                imageBean = productService.addImage(productId, imagePath, displayOrderParam);
            } else {
                imageBean = productService.addImage(productId, imagePath);
            }

            JSONObject imgObj = new JSONObject();
            imgObj.put("imageId", imageBean.getImageId());
            imgObj.put("productId", imageBean.getProductId());
            imgObj.put("imagePath", imageBean.getImagePath());
            imgObj.put("displayOrder", imageBean.getDisplayOrder());

            jsonResponse.put("success", true);
            jsonResponse.put("message", "Image uploaded and added successfully.");
            jsonResponse.put("image", imgObj);
            out.print(jsonResponse.toString());

        } catch (IllegalArgumentException | EntityNotFoundException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (ServiceException e) {
            logger.error("Error adding image to product: {}", productId, e);
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private void handleRemoveImage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject jsonResponse = new JSONObject();

        String imageId = req.getParameter("imageId");

        try {
            productService.removeImage(imageId);
            jsonResponse.put("success", true);
            jsonResponse.put("message", "Image removed successfully.");
            out.print(jsonResponse.toString());

        } catch (EntityNotFoundException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (ServiceException e) {
            logger.error("Error removing image: {}", imageId, e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to remove image.");
        }
    }

    private void handlePublishVersion(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject jsonResponse = new JSONObject();

        String productId = req.getParameter("productId");
        String versionStr = req.getParameter("version");
        String minecraftVersion = req.getParameter("minecraftVersion");
        String changelog = req.getParameter("changelog");

        try {
            String filePath = FileUploadUtils.saveUploadedFile(req, "schematicFile", "schematics");

            if (filePath == null || filePath.isBlank()) {
                filePath = req.getParameter("filePath");
            }

            if (filePath == null || filePath.isBlank()) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "No schematic file or file path provided.");
                return;
            }

            ProductVersionRequest request = new ProductVersionRequest(
                    productId,
                    versionStr,
                    minecraftVersion,
                    filePath,
                    changelog
            );

            ProductVersionBean version = productService.publishVersion(request);

            jsonResponse.put("success", true);
            jsonResponse.put("message", "Version published successfully.");
            jsonResponse.put("version", JsonUtils.serializeVersion(version));
            out.print(jsonResponse.toString());

        } catch (IllegalArgumentException | EntityNotFoundException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (ServiceException e) {
            logger.error("Error publishing version for product: {}", productId, e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to publish version.");
        }
    }

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

    private void handleAssignCategory(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject jsonResponse = new JSONObject();

        String productId = req.getParameter("productId");
        String categoryId = req.getParameter("categoryId");

        try {
            productService.assignCategory(productId, categoryId);
            jsonResponse.put("success", true);
            jsonResponse.put("message", "Category assigned successfully.");
            out.print(jsonResponse.toString());

        } catch (IllegalArgumentException | EntityNotFoundException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (ServiceException e) {
            logger.error("Error assigning category {} to product {}", categoryId, productId, e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to assign category.");
        }
    }

    private void handleRemoveCategory(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject jsonResponse = new JSONObject();

        String productId = req.getParameter("productId");
        String categoryId = req.getParameter("categoryId");

        try {
            productService.removeCategory(productId, categoryId);
            jsonResponse.put("success", true);
            jsonResponse.put("message", "Category removed successfully.");
            out.print(jsonResponse.toString());

        } catch (IllegalArgumentException | EntityNotFoundException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (ServiceException e) {
            logger.error("Error removing category {} from product {}", categoryId, productId, e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to remove category.");
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
        String pathInfo = req.getPathInfo();
        return (pathInfo == null) ? "" : pathInfo;
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
