package org.example.nexfit.model.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ReviewRequest {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateReviewRequest {
        @NotBlank(message = "Trainer ID is required")
        private String trainerId;
        
        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating cannot be more than 5")
        private Integer rating;
        
        @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
        private String comment;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateReviewRequest {
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating cannot be more than 5")
        private Integer rating;
        
        @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
        private String comment;
    }
}
