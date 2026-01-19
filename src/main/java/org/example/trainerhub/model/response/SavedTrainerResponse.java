package org.example.trainerhub.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedTrainerResponse {
    private String trainerId;
    private LocalDateTime savedAt;
    private Integer matchPercentage;
    private Boolean isSuperLike;
}
