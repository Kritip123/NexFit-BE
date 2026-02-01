package org.example.nexfit.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document(collection = "trainers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trainer {
    
    @Id
    private String id;
    
    private String name;
    
    @Indexed(unique = true)
    private String email;
    
    private String phone;

    private User.Gender gender;
    
    private String profileImage;
    
    private String coverImage;
    
    @Builder.Default
    private Set<String> specializations = new HashSet<>();
    
    private Integer experience; // in years
    
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;
    
    @Builder.Default
    private Integer reviewCount = 0;
    
    private BigDecimal hourlyRate;
    
    private String bio;
    
    @Builder.Default
    private List<String> certifications = new ArrayList<>();
    
    private String instagramId;
    
    // Location fields
    private Double latitude;
    private Double longitude;
    private String address;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    
    @Builder.Default
    private Set<String> languages = new HashSet<>();
    
    private String gymAffiliation;
    
    // Statistics
    @Builder.Default
    private Integer totalClients = 0;
    
    @Builder.Default
    private Integer transformations = 0;
    
    @Builder.Default
    private Integer sessionsCompleted = 0;
    
    @Builder.Default
    private Integer yearsActive = 0;
    
    @Builder.Default
    private List<TrainerImage> gallery = new ArrayList<>();

    @Builder.Default
    private List<Achievement> achievements = new ArrayList<>();

    @Builder.Default
    private List<TrainingLocation> trainingLocations = new ArrayList<>();

    // Contact methods
    private String whatsapp;
    private String website;

    @Builder.Default
    private List<ContactMethod> contactMethods = new ArrayList<>();

    @Builder.Default
    private Boolean isActive = true;
    
    @Builder.Default
    private Boolean isVerified = false;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrainerImage {
        private String url;
        private Integer likes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Achievement {
        private String title;
        private String description;
        private Integer year;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrainingLocation {
        private String name;
        private String address;
        private String city;
        private String state;
        private String country;
        private Double latitude;
        private Double longitude;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContactMethod {
        private String type; // whatsapp, website, email, phone, instagram
        private String value;
        private String label;
        private Boolean isPrimary;
    }
}
