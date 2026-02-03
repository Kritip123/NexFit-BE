package org.example.nexfit.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerProfileResponse {

    private String id;
    private String status;
    private Profile profile;
    private List<CertificateInfo> certificates;
    private List<MediaInfo> media;
    private boolean hasDiscoverVideo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Profile {
        private String fullName;
        private String email;
        private String phone;
        private String city;
        private String headline;
        private String bio;
        private String profileImage;
        private String coverImage;
        private List<String> specializations;
        private Integer yearsActive;
        private List<String> languages;
        private Pricing pricing;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pricing {
        private Integer monthlySubscriptionUSD;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificateInfo {
        private String id;
        private String name;
        private String issuer;
        private LocalDate issuedDate;
        private LocalDate expiresDate;
        private String fileUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MediaInfo {
        private String id;
        private String type;
        private String url;
        private String thumbnailUrl;
        private Integer durationSec;
        private Integer order;
    }
}
