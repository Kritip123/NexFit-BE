package org.example.nexfit.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerComparisonCard {

    private String id;
    private String name;
    private String profileImage;
    private String bio;

    private Set<String> specializations;
    private BigDecimal rating;
    private Integer reviewCount;
    private BigDecimal hourlyRate;
    private Double distance;
    private Integer matchScore;

    private Integer experience;
    private String city;
    private String state;

    // Interaction info
    private LocalDateTime savedAt;
    private LocalDateTime viewedAt;
    private LocalDateTime skippedAt;

    // Primary contact
    private TrainerFeedCard.ContactInfo primaryContact;
}
