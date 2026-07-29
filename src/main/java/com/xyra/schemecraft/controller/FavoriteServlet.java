package com.xyra.schemecraft.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.xyra.schemecraft.util.ServletUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyra.schemecraft.exception.EntityNotFoundException;
import com.xyra.schemecraft.exception.ServiceException;
import com.xyra.schemecraft.model.AccountBean;
import com.xyra.schemecraft.model.ProductBean;
import com.xyra.schemecraft.dto.UserSession;
import com.xyra.schemecraft.service.FavoriteService;
import com.xyra.schemecraft.util.JsonUtils;

@WebServlet(name = "FavoriteServlet", urlPatterns = {"/favorites/*"})
public class FavoriteServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteServlet.class);

    private FavoriteService favoriteService;

    public FavoriteServlet() {
        super();
    }

    public FavoriteServlet(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        if (this.favoriteService == null) {
            this.favoriteService = new FavoriteService();
        }
        logger.info("FavoriteServlet successfully initialized.");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = getActionPath(req);

        switch (action) {
            case "/list" -> handleListFavorites(req, resp);
            case "/check" -> handleCheckFavorite(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND, "The requested resource was not found.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = getActionPath(req);

        switch (action) {
            case "/add" -> handleAddFavorite(req, resp);
            case "/remove" -> handleRemoveFavorite(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    "HTTP method not allowed for this endpoint.");
        }
    }

    private void handleListFavorites(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AccountBean authenticatedAccount = getAuthenticatedAccount(req);
        if (authenticatedAccount == null) {
            JsonUtils.sendError(resp, "You must be logged in to view your favorites.", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            List<ProductBean> favorites = favoriteService.listFavorites(authenticatedAccount.getAccountId());
            JsonUtils.sendSuccess(resp, "Favorites retrieved successfully.", "favorites", favorites);
        } catch (IllegalArgumentException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (ServiceException e) {
            logger.error("Error while listing favorites for user: {}", authenticatedAccount.getAccountId(), e);
            JsonUtils.sendError(resp, "Unable to retrieve favorites.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleCheckFavorite(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AccountBean authenticatedAccount = getAuthenticatedAccount(req);
        if (authenticatedAccount == null) {
            JsonUtils.sendSuccess(resp, null, "isFavorite", false);
            return;
        }

        String productId = req.getParameter("productId");
        if (isNullOrBlank(productId)) {
            JsonUtils.sendError(resp, "Parameter productId is required.", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            boolean isFav = favoriteService.isFavorite(authenticatedAccount.getAccountId(), productId);
            JsonUtils.sendSuccess(resp, null, "isFavorite", isFav);
        } catch (IllegalArgumentException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (EntityNotFoundException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (ServiceException e) {
            logger.error("Error checking favorite status for product {} and user {}",
                    productId, authenticatedAccount.getAccountId(), e);
            JsonUtils.sendError(resp, "Unable to check favorite status.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleAddFavorite(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AccountBean authenticatedAccount = getAuthenticatedAccount(req);
        if (authenticatedAccount == null) {
            JsonUtils.sendError(resp, "You must be logged in to add favorites.", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String productId = req.getParameter("productId");
        if (isNullOrBlank(productId)) {
            JsonUtils.sendError(resp, "Parameter productId is required.", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            favoriteService.addFavorite(authenticatedAccount.getAccountId(), productId);
            JsonUtils.sendSuccess(resp, "Product added to favorites.");
        } catch (IllegalArgumentException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (EntityNotFoundException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (ServiceException e) {
            logger.error("Error adding product {} to favorites for user {}",
                    productId, authenticatedAccount.getAccountId(), e);
            JsonUtils.sendError(resp, "Unable to add product to favorites.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleRemoveFavorite(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AccountBean authenticatedAccount = getAuthenticatedAccount(req);
        if (authenticatedAccount == null) {
            JsonUtils.sendError(resp, "You must be logged in to remove favorites.", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String productId = req.getParameter("productId");
        if (isNullOrBlank(productId)) {
            JsonUtils.sendError(resp, "Parameter productId is required.", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            favoriteService.removeFavorite(authenticatedAccount.getAccountId(), productId);
            JsonUtils.sendSuccess(resp, "Product removed from favorites.");
        } catch (IllegalArgumentException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (EntityNotFoundException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (ServiceException e) {
            logger.error("Error removing product {} from favorites for user {}",
                    productId, authenticatedAccount.getAccountId(), e);
            JsonUtils.sendError(resp, "Unable to remove product from favorites.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private String getActionPath(HttpServletRequest req) {
        return ServletUtils.getActionPath(req);
    }

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
}
