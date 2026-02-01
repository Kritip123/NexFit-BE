package org.example.nexfit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.nexfit.model.dto.ReviewDTO;
import org.example.nexfit.model.request.ReviewRequest;
import org.example.nexfit.model.response.PageResponse;
import org.example.nexfit.service.ReviewService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Review management APIs")
public class ReviewController {
    
    private final ReviewService reviewService;
    
    @PostMapping
    @Operation(summary = "Create review")
    public ResponseEntity<ReviewDTO> createReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReviewRequest.CreateReviewRequest request
    ) {
        String userId = userDetails.getUsername(); // This is the email
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(userId, request));
    }
    
    @GetMapping("/trainer/{trainerId}")
    @Operation(summary = "Get trainer reviews")
    public ResponseEntity<PageResponse<ReviewDTO>> getTrainerReviews(
            @PathVariable String trainerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "recent") String sortBy
    ) {
        return ResponseEntity.ok(reviewService.getTrainerReviews(trainerId, PageRequest.of(page, limit)));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update review")
    public ResponseEntity<ReviewDTO> updateReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id,
            @Valid @RequestBody ReviewRequest.UpdateReviewRequest request
    ) {
        String userId = userDetails.getUsername();
        return ResponseEntity.ok(reviewService.updateReview(id, userId, request));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete review")
    public ResponseEntity<Map<String, String>> deleteReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id
    ) {
        String userId = userDetails.getUsername();
        reviewService.deleteReview(id, userId);
        return ResponseEntity.ok(Map.of("message", "Review deleted successfully"));
    }
}
