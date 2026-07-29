package com.xyra.schemecraft.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.xyra.schemecraft.dto.OrderDetailDTO;
import com.xyra.schemecraft.util.ServletUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyra.schemecraft.dto.OrderAdminView;
import com.xyra.schemecraft.dto.OrderSearchCriteria;
import com.xyra.schemecraft.exception.DuplicateEntityException;
import com.xyra.schemecraft.exception.EntityNotFoundException;
import com.xyra.schemecraft.exception.InsufficientStockException;
import com.xyra.schemecraft.exception.PaymentDeclinedException;
import com.xyra.schemecraft.exception.ServiceException;
import com.xyra.schemecraft.model.AccountBean;
import com.xyra.schemecraft.model.OrderBean;
import com.xyra.schemecraft.model.UserSession;
import com.xyra.schemecraft.service.OrderService;
import com.xyra.schemecraft.util.JsonUtils;

@WebServlet(name = "OrderServlet", urlPatterns = {"/order/*", "/orders/*"})
public class OrderServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(OrderServlet.class);

    private OrderService orderService;

    public OrderServlet() {
        super();
    }

    public OrderServlet(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        if (this.orderService == null) {
            this.orderService = new OrderService();
        }
        logger.info("OrderServlet successfully initialized.");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        configureEncoding(req, resp);

        AccountBean currentAccount = getAuthenticatedAccount(req);
        if (currentAccount == null) {
            handleUnauthorized(req, resp);
            return;
        }

        String action = getActionPath(req);

        switch (action) {
            case "", "/", "/list", "/my-orders" -> handleListAccountOrders(req, resp, currentAccount.getAccountId());
            case "/detail" -> handleGetOrderDetail(req, resp, currentAccount.getAccountId());
            case "/admin/search" -> handleSearchOrdersAdmin(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND, "The requested resource was not found.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        configureEncoding(req, resp);

        AccountBean currentAccount = getAuthenticatedAccount(req);
        if (currentAccount == null) {
            handleUnauthorized(req, resp);
            return;
        }

        String action = getActionPath(req);

        switch (action) {
            case "/checkout/direct" -> handlePlaceOrderDirect(req, resp, currentAccount.getAccountId());
            case "/checkout/cart" -> handlePlaceOrderFromCart(req, resp, currentAccount.getAccountId());
            case "/update-status" -> handleUpdateOrderStatus(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "HTTP method not allowed for this endpoint.");
        }
    }

    // =========================================================================
    // GET HANDLERS
    // =========================================================================

    private void handleListAccountOrders(HttpServletRequest req, HttpServletResponse resp, String accountId)
            throws ServletException, IOException {
        String format = req.getParameter("format");
        int pageNumber = parseIntegerWithDefault(req.getParameter("page"), 1);

        try {
            List<OrderBean> orders = orderService.listAccountOrders(accountId, pageNumber);

            if ("json".equalsIgnoreCase(format) || isAjaxRequest(req)) {
                resp.setContentType("application/json");
                JSONObject jsonResponse = new JSONObject();
                JSONArray array = new JSONArray();

                for (OrderBean order : orders) {
                    array.put(JsonUtils.serializeOrder(order));
                }

                jsonResponse.put("success", true);
                jsonResponse.put("orders", array);
                jsonResponse.put("page", pageNumber);
                resp.getWriter().print(jsonResponse.toString());
            } else {
                req.setAttribute("orders", orders);
                req.setAttribute("currentPage", pageNumber);
                req.getRequestDispatcher("/orders.jsp").forward(req, resp);
            }

        } catch (ServiceException e) {
            logger.error("Error listing orders for account: {}", accountId, e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to retrieve order history.");
        }
    }

    private void handleGetOrderDetail(HttpServletRequest req, HttpServletResponse resp, String accountId)
            throws ServletException, IOException {
        String orderId = req.getParameter("id");
        String format = req.getParameter("format");

        if (orderId == null || orderId.isBlank()) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Parameter missing: id");
            return;
        }

        try {
            OrderDetailDTO orderDetail = orderService.getFullOrderDetail(orderId);

            if (!orderDetail.getOrder().getAccountId().equals(accountId)) {
                sendErrorResponse(resp, HttpServletResponse.SC_FORBIDDEN, "You do not have permission to view this order.");
                return;
            }

            if ("json".equalsIgnoreCase(format) || isAjaxRequest(req)) {
                resp.setContentType("application/json");
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("success", true);
                jsonResponse.put("order", JsonUtils.serializeOrderDetail(orderDetail));
                resp.getWriter().print(jsonResponse.toString());
            } else {
                req.setAttribute("orderDetail", orderDetail);
                req.getRequestDispatcher("/order-detail.jsp").forward(req, resp);
            }

        } catch (EntityNotFoundException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (ServiceException e) {
            logger.error("Error retrieving order details for ID: {}", orderId, e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to retrieve order details.");
        }
    }

    private void handleSearchOrdersAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject jsonResponse = new JSONObject();

        try {
            OrderSearchCriteria criteria = buildSearchCriteria(req);
            List<OrderAdminView> results = orderService.searchOrders(criteria);

            JSONArray array = new JSONArray();
            for (OrderAdminView adminView : results) {
                array.put(JsonUtils.serializeOrderAdminView(adminView));
            }

            jsonResponse.put("success", true);
            jsonResponse.put("orders", array);
            out.print(jsonResponse.toString());

        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (ServiceException e) {
            logger.error("Error searching orders for admin", e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to execute order search.");
        }
    }

    // =========================================================================
    // POST HANDLERS
    // =========================================================================

    private void handlePlaceOrderDirect(HttpServletRequest req, HttpServletResponse resp, String accountId) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject jsonResponse = new JSONObject();

        String productId = req.getParameter("productId");

        if (productId == null || productId.isBlank()) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing required parameter: productId");
            return;
        }

        try {
            String transactionId = orderService.placeOrderDirect(accountId, productId);

            jsonResponse.put("success", true);
            jsonResponse.put("message", "Order placed successfully.");
            jsonResponse.put("transactionId", transactionId);
            out.print(jsonResponse.toString());

        } catch (DuplicateEntityException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (InsufficientStockException | IllegalArgumentException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (PaymentDeclinedException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_PAYMENT_REQUIRED, e.getMessage());
        } catch (EntityNotFoundException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (ServiceException e) {
            logger.error("Error placing direct order for product {} and account {}", productId, accountId, e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void handlePlaceOrderFromCart(HttpServletRequest req, HttpServletResponse resp, String accountId) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject jsonResponse = new JSONObject();

        try {
            String transactionId = orderService.placeOrderFromCart(accountId);

            jsonResponse.put("success", true);
            jsonResponse.put("message", "Cart order placed successfully.");
            jsonResponse.put("transactionId", transactionId);
            out.print(jsonResponse.toString());

        } catch (DuplicateEntityException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (InsufficientStockException | IllegalArgumentException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (PaymentDeclinedException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_PAYMENT_REQUIRED, e.getMessage());
        } catch (EntityNotFoundException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (ServiceException e) {
            logger.error("Error placing cart order for account {}", accountId, e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void handleUpdateOrderStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject jsonResponse = new JSONObject();

        String orderId = req.getParameter("orderId");
        String statusIdStr = req.getParameter("statusId");

        if (orderId == null || orderId.isBlank() || statusIdStr == null || statusIdStr.isBlank()) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters: orderId or statusId");
            return;
        }

        try {
            int statusId = Integer.parseInt(statusIdStr);
            orderService.updateOrderStatus(orderId, statusId);

            jsonResponse.put("success", true);
            jsonResponse.put("message", "Order status updated successfully.");
            out.print(jsonResponse.toString());

        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid statusId parameter format.");
        } catch (EntityNotFoundException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (ServiceException e) {
            logger.error("Error updating status for order {}", orderId, e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to update order status.");
        }
    }

    // =========================================================================
    // HELPER & UTILITY METHODS
    // =========================================================================

    private OrderSearchCriteria buildSearchCriteria(HttpServletRequest req) {
        OrderSearchCriteria criteria = new OrderSearchCriteria();

        String dateFromStr = req.getParameter("dateFrom");
        if (dateFromStr != null && !dateFromStr.isBlank()) {
            try {
                if (dateFromStr.contains("T")) {
                    criteria.setDateFrom(LocalDateTime.parse(dateFromStr));
                } else {
                    criteria.setDateFrom(LocalDate.parse(dateFromStr).atStartOfDay());
                }
            } catch (DateTimeParseException e) {
                logger.warn(": {}", dateFromStr);
            }
        }

        String dateToStr = req.getParameter("dateTo");
        if (dateToStr != null && !dateToStr.isBlank()) {
            try {
                if (dateToStr.contains("T")) {
                    criteria.setDateTo(LocalDateTime.parse(dateToStr));
                } else {
                    criteria.setDateTo(LocalDate.parse(dateToStr).atTime(LocalTime.MAX));
                }
            } catch (DateTimeParseException e) {
                logger.warn(": {}", dateToStr);
            }
        }

        criteria.setCustomerId(req.getParameter("customerId"));
        criteria.setCustomerUsername(req.getParameter("customerUsername"));
        criteria.setCustomerEmail(req.getParameter("customerEmail"));

        String statusStr = req.getParameter("status");
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                criteria.setStatus(Integer.parseInt(statusStr));
            } catch (NumberFormatException e) {
                logger.warn(": {}", statusStr);
            }
        }

        criteria.setOrderByColumn(req.getParameter("orderByColumn"));
        String ascStr = req.getParameter("ascending");
        if (ascStr != null && !ascStr.isBlank()) {
            criteria.setAscending(Boolean.parseBoolean(ascStr));
        }

        String pageParam = req.getParameter("pageNumber");
        if (pageParam == null || pageParam.isBlank()) {
            pageParam = req.getParameter("page");
        }

        criteria.setPageNumber(parseIntegerWithDefault(pageParam, 1));
        criteria.setPageSize(parseIntegerWithDefault(req.getParameter("pageSize"), 20));

        return criteria;
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

    private int parseIntegerWithDefault(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
