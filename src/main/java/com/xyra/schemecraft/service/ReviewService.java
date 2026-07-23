package com.xyra.schemecraft.service;

import com.xyra.schemecraft.connection.ConnectionPool;
import com.xyra.schemecraft.constant.ServiceConstants;
import com.xyra.schemecraft.dao.AccountProductDAO;
import com.xyra.schemecraft.dao.ReviewDAO;
import com.xyra.schemecraft.exception.DAOException;
import com.xyra.schemecraft.exception.ServiceException;
import com.xyra.schemecraft.model.ReviewBean;
import com.xyra.schemecraft.dto.ReviewRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ReviewService {
    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    private final EntityValidator entityValidator;

    private final ReviewDAO reviewDAO;
    private final AccountProductDAO accountProductDAO;

    public ReviewService(){
        this.reviewDAO = new ReviewDAO();
        this.entityValidator = new EntityValidator();
        this.accountProductDAO = new AccountProductDAO();
    }

    public void addReview(ReviewRequest review) {
        if(review == null) {
            throw new IllegalArgumentException("Review cannot be null");
        }
        if(review.accountId() == null || review.accountId().isBlank()) {
            throw new IllegalArgumentException("Account ID cannot be null or blank");
        }
        if(review.productId() == null || review.productId().isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be null or blank");
        }
        try(Connection conn = ConnectionPool.getConnection()) {
            entityValidator.validateActiveAccount(conn, review.accountId());
            entityValidator.validateActiveProduct(conn, review.productId());
            ReviewBean reviewBean = new ReviewBean();
            reviewBean.setAccountId(review.accountId());
            reviewBean.setProductId(review.productId());
            reviewBean.setRating(review.rating());
            reviewBean.setComment(review.comment().isBlank() ? null : review.comment());
            reviewBean.setVerifiedPurchase(accountProductDAO.findById(conn, review.accountId(), review.productId())
                    .isPresent());
            reviewDAO.insert(conn, reviewBean);
            logger.info("Review added for product {} by account {}", review.productId(), review.accountId());
        } catch (SQLException e) {
            logger.info("Database connection error while adding review for accountId: {} and productId: {}", review.accountId(), review.productId(), e);
            throw new ServiceException("Database connection error while adding review", e);
        } catch (DAOException e) {
            logger.info("DAO error while adding review for accountId: {} and productId: {}", review.accountId(), review.productId(), e);
            throw new ServiceException("DAO error while adding review", e);
        }
    }

    public void deleteReview(String rawAccountId, String rawProductId)  {
        String accountId = rawAccountId == null ? null : rawAccountId.trim();
        String productId = rawProductId == null ? null : rawProductId.trim();

        if(accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account ID cannot be null or blank");
        }
        if(productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be null or blank");
        }

        try(Connection conn = ConnectionPool.getConnection()) {
            reviewDAO.delete(conn, accountId, productId);
            logger.info("Review deleted for product {} by account {}", productId, accountId);
        } catch (SQLException e) {
            logger.info("Database connection error while deleting review for accountId: {} and productId: {}", accountId, productId, e);
            throw new ServiceException("Database connection error while deleting review", e);
        } catch (DAOException e) {
            logger.info("DAO error while deleting review for accountId: {} and productId: {}", accountId, productId, e);
            throw new ServiceException("DAO error while deleting review", e);
        }
    }

    public List<ReviewBean> listProductReviews(String rawProductId, int pageNumber) {
        String productId = rawProductId == null ? null : rawProductId.trim();

        if(productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be null or blank");
        }

        try(Connection conn = ConnectionPool.getConnection()) {
            return reviewDAO.findByProductId(conn, productId, pageNumber, ServiceConstants.REVIEWS_PAGE_SIZE);
        } catch (SQLException e) {
            logger.info("Database connection error while listing reviews for productId: {}", productId, e);
            throw new ServiceException("Database connection error while listing reviews", e);
        } catch (DAOException e) {
            logger.info("DAO error while listing reviews for productId: {}", productId, e);
            throw new ServiceException("DAO error while listing reviews", e);
        }
    }
}