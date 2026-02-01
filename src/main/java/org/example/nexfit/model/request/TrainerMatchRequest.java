package org.example.nexfit.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.nexfit.entity.User;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerMatchRequest {
    private Double latitude;
    private Double longitude;
    private List<String> activities;
    private List<String> goals;
    private User.ExperienceLevel experienceLevel;
    private User.TrainerGenderPreference trainerGender;
}
