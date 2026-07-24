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

import com.xyra.schemecraft.dto.OrderAdminView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyra.schemecraft.dto.OrderSearchCriteria;
import com.xyra.schemecraft.dto.ProductRequest;
import com.xyra.schemecraft.dto.ProductSearchCriteria;
import com.xyra.schemecraft.exception.EntityNotFoundException;
import com.xyra.schemecraft.exception.ServiceException;
import com.xyra.schemecraft.exception.UnauthorizedActionException;
import com.xyra.schemecraft.model.AccountBean;
import com.xyra.schemecraft.model.ProductBean;
import com.xyra.schemecraft.service.OrderService;
import com.xyra.schemecraft.service.ProductService;
import com.xyra.schemecraft.util.JsonUtils;

@WebServlet(name = "AdminServlet", urlPatterns = {"/admin/*"})
public class AdminServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AdminServlet.class);

    private ProductService productService;
    private OrderService orderService;

    public AdminServlet() {
        super();
    }

    public AdminServlet(ProductService productService, OrderService orderService) {
        this.productService = productService;
        this.orderService = orderService;
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
        logger.info("AdminServlet successfully initialized.");
    }

    // =========================================================================
    // ROUTING & SECURITY GUARD
    // =========================================================================

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (checkAdminAccess(req, resp)) {
            return;
        }

        String action = getActionPath(req);

        switch (action) {
            case "/products/list" -> handleListProducts(req, resp);
            case "/orders/list" -> handleListOrders(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND, "The requested admin endpoint was not found.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (checkAdminAccess(req, resp)) {
            return;
        }

        String action = getActionPath(req);

        switch (action) {
            case "/products/create" -> handleCreateProduct(req, resp);
            case "/products/update" -> handleUpdateProduct(req, resp);
            case "/products/delete" -> handleDeleteProduct(req, resp);
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
            JsonUtils.sendSuccess(resp, "Products retrieved successfully.", "products", products);
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

            JsonUtils.sendSuccess(resp, "Product created successfully.", "product", createdProduct);

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

            JsonUtils.sendSuccess(resp, "Product updated successfully.", null, null);

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

// =========================================================================
// PRIVATE HELPER FOR DTO EXTRACTION
// =========================================================================

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
            JsonUtils.sendSuccess(resp, "Product deactivated/deleted successfully.", null, null);
        } catch (IllegalArgumentException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (EntityNotFoundException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (ServiceException e) {
            logger.error("Error deactivating product {}", productId, e);
            JsonUtils.sendError(resp, "Unable to deactivate product.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
            JsonUtils.sendSuccess(resp, "Orders retrieved successfully.", "orders", orders);
        } catch (IllegalArgumentException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (ServiceException e) {
            logger.error("Error searching orders with filter criteria", e);
            JsonUtils.sendError(resp, "Unable to retrieve orders.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // =========================================================================
    // SECURITY & UTILITY HELPER METHODS
    // =========================================================================

    private boolean checkAdminAccess(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AccountBean account = getAuthenticatedAccount(req);

        if (account == null) {
            JsonUtils.sendError(resp, "Authentication required.", HttpServletResponse.SC_UNAUTHORIZED);
            return true;
        }

        if (!account.isAdmin()) {
            logger.warn("Unauthorized admin access attempt by user: {}", account.getAccountId());
            JsonUtils.sendError(resp, "Forbidden: Admin privileges required.", HttpServletResponse.SC_FORBIDDEN);
            return true;
        }

        return false;
    }

    private String getActionPath(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        return (pathInfo == null) ? "" : pathInfo;
    }

    private boolean isNullOrBlank(String str) {
        return str == null || str.isBlank();
    }

    private AccountBean getAuthenticatedAccount(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        return (AccountBean) session.getAttribute("account");
    }
}
