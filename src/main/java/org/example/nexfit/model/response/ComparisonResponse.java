package org.example.nexfit.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonResponse {

    private List<TrainerComparisonCard> saved;
    private List<TrainerComparisonCard> suggestedReconsiderations;
    private List<TrainerComparisonCard> recentlyViewed;
}
