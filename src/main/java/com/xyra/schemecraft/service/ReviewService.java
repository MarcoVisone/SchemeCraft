package com.xyra.schemecraft.service;

import com.xyra.schemecraft.model.ReviewBean;
import com.xyra.schemecraft.exception.DuplicateEntityException;
import com.xyra.schemecraft.exception.EntityNotFoundException;

import java.util.List;

public class ReviewService {

    public void addReview(ReviewBean review) throws DuplicateEntityException {
        throw new UnsupportedOperationException("TODO: addReview");
    }

    public void updateReview(ReviewBean review) throws EntityNotFoundException {
        throw new UnsupportedOperationException("TODO: updateReview");
    }

    public void deleteReview(String accountId, String productId) throws EntityNotFoundException {
        throw new UnsupportedOperationException("TODO: deleteReview");
    }

    public List<ReviewBean> listProductReviews(String productId) {
        throw new UnsupportedOperationException("TODO: listProductReviews");
    }
}