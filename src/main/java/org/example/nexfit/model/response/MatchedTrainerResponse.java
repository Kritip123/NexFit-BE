package org.example.nexfit.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.nexfit.model.dto.TrainerDTO;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchedTrainerResponse {
    private TrainerDTO trainer;
    private Integer matchPercentage;
    private List<String> matchReasons;
}
