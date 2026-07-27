package com.xyra.schemecraft.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.xyra.schemecraft.dto.*;
import com.xyra.schemecraft.util.ServletUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyra.schemecraft.exception.EntityNotFoundException;
import com.xyra.schemecraft.exception.ServiceException;
import com.xyra.schemecraft.exception.UnauthorizedActionException;
import com.xyra.schemecraft.model.AccountBean;
import com.xyra.schemecraft.model.CategoryBean;
import com.xyra.schemecraft.model.ProductBean;
import com.xyra.schemecraft.model.UserSession;
import com.xyra.schemecraft.service.AccountService;
import com.xyra.schemecraft.service.CategoryService;
import com.xyra.schemecraft.service.OrderService;
import com.xyra.schemecraft.service.ProductService;
import com.xyra.schemecraft.util.JsonUtils;

@WebServlet(name = "AdminServlet", urlPatterns = {"/admin/*"})
public class AdminServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AdminServlet.class);

    private static final String SESSION_ATTRIBUTE = "userSession";

    private ProductService productService;
    private OrderService orderService;
    private AccountService accountService;
    private CategoryService categoryService;

    public AdminServlet() {
        super();
    }

    public AdminServlet(ProductService productService, OrderService orderService,
                        AccountService accountService, CategoryService categoryService) {
        this.productService = productService;
        this.orderService = orderService;
        this.accountService = accountService;
        this.categoryService = categoryService;
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
            case "/orders" ->
                    req.getRequestDispatcher("/WEB-INF/admin/orders.jsp").forward(req, resp);
            case "/users" ->
                    req.getRequestDispatcher("/WEB-INF/admin/users.jsp").forward(req, resp);

            case "/products/list" -> handleListProducts(req, resp);
            case "/orders/list" -> handleListOrders(req, resp);
            case "/users/list" -> handleListUsers(req, resp);
            case "/categories/list" -> handleListCategories(req, resp);

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