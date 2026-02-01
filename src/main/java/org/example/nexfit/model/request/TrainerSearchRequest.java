package org.example.nexfit.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerSearchRequest {
    
    private String search;
    private List<String> specializations;
    private Double maxDistance;
    private BigDecimal minRating;
    private BigDecimal maxPrice;
    private String sortBy; // distance, rating, price_low, price_high, experience
    private Double latitude;
    private Double longitude;
}
