package org.example.nexfit.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.nexfit.entity.TrainerMedia;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerFeedCard {

    private String id;
    private String name;
    private String profileImage;
    private String bio;

    private MediaInfo featuredVideo; // Primary video for reel display
    private List<MediaInfo> media;
    private List<String> gallery; // Gallery image URLs for photo-only mode
    private Set<String> specializations;

    private BigDecimal rating;
    private Integer reviewCount;
    private BigDecimal hourlyRate;
    private Double distance;
    private Integer matchScore;

    private Integer experience;
    private String city;

    private ContactInfo primaryContact;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MediaInfo {
        private String id;
        private String type;
        private String mediaUrl;
        private String thumbnailUrl;
        private String title;
        private Integer durationSeconds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContactInfo {
        private String type;
        private String value;
        private String label;
    }
}
