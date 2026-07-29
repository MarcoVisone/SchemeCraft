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

import com.xyra.schemecraft.constant.ServiceConstants;
import com.xyra.schemecraft.dto.CartLineItem;
import com.xyra.schemecraft.exception.*;
import com.xyra.schemecraft.model.CurrencyBean;
import com.xyra.schemecraft.service.LookupService;
import com.xyra.schemecraft.util.CookieUtils;
import com.xyra.schemecraft.util.JsonUtils;
import com.xyra.schemecraft.util.ServletUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyra.schemecraft.model.AccountBean;
import com.xyra.schemecraft.dto.UserSession;
import com.xyra.schemecraft.service.CartService;
import com.xyra.schemecraft.service.OrderService;

@WebServlet(name = "CartServlet", urlPatterns = {"/cart/*"})
public class CartServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CartServlet.class);

    private CartService cartService;
    private OrderService orderService;
    private LookupService lookupService;

    public CartServlet() {
        super();
    }

    public CartServlet(CartService cartService, OrderService orderService, LookupService lookupService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.lookupService = lookupService;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        if (this.cartService == null) {
            this.cartService = new CartService();
        }
        if (this.orderService == null) {
            this.orderService = new OrderService();
        }
        if(this.lookupService == null) {
            this.lookupService = new LookupService();
        }
        logger.info("CartServlet successfully initialized.");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        configureEncoding(req, resp);

        String action = getActionPath(req);

        switch (action) {
            case "", "/", "/view", "/items" -> handleViewCart(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND, "The requested resource was not found.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        configureEncoding(req, resp);

        String action = getActionPath(req);

        switch (action) {
            case "/add" -> handleAddToCart(req, resp);
            case "/remove" -> handleRemoveFromCart(req, resp);
            case "/clear" -> handleClearCart(req, resp);
            case "/checkout" -> handleCheckout(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "HTTP method not allowed for this endpoint.");
        }
    }

    // =========================================================================
    // GET HANDLERS
    // =========================================================================


    private void handleViewCart(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String format = req.getParameter("format");
        AccountBean currentAccount = getAuthenticatedAccount(req);

        String currencySymbol = ServiceConstants.DEFAULT_CURRENCY_SYMBOL;
        if (currentAccount != null && currentAccount.getCurrencyId() != null) {
            currencySymbol = lookupService.getCurrencyById(currentAccount.getCurrencyId())
                    .map(CurrencyBean::getSymbol)
                    .orElse(ServiceConstants.DEFAULT_CURRENCY_SYMBOL);
        }

        try {
            List<CartLineItem> cartItems = (currentAccount != null)
                    ? cartService.viewCart(currentAccount.getAccountId())
                    : cartService.resolveCartLineItems(CookieUtils.getCartProductIds(req));

            if ("json".equalsIgnoreCase(format) || isAjaxRequest(req)) {
                resp.setContentType("application/json");
                JSONObject jsonResponse = new JSONObject();
                JSONArray array = new JSONArray();

                for (CartLineItem item : cartItems) {
                    JSONObject obj = JsonUtils.serializeProduct(item.product());
                    obj.put("coverImagePath", item.coverImagePath());
                    array.put(obj);
                }

                jsonResponse.put("success", true);
                jsonResponse.put("items", array);
                jsonResponse.put("count", cartItems.size());
                jsonResponse.put("currencySymbol", currencySymbol);
                resp.getWriter().print(jsonResponse.toString());
            } else {
                req.setAttribute("cartItems", cartItems);
                req.setAttribute("currencySymbol", currencySymbol);
                req.getRequestDispatcher("WEB-INF/cart/cart.jsp").forward(req, resp);
            }

        } catch (IllegalArgumentException | EntityNotFoundException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (ServiceException e) {
            logger.error("Error viewing cart", e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to retrieve cart items.");
        }
    }

    // =========================================================================
    // POST HANDLERS
    // =========================================================================

    private void handleAddToCart(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject jsonResponse = new JSONObject();

        String productId = req.getParameter("productId");
        AccountBean currentAccount = getAuthenticatedAccount(req);

        try {
            if (currentAccount != null) {
                cartService.addToCart(currentAccount.getAccountId(), productId);
            } else {
                addToGuestCart(req, resp, productId);
            }

            jsonResponse.put("success", true);
            jsonResponse.put("message", "Product added to cart successfully.");
            out.print(jsonResponse.toString());

        } catch (IllegalArgumentException | EntityNotFoundException | DuplicateEntityException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (ServiceException e) {
            logger.error("Error adding product {} to cart", productId, e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to add product to cart.");
        }
    }

    private void handleRemoveFromCart(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject jsonResponse = new JSONObject();

        String productId = req.getParameter("productId");
        AccountBean currentAccount = getAuthenticatedAccount(req);

        try {
            if (currentAccount != null) {
                cartService.removeFromCart(currentAccount.getAccountId(), productId);
            } else {
                removeFromGuestCart(req, resp, productId);
            }

            jsonResponse.put("success", true);
            jsonResponse.put("message", "Product removed from cart successfully.");
            out.print(jsonResponse.toString());

        } catch (EntityNotFoundException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (ServiceException e) {
            logger.error("Error removing product {} from cart", productId, e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to remove product from cart.");
        }
    }

    private void handleClearCart(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject jsonResponse = new JSONObject();

        AccountBean currentAccount = getAuthenticatedAccount(req);

        try {
            if (currentAccount != null) {
                cartService.clearCart(currentAccount.getAccountId());
            } else {
                CookieUtils.clearCartCookie(resp, req.getContextPath());
            }

            jsonResponse.put("success", true);
            jsonResponse.put("message", "Cart cleared successfully.");
            out.print(jsonResponse.toString());

        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (ServiceException e) {
            logger.error("Error clearing cart", e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to clear cart.");
        }
    }

    private void handleCheckout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AccountBean currentAccount = getAuthenticatedAccount(req);
        if (currentAccount == null) {
            // Guests cannot check out: no account means no address/payment method to charge.
            handleUnauthorized(req, resp);
            return;
        }

        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JSONObject jsonResponse = new JSONObject();

        try {
            String transactionId = orderService.placeOrderFromCart(currentAccount.getAccountId());

            jsonResponse.put("success", true);
            jsonResponse.put("message", "Order placed successfully.");
            jsonResponse.put("transactionId", transactionId);
            out.print(jsonResponse.toString());

        } catch (EntityNotFoundException e) {
            // Missing default address, payment method, or empty cart
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (InsufficientStockException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (PaymentDeclinedException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_PAYMENT_REQUIRED, e.getMessage());
        } catch (ServiceException e) {
            logger.error("Error processing checkout for account: {}", currentAccount.getAccountId(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to complete checkout.");
        }
    }

    // =========================================================================
    // GUEST CART HELPERS (cookie-based, no DB access)
    // =========================================================================

    private void addToGuestCart(HttpServletRequest req, HttpServletResponse resp, String productId) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId cannot be null or blank");
        }

        List<String> guestCart = new java.util.ArrayList<>(CookieUtils.getCartProductIds(req));
        if (!guestCart.contains(productId)) {
            guestCart.add(productId);
        }
        CookieUtils.setCartCookie(resp, guestCart, req.getContextPath());
    }

    private void removeFromGuestCart(HttpServletRequest req, HttpServletResponse resp, String productId) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId cannot be null or blank");
        }

        List<String> guestCart = new java.util.ArrayList<>(CookieUtils.getCartProductIds(req));
        if (!guestCart.remove(productId)) {
            throw new EntityNotFoundException("Product not found in guest cart",
                    EntityNotFoundException.EntityType.PRODUCT);
        }
        CookieUtils.setCartCookie(resp, guestCart, req.getContextPath());
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
