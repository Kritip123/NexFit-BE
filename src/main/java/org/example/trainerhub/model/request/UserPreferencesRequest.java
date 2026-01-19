package org.example.trainerhub.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.trainerhub.entity.User;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferencesRequest {
    
    @NotNull(message = "Selected activities are required")
    private List<String> selectedActivities;
    
    @NotNull(message = "Fitness goals are required")
    private List<String> fitnessGoals;
    
    @NotNull(message = "Trainer gender preference is required")
    private User.TrainerGenderPreference trainerGenderPreference;
    
    @NotNull(message = "Experience level is required")
    private User.ExperienceLevel experienceLevel;
}
