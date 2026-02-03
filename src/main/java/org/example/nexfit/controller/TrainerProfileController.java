package org.example.nexfit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.nexfit.entity.Trainer;
import org.example.nexfit.model.request.TrainerCertificateCreateRequest;
import org.example.nexfit.model.request.TrainerCertificateUploadUrlRequest;
import org.example.nexfit.model.request.TrainerMediaCreateRequest;
import org.example.nexfit.model.request.TrainerMediaUploadUrlRequest;
import org.example.nexfit.model.request.TrainerProfileCreateRequest;
import org.example.nexfit.model.request.TrainerProfileUpdateRequest;
import org.example.nexfit.model.response.TrainerProfileResponse;
import org.example.nexfit.model.response.TrainerUploadUrlResponse;
import org.example.nexfit.service.TrainerCertificateService;
import org.example.nexfit.service.TrainerProfileMediaService;
import org.example.nexfit.service.TrainerProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/trainer")
@RequiredArgsConstructor
@Tag(name = "Trainer Profile", description = "Trainer profile onboarding APIs")
public class TrainerProfileController {

    private final TrainerProfileService trainerProfileService;
    private final TrainerCertificateService trainerCertificateService;
    private final TrainerProfileMediaService trainerProfileMediaService;

    @GetMapping("/profile")
    @Operation(summary = "Get current trainer profile")
    public ResponseEntity<TrainerProfileResponse> getProfile(@AuthenticationPrincipal Trainer trainer) {
        return ResponseEntity.ok(trainerProfileService.getProfile(trainer.getId()));
    }

    @PostMapping("/profile")
    @Operation(summary = "Create trainer profile draft")
    public ResponseEntity<TrainerProfileResponse> createProfile(
            @AuthenticationPrincipal Trainer trainer,
            @Valid @RequestBody TrainerProfileCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                trainerProfileService.createProfile(trainer.getId(), request)
        );
    }

    @PatchMapping("/profile")
    @Operation(summary = "Update trainer profile draft")
    public ResponseEntity<TrainerProfileResponse> updateProfile(
            @AuthenticationPrincipal Trainer trainer,
            @Valid @RequestBody TrainerProfileUpdateRequest request
    ) {
        return ResponseEntity.ok(trainerProfileService.updateProfile(trainer.getId(), request));
    }

    @PostMapping("/certificates/upload-url")
    @Operation(summary = "Get signed URL for certificate upload")
    public ResponseEntity<TrainerUploadUrlResponse> getCertificateUploadUrl(
            @AuthenticationPrincipal Trainer trainer,
            @Valid @RequestBody TrainerCertificateUploadUrlRequest request
    ) {
        return ResponseEntity.ok(trainerCertificateService.generateUploadUrl(trainer.getId(), request));
    }

    @PostMapping("/certificates")
    @Operation(summary = "Register certificate metadata")
    public ResponseEntity<Map<String, String>> createCertificate(
            @AuthenticationPrincipal Trainer trainer,
            @Valid @RequestBody TrainerCertificateCreateRequest request
    ) {
        String id = trainerCertificateService.createCertificate(trainer.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    @DeleteMapping("/certificates/{id}")
    @Operation(summary = "Delete certificate")
    public ResponseEntity<Void> deleteCertificate(
            @AuthenticationPrincipal Trainer trainer,
            @PathVariable String id
    ) {
        trainerCertificateService.deleteCertificate(trainer.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/media/upload-url")
    @Operation(summary = "Get signed URL for media upload")
    public ResponseEntity<TrainerUploadUrlResponse> getMediaUploadUrl(
            @AuthenticationPrincipal Trainer trainer,
            @Valid @RequestBody TrainerMediaUploadUrlRequest request
    ) {
        return ResponseEntity.ok(trainerProfileMediaService.generateUploadUrl(trainer.getId(), request));
    }

    @PostMapping("/media")
    @Operation(summary = "Register media metadata")
    public ResponseEntity<Map<String, String>> createMedia(
            @AuthenticationPrincipal Trainer trainer,
            @Valid @RequestBody TrainerMediaCreateRequest request
    ) {
        String id = trainerProfileMediaService.createMedia(trainer.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    @DeleteMapping("/media/{id}")
    @Operation(summary = "Delete media")
    public ResponseEntity<Void> deleteMedia(
            @AuthenticationPrincipal Trainer trainer,
            @PathVariable String id
    ) {
        trainerProfileMediaService.deleteMedia(trainer.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/submit")
    @Operation(summary = "Submit trainer profile for approval")
    public ResponseEntity<Map<String, String>> submitProfile(@AuthenticationPrincipal Trainer trainer) {
        TrainerProfileResponse response = trainerProfileService.submitProfile(trainer.getId());
        return ResponseEntity.ok(Map.of("status", response.getStatus()));
    }
}
