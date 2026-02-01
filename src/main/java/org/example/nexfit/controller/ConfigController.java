package org.example.nexfit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nexfit.entity.Trainer;
import org.example.nexfit.entity.TrainerMedia;
import org.example.nexfit.entity.TrainingCategory;
import org.example.nexfit.entity.TrainingSubcategory;
import org.example.nexfit.entity.enums.MediaType;
import org.example.nexfit.model.response.TrainingCategoryResponse;
import org.example.nexfit.repository.TrainerMediaRepository;
import org.example.nexfit.repository.TrainerRepository;
import org.example.nexfit.repository.TrainingCategoryRepository;
import org.example.nexfit.repository.TrainingSubcategoryRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Configuration", description = "Application configuration APIs")
public class ConfigController {

    private final TrainingCategoryRepository categoryRepository;
    private final TrainingSubcategoryRepository subcategoryRepository;
    private final TrainerRepository trainerRepository;
    private final TrainerMediaRepository mediaRepository;

    @Value("${app.currency}")
    private String currency;

    @Value("${app.support-email}")
    private String supportEmail;

    @GetMapping("/training-categories")
    @Operation(summary = "Get all training categories")
    public ResponseEntity<List<TrainingCategoryResponse>> getTrainingCategories() {
        List<TrainingCategory> categories = categoryRepository.findByActiveTrueOrderByDisplayOrderAsc();

        List<TrainingCategoryResponse> response = categories.stream()
                .map(cat -> {
                    List<TrainingSubcategory> subcats = subcategoryRepository
                            .findByCategoryIdAndActiveTrueOrderByDisplayOrderAsc(cat.getId());

                    return TrainingCategoryResponse.builder()
                            .id(cat.getId())
                            .name(cat.getName())
                            .icon(cat.getIcon())
                            .description(cat.getDescription())
                            .imageUrl(cat.getImageUrl())
                            .displayOrder(cat.getDisplayOrder())
                            .subcategories(subcats.stream()
                                    .map(sub -> TrainingCategoryResponse.SubcategoryInfo.builder()
                                            .id(sub.getId())
                                            .name(sub.getName())
                                            .description(sub.getDescription())
                                            .icon(sub.getIcon())
                                            .build())
                                    .toList())
                            .build();
                })
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/training-subcategories")
    @Operation(summary = "Get training subcategories by category")
    public ResponseEntity<List<TrainingCategoryResponse.SubcategoryInfo>> getTrainingSubcategories(
            @RequestParam(required = false) String categoryId
    ) {
        List<TrainingSubcategory> subcategories;

        if (categoryId != null && !categoryId.isBlank()) {
            subcategories = subcategoryRepository.findByCategoryIdAndActiveTrueOrderByDisplayOrderAsc(categoryId);
        } else {
            subcategories = subcategoryRepository.findByActiveTrueOrderByDisplayOrderAsc();
        }

        List<TrainingCategoryResponse.SubcategoryInfo> response = subcategories.stream()
                .map(sub -> TrainingCategoryResponse.SubcategoryInfo.builder()
                        .id(sub.getId())
                        .name(sub.getName())
                        .description(sub.getDescription())
                        .icon(sub.getIcon())
                        .build())
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/specializations")
    @Operation(summary = "Get available specializations")
    public ResponseEntity<List<String>> getSpecializations() {
        List<String> specializations = List.of(
                "Strength Training",
                "HIIT",
                "Yoga",
                "Pilates",
                "Boxing",
                "CrossFit",
                "Personal Training",
                "Weight Loss",
                "Muscle Building",
                "Cardio Training",
                "Functional Training",
                "Sports Performance",
                "Rehabilitation",
                "Nutrition Coaching",
                "Online Coaching",
                "Group Fitness",
                "MMA",
                "Kickboxing",
                "Swimming",
                "Running Coach",
                "Cycling",
                "Dance Fitness",
                "Flexibility Training",
                "Powerlifting",
                "Olympic Weightlifting",
                "Bodybuilding",
                "Calisthenics",
                "Mobility Training",
                "Pre/Post Natal",
                "Senior Fitness"
        );

        return ResponseEntity.ok(specializations);
    }

    @GetMapping("/app-settings")
    @Operation(summary = "Get app configuration")
    public ResponseEntity<Map<String, Object>> getAppSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("currency", currency);
        settings.put("supportEmail", supportEmail);

        // Add time zone information for Australia
        settings.put("timezone", "Australia/Sydney");
        settings.put("country", "Australia");

        // Add supported languages
        settings.put("languages", List.of("English", "Mandarin", "Spanish", "Arabic", "Vietnamese", "Italian", "Greek"));

        // Add feature flags
        settings.put("features", Map.of(
                "s3Enabled", false, // Will be dynamically set based on config
                "analyticsEnabled", true,
                "feedEnabled", true,
                "comparisonEnabled", true
        ));

        return ResponseEntity.ok(settings);
    }

    @GetMapping("/cities")
    @Operation(summary = "Get supported Australian cities")
    public ResponseEntity<List<Map<String, Object>>> getSupportedCities() {
        List<Map<String, Object>> cities = List.of(
                Map.of("name", "Sydney", "state", "NSW", "latitude", -33.8688, "longitude", 151.2093),
                Map.of("name", "Melbourne", "state", "VIC", "latitude", -37.8136, "longitude", 144.9631),
                Map.of("name", "Brisbane", "state", "QLD", "latitude", -27.4698, "longitude", 153.0251),
                Map.of("name", "Perth", "state", "WA", "latitude", -31.9505, "longitude", 115.8605),
                Map.of("name", "Adelaide", "state", "SA", "latitude", -34.9285, "longitude", 138.6007),
                Map.of("name", "Gold Coast", "state", "QLD", "latitude", -28.0167, "longitude", 153.4000),
                Map.of("name", "Newcastle", "state", "NSW", "latitude", -32.9283, "longitude", 151.7817),
                Map.of("name", "Canberra", "state", "ACT", "latitude", -35.2809, "longitude", 149.1300),
                Map.of("name", "Central Coast", "state", "NSW", "latitude", -33.4256, "longitude", 151.3990),
                Map.of("name", "Wollongong", "state", "NSW", "latitude", -34.4248, "longitude", 150.8931),
                Map.of("name", "Hobart", "state", "TAS", "latitude", -42.8821, "longitude", 147.3272),
                Map.of("name", "Darwin", "state", "NT", "latitude", -12.4634, "longitude", 130.8456)
        );

        return ResponseEntity.ok(cities);
    }

    @PostMapping("/seed-media")
    @Operation(summary = "Seed demo media for all trainers (dev only)")
    public ResponseEntity<Map<String, Object>> seedMedia() {
        // Demo video URLs (Google sample videos - publicly accessible MP4s)
        List<String> videoUrls = List.of(
                "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4",
                "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4",
                "https://storage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4",
                "https://storage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                "https://storage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                "https://storage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
        );

        List<String> thumbnailUrls = List.of(
                "https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=800",
                "https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?w=800",
                "https://images.unsplash.com/photo-1549576490-b0b4831ef60a?w=800",
                "https://images.unsplash.com/photo-1518611012118-696072aa579a?w=800",
                "https://images.unsplash.com/photo-1574680096145-d05b474e2155?w=800",
                "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=800",
                "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?w=800",
                "https://images.unsplash.com/photo-1526506118085-60ce8714f8c5?w=800",
                "https://images.unsplash.com/photo-1581009146145-b5ef050c149a?w=800",
                "https://images.unsplash.com/photo-1594737625785-a6cbdabd333c?w=800"
        );

        List<String> videoTitles = List.of(
                "Morning HIIT Workout", "Strength Training Basics", "Full Body Burn",
                "Core Crusher", "Leg Day Special", "Upper Body Power",
                "Cardio Blast", "Flexibility Flow", "Power Circuit", "Recovery Session"
        );

        // Clear existing media
        mediaRepository.deleteAll();
        log.info("Cleared existing trainer media");

        List<Trainer> trainers = trainerRepository.findAll();
        Random random = new Random();
        int totalMedia = 0;
        int trainerIndex = 0;

        for (Trainer trainer : trainers) {
            List<TrainerMedia> mediaList = new ArrayList<>();

            // Each trainer gets a different starting offset for variety
            int startOffset = trainerIndex * 3;

            // Add 5-6 demo videos per trainer (first one marked as featured)
            int videoCount = 5 + random.nextInt(2);
            for (int v = 0; v < videoCount; v++) {
                int videoIdx = (startOffset + v) % videoUrls.size();
                int thumbIdx = (startOffset + v) % thumbnailUrls.size();
                int titleIdx = (startOffset + v) % videoTitles.size();

                mediaList.add(TrainerMedia.builder()
                        .trainerId(trainer.getId())
                        .type(MediaType.VIDEO)
                        .mediaUrl(videoUrls.get(videoIdx))
                        .thumbnailUrl(thumbnailUrls.get(thumbIdx))
                        .title(videoTitles.get(titleIdx))
                        .description("Professional training session demonstrating proper form and technique")
                        .durationSeconds(15 + random.nextInt(45))
                        .displayOrder(v)
                        .likes(10 + random.nextInt(200))
                        .isDemo(true)
                        .isFeatured(v == 0)
                        .createdAt(LocalDateTime.now().minusDays(random.nextInt(30)))
                        .build());
            }

            mediaRepository.saveAll(mediaList);
            totalMedia += mediaList.size();
            trainerIndex++;
        }

        log.info("Seeded {} media items for {} trainers", totalMedia, trainers.size());

        return ResponseEntity.ok(Map.of(
                "message", "Media seeded successfully",
                "trainersCount", trainers.size(),
                "mediaCount", totalMedia
        ));
    }
}
