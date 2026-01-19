package org.example.trainerhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.trainerhub.model.dto.UserDTO;
import org.example.trainerhub.model.request.SavedTrainerRequest;
import org.example.trainerhub.model.request.SkippedTrainerRequest;
import org.example.trainerhub.model.request.UpdateUserRequest;
import org.example.trainerhub.model.request.UserPreferencesRequest;
import org.example.trainerhub.model.response.SavedTrainerResponse;
import org.example.trainerhub.model.response.UserPreferencesResponse;
import org.example.trainerhub.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "User profile management APIs")
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        return ResponseEntity.ok(userService.getCurrentUser(userEmail));
    }
    
    @PutMapping("/me")
    @Operation(summary = "Update user profile")
    public ResponseEntity<UserDTO> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        String userEmail = userDetails.getUsername();
        return ResponseEntity.ok(userService.updateProfile(userEmail, request));
    }
    
    @PostMapping("/me/avatar")
    @Operation(summary = "Upload profile avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file
    ) {
        String userEmail = userDetails.getUsername();
        String avatarUrl = userService.uploadAvatar(userEmail, file);
        return ResponseEntity.ok(Map.of("avatar", avatarUrl, "message", "Avatar uploaded successfully"));
    }
    
    @DeleteMapping("/me")
    @Operation(summary = "Delete user account")
    public ResponseEntity<Map<String, String>> deleteAccount(@AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        userService.deleteAccount(userEmail);
        return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
    }
    
    @GetMapping("/preferences")
    @Operation(summary = "Get user preferences")
    public ResponseEntity<UserPreferencesResponse> getPreferences(@AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        return ResponseEntity.ok(userService.getPreferences(userEmail));
    }

    @PutMapping("/preferences")
    @Operation(summary = "Update user preferences")
    public ResponseEntity<UserPreferencesResponse> updatePreferences(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserPreferencesRequest request
    ) {
        String userEmail = userDetails.getUsername();
        return ResponseEntity.ok(userService.updatePreferences(userEmail, request));
    }

    @PostMapping("/saved-trainers")
    @Operation(summary = "Save trainer")
    public ResponseEntity<SavedTrainerResponse> saveTrainer(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SavedTrainerRequest request
    ) {
        String userEmail = userDetails.getUsername();
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.saveTrainer(userEmail, request));
    }

    @GetMapping("/saved-trainers")
    @Operation(summary = "Get saved trainers")
    public ResponseEntity<Map<String, List<SavedTrainerResponse>>> getSavedTrainers(@AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        return ResponseEntity.ok(Map.of("data", userService.getSavedTrainers(userEmail)));
    }

    @DeleteMapping("/saved-trainers/{trainerId}")
    @Operation(summary = "Remove saved trainer")
    public ResponseEntity<Map<String, String>> removeSavedTrainer(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String trainerId
    ) {
        String userEmail = userDetails.getUsername();
        userService.removeSavedTrainer(userEmail, trainerId);
        return ResponseEntity.ok(Map.of("message", "Trainer removed from saved list"));
    }

    @PostMapping("/skipped-trainers")
    @Operation(summary = "Skip trainer")
    public ResponseEntity<Map<String, String>> skipTrainer(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SkippedTrainerRequest request
    ) {
        String userEmail = userDetails.getUsername();
        userService.skipTrainer(userEmail, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Trainer skipped"));
    }
}
