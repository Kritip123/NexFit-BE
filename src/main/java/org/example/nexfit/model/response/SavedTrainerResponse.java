package org.example.nexfit.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedTrainerResponse {
    private String trainerId;
    private LocalDateTime savedAt;
    private Integer matchPercentage;
    private Boolean isSuperLike;

    // Trainer details for enhanced response
    private String trainerName;
    private String trainerProfileImage;
    private BigDecimal trainerRating;
    private BigDecimal trainerHourlyRate;
    private Set<String> trainerSpecializations;
    private String trainerCity;
    private Integer trainerExperience;
}
