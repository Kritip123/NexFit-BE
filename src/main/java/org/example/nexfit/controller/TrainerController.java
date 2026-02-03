package org.example.nexfit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.nexfit.entity.TrainerMedia;
import org.example.nexfit.entity.User;
import org.example.nexfit.model.dto.TrainerDTO;
import org.example.nexfit.model.request.*;
import org.example.nexfit.model.response.*;
import org.example.nexfit.service.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final FeedService feedService;
    private final ComparisonService comparisonService;
    private final TrainerInteractionService interactionService;
    private final TrainerMediaService mediaService;

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

    @GetMapping("/feed")
    @Operation(summary = "Get personalized trainer feed (TikTok-style)")
    public ResponseEntity<FeedResponse> getFeed(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) String sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer limit, // alias for size
            @RequestParam(required = false) Long seed // random seed for consistent shuffle; null = new sequence
    ) {
        String userId = userDetails != null ? ((User) userDetails).getId() : null;
        int effectiveSize = limit != null ? limit : size;
        FeedRequest request = FeedRequest.builder()
                .latitude(lat)
                .longitude(lng)
                .sessionId(sessionId)
                .page(page)
                .size(effectiveSize)
                .seed(seed)
                .build();

        return ResponseEntity.ok(feedService.getFeed(userId, request));
    }

    @GetMapping("/comparison")
    @Operation(summary = "Get comparison view (saved vs reconsiderations)")
    public ResponseEntity<ComparisonResponse> getComparison(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng
    ) {
        String userId = userDetails != null ? ((User) userDetails).getId() : null;
        return ResponseEntity.ok(comparisonService.getComparison(userId, lat, lng));
    }

    @PostMapping("/interactions")
    @Operation(summary = "Record trainer interaction (view/save/skip)")
    public ResponseEntity<Map<String, Object>> recordInteraction(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody InteractionRequest request
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required"));
        }
        User user = (User) userDetails;
        var interaction = interactionService.recordInteraction(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", interaction.getId(),
                "message", "Interaction recorded successfully"
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get trainer by ID")
    public ResponseEntity<TrainerDTO> getTrainerById(@PathVariable String id) {
        return ResponseEntity.ok(trainerService.getTrainerById(id));
    }

    @GetMapping("/{id}/media")
    @Operation(summary = "Get trainer media (videos, images, transformations)")
    public ResponseEntity<List<TrainerMedia>> getTrainerMedia(@PathVariable String id) {
        trainerService.getTrainerById(id);
        return ResponseEntity.ok(mediaService.getTrainerMedia(id));
    }

    @PostMapping("/{id}/media/upload-url")
    @Operation(summary = "Get pre-signed URL for media upload")
    public ResponseEntity<UploadUrlResponse> getMediaUploadUrl(
            @PathVariable String id,
            @Valid @RequestBody UploadUrlRequest request
    ) {
        return ResponseEntity.ok(mediaService.generateUploadUrl(id, request));
    }

    @PostMapping("/{id}/media/confirm")
    @Operation(summary = "Confirm media upload and save metadata")
    public ResponseEntity<TrainerMedia> confirmMediaUpload(
            @PathVariable String id,
            @Valid @RequestBody ConfirmUploadRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mediaService.confirmUpload(id, request));
    }

    @DeleteMapping("/{id}/media/{mediaId}")
    @Operation(summary = "Delete trainer media")
    public ResponseEntity<Map<String, String>> deleteMedia(
            @PathVariable String id,
            @PathVariable String mediaId
    ) {
        mediaService.deleteMedia(id, mediaId);
        return ResponseEntity.ok(Map.of("message", "Media deleted successfully"));
    }

    @GetMapping("/{id}/availability")
    @Operation(summary = "Get trainer availability")
    public ResponseEntity<?> getTrainerAvailability(
            @PathVariable String id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        trainerService.getTrainerById(id);
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
        trainerService.getTrainerById(id);
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

    private User.ExperienceLevel parseExperienceLevel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return User.ExperienceLevel.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private User.TrainerGenderPreference parseTrainerGender(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return User.TrainerGenderPreference.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
