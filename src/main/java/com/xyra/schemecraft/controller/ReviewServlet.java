package com.xyra.schemecraft.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.xyra.schemecraft.model.UserSession;
import com.xyra.schemecraft.util.ServletUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyra.schemecraft.dto.ReviewRequest;
import com.xyra.schemecraft.exception.EntityNotFoundException;
import com.xyra.schemecraft.exception.ServiceException;
import com.xyra.schemecraft.model.AccountBean;
import com.xyra.schemecraft.model.ReviewBean;
import com.xyra.schemecraft.service.ReviewService;
import com.xyra.schemecraft.util.JsonUtils;

@WebServlet(name = "ReviewServlet", urlPatterns = {"/reviews/*"})
public class ReviewServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ReviewServlet.class);

    private ReviewService reviewService;

    public ReviewServlet() {
        super();
    }

    public ReviewServlet(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        if (this.reviewService == null) {
            this.reviewService = new ReviewService();
        }
        logger.info("ReviewServlet successfully initialized.");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = getActionPath(req);

        if (action.equals("/list")) {
            handleListReviews(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "The requested resource was not found.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = getActionPath(req);

        if (action.equals("/add")) {
            handleAddReview(req, resp);
        } else if (action.equals("/delete")) {
            handleDeleteReview(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    "HTTP method not allowed for this endpoint.");
        }
    }

    // =========================================================================
    // ACTION HANDLERS
    // =========================================================================

    private void handleListReviews(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String productId = req.getParameter("productId");
        String pageParam = req.getParameter("page");

        if (isNullOrBlank(productId)) {
            JsonUtils.sendError(resp, "Parameter productId is required.", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int page = 1;
        if (!isNullOrBlank(pageParam)) {
            try {
                page = Integer.parseInt(pageParam);
                if (page < 1) {
                    page = 1;
                }
            } catch (NumberFormatException e) {
                JsonUtils.sendError(resp, "Invalid page parameter.", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        }

        try {
            List<ReviewBean> reviews = reviewService.listProductReviews(productId, page);
            JsonUtils.sendSuccess(resp, "Reviews retrieved successfully.", "reviews", reviews);
        } catch (IllegalArgumentException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (EntityNotFoundException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (ServiceException e) {
            logger.error("Error while fetching reviews for product: {}", productId, e);
            JsonUtils.sendError(resp, "Unable to retrieve reviews.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleAddReview(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AccountBean authenticatedAccount = getAuthenticatedAccount(req);
        if (authenticatedAccount == null) {
            JsonUtils.sendError(resp, "You must be logged in to leave a review.", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String productId = req.getParameter("productId");
        String ratingParam = req.getParameter("rating");
        String comment = req.getParameter("comment");

        if (isNullOrBlank(productId) || isNullOrBlank(ratingParam)) {
            JsonUtils.sendError(resp, "Missing required parameters: productId and rating.", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int rating;
        try {
            rating = Integer.parseInt(ratingParam);
            if (rating < 1 || rating > 5) {
                JsonUtils.sendError(resp, "Rating must be between 1 and 5.", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        } catch (NumberFormatException e) {
            JsonUtils.sendError(resp, "Invalid rating value.", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        ReviewRequest reviewRequest = new ReviewRequest(
                authenticatedAccount.getAccountId(),
                productId,
                rating,
                comment != null ? comment : ""
        );

        try {
            reviewService.addReview(reviewRequest);
            reviewService.addReview(reviewRequest);
            JsonUtils.sendSuccess(resp, "Review added successfully.");
        } catch (IllegalArgumentException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (EntityNotFoundException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (ServiceException e) {
            logger.error("Error while adding review for product {} by user {}",
                    productId, authenticatedAccount.getAccountId(), e);
            JsonUtils.sendError(resp, "Unable to add review.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleDeleteReview(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AccountBean authenticatedAccount = getAuthenticatedAccount(req);
        if (authenticatedAccount == null) {
            JsonUtils.sendError(resp, "You must be logged in to delete a review.", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String productId = req.getParameter("productId");

        if (isNullOrBlank(productId)) {
            JsonUtils.sendError(resp, "Parameter productId is required.", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            reviewService.deleteReview(authenticatedAccount.getAccountId(), productId);
            JsonUtils.sendSuccess(resp, "Review deleted successfully.");
        } catch (IllegalArgumentException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (EntityNotFoundException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (ServiceException e) {
            logger.error("Error while deleting review for product {} by user {}",
                    productId, authenticatedAccount.getAccountId(), e);
            JsonUtils.sendError(resp, "Unable to delete review.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // =========================================================================
    // UTILITY / HELPER METHODS
    // =========================================================================

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
