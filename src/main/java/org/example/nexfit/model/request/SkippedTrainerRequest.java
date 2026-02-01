package org.example.nexfit.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkippedTrainerRequest {
    
    @NotBlank(message = "Trainer ID is required")
    private String trainerId;
}
