package org.example.nexfit.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerDTO {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String profileImage;
    private String coverImage;
    private Set<String> specializations;
    private Integer experience;
    private BigDecimal rating;
    private Integer reviewCount;
    private BigDecimal hourlyRate;
    private String bio;
    private List<String> certifications;
    private String instagramId;
    private LocationDTO location;
    private Double distance; // Distance from user
    private Set<String> languages;
    private String gymAffiliation;
    private StatsDTO stats;
    private List<String> gallery;
    private Map<String, List<String>> availability;
    private String whatsapp;
    private String website;
    private List<ContactMethodDTO> contactMethods;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContactMethodDTO {
        private String type;
        private String value;
        private String label;
        private Boolean isPrimary;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationDTO {
        private Double latitude;
        private Double longitude;
        private String address;
        private String city;
        private String state;
        private String country;
        private String zipCode;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatsDTO {
        private Integer totalClients;
        private Integer transformations;
        private Integer sessionsCompleted;
        private Integer yearsActive;
    }
}
