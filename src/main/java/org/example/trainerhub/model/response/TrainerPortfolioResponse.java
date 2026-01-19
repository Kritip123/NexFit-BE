package org.example.trainerhub.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.trainerhub.entity.Trainer;
import org.example.trainerhub.model.dto.ReviewDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerPortfolioResponse {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String profileImage;
    private String coverImage;
    private BigDecimal hourlyRate;
    private BigDecimal rating;
    private Integer reviewCount;
    private Integer experience;
    private Integer totalClients;
    private Integer transformations;
    private Integer sessionsCompleted;
    private Integer yearsActive;
    private String bio;
    private String gymAffiliation;
    private Set<String> specializations;
    private List<String> certifications;
    private Set<String> languages;
    private TrainerLocation location;
    private List<Trainer.TrainerImage> gallery;
    private List<Trainer.Achievement> achievements;
    private List<Trainer.TrainingLocation> trainingLocations;
    private List<ReviewDTO> reviews;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrainerLocation {
        private Double latitude;
        private Double longitude;
        private String address;
        private String city;
        private String state;
        private String country;
        private String zipCode;
    }
}
