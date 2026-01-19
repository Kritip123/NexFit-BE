package org.example.trainerhub.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.trainerhub.entity.Review;
import org.example.trainerhub.entity.Trainer;
import org.example.trainerhub.entity.User;
import org.example.trainerhub.exception.BusinessException;
import org.example.trainerhub.exception.ResourceNotFoundException;
import org.example.trainerhub.model.dto.ReviewDTO;
import org.example.trainerhub.model.request.ReviewRequest;
import org.example.trainerhub.model.response.PageResponse;
import org.example.trainerhub.repository.ReviewRepository;
import org.example.trainerhub.repository.TrainerRepository;
import org.example.trainerhub.repository.UserRepository;
import org.example.trainerhub.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final TrainerRepository trainerRepository;
    
    @Override
    public ReviewDTO createReview(String userId, ReviewRequest.CreateReviewRequest request) {
        // Get user
        User user = userRepository.findByEmail(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Get trainer
        Trainer trainer = trainerRepository.findById(request.getTrainerId())
                .orElseThrow(() -> new ResourceNotFoundException("Trainer", request.getTrainerId()));
        
        // Create review
        Review review = Review.builder()
                .trainerId(trainer.getId())
                .userId(user.getId())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
        
        review = reviewRepository.save(review);
        
        // Update trainer rating
        updateTrainerRating(trainer);
        
        log.info("Review created for trainer {} by user {}", trainer.getId(), user.getId());
        
        return convertToDTO(review);
    }
    
    @Override
    public PageResponse<ReviewDTO> getTrainerReviews(String trainerId, Pageable pageable) {
        Page<Review> reviewsPage = reviewRepository.findByTrainerId(trainerId, pageable);
        
        return PageResponse.<ReviewDTO>builder()
                .data(reviewsPage.getContent().stream()
                        .map(this::convertToDTO)
                        .toList())
                .pagination(PageResponse.PaginationInfo.builder()
                        .page(reviewsPage.getNumber())
                        .limit(reviewsPage.getSize())
                        .total(reviewsPage.getTotalElements())
                        .totalPages(reviewsPage.getTotalPages())
                        .hasNext(reviewsPage.hasNext())
                        .hasPrevious(reviewsPage.hasPrevious())
                        .build())
                .build();
    }
    
    @Override
    public ReviewDTO updateReview(String reviewId, String userId, ReviewRequest.UpdateReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));
        
        // Verify user owns the review
        User user = userRepository.findByEmail(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (!review.getUserId().equals(user.getId())) {
            throw new BusinessException("You can only update your own reviews");
        }
        
        // Update review fields
        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        
        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }
        
        review = reviewRepository.save(review);
        
        // Update trainer rating if rating changed
        if (request.getRating() != null) {
            updateTrainerRating(trainerRepository.findById(review.getTrainerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Trainer not found")));
        }
        
        log.info("Review {} updated by user {}", reviewId, user.getId());
        
        return convertToDTO(review);
    }
    
    @Override
    public void deleteReview(String reviewId, String userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));
        
        // Verify user owns the review
        User user = userRepository.findByEmail(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (!review.getUserId().equals(user.getId())) {
            throw new BusinessException("You can only delete your own reviews");
        }
        Trainer trainer = trainerRepository.findById(review.getTrainerId())
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));
        
        reviewRepository.deleteById(reviewId);
        
        // Update trainer rating
        updateTrainerRating(trainer);
        
        log.info("Review {} deleted by user {}", reviewId, user.getId());
    }
    
    private void updateTrainerRating(Trainer trainer) {
        var reviews = reviewRepository.findByTrainerId(trainer.getId());
        if (reviews.isEmpty()) {
            trainer.setRating(BigDecimal.ZERO);
            trainer.setReviewCount(0);
        } else {
            double averageRating = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            trainer.setRating(BigDecimal.valueOf(averageRating).setScale(2, RoundingMode.HALF_UP));
            trainer.setReviewCount(reviews.size());
        }
        
        trainerRepository.save(trainer);
    }
    
    private ReviewDTO convertToDTO(Review review) {
        User user = userRepository.findById(review.getUserId())
                .orElse(null);

        ReviewDTO.UserInfo userInfo = ReviewDTO.UserInfo.builder()
                .id(review.getUserId())
                .name(user != null ? user.getName() : "Unknown")
                .avatar(user != null ? user.getAvatar() : null)
                .build();

        return ReviewDTO.builder()
                .id(review.getId())
                .user(userInfo)
                .trainerId(review.getTrainerId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
