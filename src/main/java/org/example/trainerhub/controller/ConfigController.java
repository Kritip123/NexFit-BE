package org.example.trainerhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
@Tag(name = "Configuration", description = "Application configuration APIs")
public class ConfigController {
    
    @Value("${app.currency}")
    private String currency;
    
    @Value("${app.support-email}")
    private String supportEmail;
    
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
}
