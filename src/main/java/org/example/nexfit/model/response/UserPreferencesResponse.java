package org.example.nexfit.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.nexfit.entity.User;

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

    // Two-step onboarding categories
    private List<String> selectedCategories;
    private List<String> selectedSubcategories;

    // User location
    private Double latitude;
    private Double longitude;
}
