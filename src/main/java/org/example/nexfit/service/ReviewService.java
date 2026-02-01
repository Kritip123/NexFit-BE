package org.example.nexfit.service;

import org.example.nexfit.model.dto.ReviewDTO;
import org.example.nexfit.model.request.ReviewRequest;
import org.example.nexfit.model.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    
    ReviewDTO createReview(String userId, ReviewRequest.CreateReviewRequest request);
    
    PageResponse<ReviewDTO> getTrainerReviews(String trainerId, Pageable pageable);
    
    ReviewDTO updateReview(String reviewId, String userId, ReviewRequest.UpdateReviewRequest request);
    
    void deleteReview(String reviewId, String userId);
}
