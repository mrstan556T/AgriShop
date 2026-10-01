package com.agrishop.service;

import com.agrishop.dto.ReviewDTO;
import jakarta.ejb.Local;
import java.util.List;
import java.util.Map;

@Local
public interface ReviewServiceLocal {
    List<ReviewDTO> getReviewsLazy(int first, int pageSize, String sortField, String sortOrder, Map<String, Object> filters);
    int countReviews(Map<String, Object> filters);
    void toggleReviewStatus(Long reviewId) throws Exception;
    void approveReview(Long reviewId) throws Exception;
    void hideReview(Long reviewId) throws Exception;
    void replyReview(Long reviewId, String reply, Long adminId) throws Exception;
    List<ReviewDTO> getApprovedReviewsByProduct(Long productId);
    
    ReviewDTO createReview(Long userId, Long productId, int rating, String comment) throws Exception;
    ReviewDTO addReview(Long userId, Long productId, int rating, String comment) throws Exception;
    ReviewDTO createReview(ReviewDTO dto) throws Exception;
    ReviewDTO updateReview(Long userId, Long reviewId, int rating, String comment) throws Exception;
    ReviewDTO updateReview(ReviewDTO dto) throws Exception;
}
