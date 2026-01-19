package org.example.trainerhub.service;

import org.example.trainerhub.model.dto.ReviewDTO;
import org.example.trainerhub.model.request.ReviewRequest;
import org.example.trainerhub.model.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    
    ReviewDTO createReview(String userId, ReviewRequest.CreateReviewRequest request);
    
    PageResponse<ReviewDTO> getTrainerReviews(String trainerId, Pageable pageable);
    
    ReviewDTO updateReview(String reviewId, String userId, ReviewRequest.UpdateReviewRequest request);
    
    void deleteReview(String reviewId, String userId);
}
