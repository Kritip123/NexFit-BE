package org.example.nexfit.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.nexfit.entity.enums.InteractionSource;
import org.example.nexfit.entity.enums.InteractionType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteractionRequest {

    @NotBlank(message = "Trainer ID is required")
    private String trainerId;

    @NotNull(message = "Interaction type is required")
    private InteractionType interactionType;

    private InteractionSource source;

    @Min(0)
    @Max(100)
    private Integer matchScore;

    private String sessionId;

    private Long viewDurationMs;
}
