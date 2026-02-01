package org.example.nexfit.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedTrainerRequest {
    
    @NotBlank(message = "Trainer ID is required")
    private String trainerId;
    
    @Min(value = 0, message = "Match percentage cannot be negative")
    @Max(value = 100, message = "Match percentage cannot exceed 100")
    private Integer matchPercentage;
    
    private Boolean isSuperLike;
}
