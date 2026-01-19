package org.example.trainerhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.trainerhub.model.dto.TrainerDTO;
import org.example.trainerhub.model.request.TrainerMatchRequest;
import org.example.trainerhub.model.request.TrainerSearchRequest;
import org.example.trainerhub.model.response.MatchedTrainerResponse;
import org.example.trainerhub.model.response.PageResponse;
import org.example.trainerhub.model.response.TrainerPortfolioResponse;
import org.example.trainerhub.service.ReviewService;
import org.example.trainerhub.service.TrainerService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/trainers")
@RequiredArgsConstructor
@Tag(name = "Trainers", description = "Trainer management APIs")
public class TrainerController {
    
    private final TrainerService trainerService;
    private final ReviewService reviewService;
    
    @GetMapping
    @Operation(summary = "Get all trainers with filters")
    public ResponseEntity<PageResponse<TrainerDTO>> getTrainers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> specializations,
            @RequestParam(required = false) Double maxDistance,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false, defaultValue = "distance") String sortBy,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        TrainerSearchRequest searchRequest = TrainerSearchRequest.builder()
                .search(search)
                .specializations(specializations)
                .maxDistance(maxDistance)
                .minRating(minRating)
                .maxPrice(maxPrice)
                .sortBy(sortBy)
                .latitude(lat)
                .longitude(lng)
                .build();
        
        Pageable pageable = PageRequest.of(page, limit);
        return ResponseEntity.ok(trainerService.searchTrainers(searchRequest, pageable));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get trainer by ID")
    public ResponseEntity<TrainerDTO> getTrainerById(@PathVariable String id) {
        return ResponseEntity.ok(trainerService.getTrainerById(id));
    }
    
    @GetMapping("/{id}/availability")
    @Operation(summary = "Get trainer availability")
    public ResponseEntity<?> getTrainerAvailability(
            @PathVariable String id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (date == null) {
            date = LocalDate.now();
        }
        
        // Check if requesting specific date or weekly availability
        if (date.equals(LocalDate.now())) {
            // Return weekly availability
            Map<String, List<String>> weeklyAvailability = trainerService.getTrainerAvailability(id, date);
            return ResponseEntity.ok(weeklyAvailability);
        } else {
            // Return specific date availability
            List<String> slots = trainerService.getAvailableTimeSlots(id, date);
            return ResponseEntity.ok(Map.of(
                "date", date.toString(),
                "slots", slots
            ));
        }
    }
    
    @GetMapping("/{id}/reviews")
    @Operation(summary = "Get trainer reviews")
    public ResponseEntity<?> getTrainerReviews(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "recent") String sortBy
    ) {
        return ResponseEntity.ok(reviewService.getTrainerReviews(id, PageRequest.of(page, limit)));
    }
    
    @GetMapping("/matched")
    @Operation(summary = "Get matched trainers")
    public ResponseEntity<PageResponse<MatchedTrainerResponse>> getMatchedTrainers(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) String activities,
            @RequestParam(required = false) String goals,
            @RequestParam(required = false) String experienceLevel,
            @RequestParam(required = false) String trainerGender,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        TrainerMatchRequest request = TrainerMatchRequest.builder()
                .latitude(lat)
                .longitude(lng)
                .activities(splitCsv(activities))
                .goals(splitCsv(goals))
                .experienceLevel(parseExperienceLevel(experienceLevel))
                .trainerGender(parseTrainerGender(trainerGender))
                .build();
        
        return ResponseEntity.ok(trainerService.getMatchedTrainers(request, PageRequest.of(page, limit)));
    }
    
    @GetMapping("/{id}/portfolio")
    @Operation(summary = "Get trainer portfolio")
    public ResponseEntity<TrainerPortfolioResponse> getTrainerPortfolio(@PathVariable String id) {
        return ResponseEntity.ok(trainerService.getTrainerPortfolio(id));
    }

    private List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return List.of(csv.split(",")).stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toList());
    }
    
    private org.example.trainerhub.entity.User.ExperienceLevel parseExperienceLevel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return org.example.trainerhub.entity.User.ExperienceLevel.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    private org.example.trainerhub.entity.User.TrainerGenderPreference parseTrainerGender(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return org.example.trainerhub.entity.User.TrainerGenderPreference.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
