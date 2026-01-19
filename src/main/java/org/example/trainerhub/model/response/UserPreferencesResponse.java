package org.example.trainerhub.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.trainerhub.entity.User;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferencesResponse {
    private List<String> selectedActivities;
    private List<String> fitnessGoals;
    private User.TrainerGenderPreference trainerGenderPreference;
    private User.ExperienceLevel experienceLevel;
    private User.Gender gender;
    private LocalDate dateOfBirth;
}
