package com.xyra.schemecraft.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.xyra.schemecraft.util.JsonUtils;
import com.xyra.schemecraft.util.ServletUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyra.schemecraft.exception.EntityNotFoundException;
import com.xyra.schemecraft.exception.ServiceException;
import com.xyra.schemecraft.model.AccountBean;
import com.xyra.schemecraft.model.ProductBean;
import com.xyra.schemecraft.model.UserSession;
import com.xyra.schemecraft.service.CartService;

@WebServlet(name = "CartServlet", urlPatterns = {"/cart/*"})
public class CartServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CartServlet.class);

    private CartService cartService;

    public CartServlet() {
        super();
    }

    public CartServlet(CartService cartService) {
        this.cartService = cartService;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        if (this.cartService == null) {
            this.cartService = new CartService();
        }
        logger.info("CartServlet successfully initialized.");
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
            case "", "/", "/view", "/items" -> handleViewCart(req, resp, currentAccount.getAccountId());
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
            case "/add" -> handleAddToCart(req, resp, currentAccount.getAccountId());
            case "/remove" -> handleRemoveFromCart(req, resp, currentAccount.getAccountId());
            case "/clear" -> handleClearCart(req, resp, currentAccount.getAccountId());
            default -> resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "HTTP method not allowed for this endpoint.");
        }
    }

    // =========================================================================
    // GET HANDLERS
    // =========================================================================

    private void handleViewCart(HttpServletRequest req, HttpServletResponse resp, String accountId)
            throws ServletException, IOException {
        String format = req.getParameter("format");

        try {
            List<ProductBean> productsInCart = cartService.viewCart(accountId);

            if ("json".equalsIgnoreCase(format) || isAjaxRequest(req)) {
                resp.setContentType("application/json");
                JSONObject jsonResponse = new JSONObject();
                JSONArray array = new JSONArray();

                for (ProductBean product : productsInCart) {
                    array.put(JsonUtils.serializeProduct(product));
                }

                jsonResponse.put("success", true);
                jsonResponse.put("items", array);
                jsonResponse.put("count", productsInCart.size());
                resp.getWriter().print(jsonResponse.toString());
            } else {
                req.setAttribute("cartProducts", productsInCart);
                req.getRequestDispatcher("/cart.jsp").forward(req, resp);
            }

        } catch (IllegalArgumentException | EntityNotFoundException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (ServiceException e) {
            logger.error("Error viewing cart for account: {}", accountId, e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to retrieve cart items.");
        }
    }

    // =========================================================================
    // POST HANDLERS
    // =========================================================================

    private void handleAddToCart(HttpServletRequest req, HttpServletResponse resp, String accountId) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject jsonResponse = new JSONObject();

        String productId = req.getParameter("productId");

        try {
            cartService.addToCart(accountId, productId);

            jsonResponse.put("success", true);
            jsonResponse.put("message", "Product added to cart successfully.");
            out.print(jsonResponse.toString());

        } catch (IllegalArgumentException | EntityNotFoundException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (ServiceException e) {
            logger.error("Error adding product {} to cart for account: {}", productId, accountId, e);
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private void handleRemoveFromCart(HttpServletRequest req, HttpServletResponse resp, String accountId) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject jsonResponse = new JSONObject();

        String productId = req.getParameter("productId");

        try {
            cartService.removeFromCart(accountId, productId);

            jsonResponse.put("success", true);
            jsonResponse.put("message", "Product removed from cart successfully.");
            out.print(jsonResponse.toString());

        } catch (EntityNotFoundException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (ServiceException e) {
            logger.error("Error removing product {} from cart for account: {}", productId, accountId, e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to remove product from cart.");
        }
    }

    private void handleClearCart(HttpServletRequest req, HttpServletResponse resp, String accountId) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject jsonResponse = new JSONObject();

        try {
            cartService.clearCart(accountId);

            jsonResponse.put("success", true);
            jsonResponse.put("message", "Cart cleared successfully.");
            out.print(jsonResponse.toString());

        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (ServiceException e) {
            logger.error("Error clearing cart for account: {}", accountId, e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to clear cart.");
        }
    }

    // =========================================================================
    // HELPER & UTILITY METHODS
    // =========================================================================

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
}
