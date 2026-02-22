package org.example.nexfit.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.nexfit.entity.Trainer;
import org.example.nexfit.entity.TrainerCertificate;
import org.example.nexfit.entity.TrainerMedia;
import org.example.nexfit.entity.enums.TrainerStatus;
import org.example.nexfit.exception.BusinessException;
import org.example.nexfit.exception.ResourceNotFoundException;
import org.example.nexfit.model.request.TrainerProfileCreateRequest;
import org.example.nexfit.model.request.TrainerProfileUpdateRequest;
import org.example.nexfit.model.response.TrainerProfileResponse;
import org.example.nexfit.repository.TrainerCertificateRepository;
import org.example.nexfit.repository.TrainerMediaRepository;
import org.example.nexfit.repository.TrainerRepository;
import org.example.nexfit.service.TrainerProfileService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainerProfileServiceImpl implements TrainerProfileService {

    private static final int REQUIRED_PRICE_USD = 1;

    private final TrainerRepository trainerRepository;
    private final TrainerCertificateRepository certificateRepository;
    private final TrainerMediaRepository mediaRepository;

    @Override
    public TrainerProfileResponse getProfile(String trainerId) {
        Trainer trainer = getTrainerOrThrow(trainerId);
        return buildProfileResponse(trainer);
    }

    @Override
    public TrainerProfileResponse createProfile(String trainerId, TrainerProfileCreateRequest request) {
        Trainer trainer = getTrainerOrThrow(trainerId);
        if (Boolean.TRUE.equals(trainer.getProfileInitialized())) {
            throw new BusinessException("Trainer profile already created");
        }
        if (trainer.getStatus() != TrainerStatus.DRAFT) {
            throw new BusinessException("Trainer profile is not editable");
        }

        TrainerProfileCreateRequest.Profile profile = request.getProfile();
        applyBaseProfile(trainer, profile.getFullName(), profile.getEmail(), profile.getPhone(), profile.getCity());
        trainer.setPricingMonthlySubscriptionUsd(BigDecimal.valueOf(REQUIRED_PRICE_USD));
        trainer.setProfileInitialized(true);

        trainer = trainerRepository.save(trainer);
        return buildProfileResponse(trainer);
    }

    @Override
    public TrainerProfileResponse updateProfile(String trainerId, TrainerProfileUpdateRequest request) {
        Trainer trainer = getTrainerOrThrow(trainerId);

        TrainerProfileUpdateRequest.Profile profile = request.getProfile();
        if (profile != null) {
            if (profile.getProfileImage() != null) {
                trainer.setProfileImage(profile.getProfileImage());
            }
            if (profile.getCoverImage() != null) {
                trainer.setCoverImage(profile.getCoverImage());
            }
            if (profile.getFullName() != null) {
                trainer.setName(profile.getFullName());
            }
            if (profile.getEmail() != null && !profile.getEmail().equalsIgnoreCase(trainer.getEmail())) {
                if (trainerRepository.existsByEmail(profile.getEmail())) {
                    throw new BusinessException("Email already registered");
                }
                trainer.setEmail(profile.getEmail());
            }
            if (profile.getPhone() != null) {
                trainer.setPhone(profile.getPhone());
            }
            if (profile.getCity() != null) {
                trainer.setCity(profile.getCity());
            }
            if (profile.getHeadline() != null) {
                trainer.setHeadline(profile.getHeadline());
            }
            if (profile.getBio() != null) {
                trainer.setBio(profile.getBio());
            }
            if (profile.getSpecializations() != null) {
                trainer.setSpecializations(new HashSet<>(profile.getSpecializations()));
            }
            if (profile.getYearsActive() != null) {
                trainer.setYearsActive(profile.getYearsActive());
                trainer.setExperience(profile.getYearsActive());
            }
            if (profile.getLanguages() != null) {
                trainer.setLanguages(new HashSet<>(profile.getLanguages()));
            }
            if (profile.getPricing() != null && profile.getPricing().getMonthlySubscriptionUSD() != null) {
                if (!Objects.equals(profile.getPricing().getMonthlySubscriptionUSD(), REQUIRED_PRICE_USD)) {
                    throw new BusinessException("Monthly subscription must be $1");
                }
                trainer.setPricingMonthlySubscriptionUsd(BigDecimal.valueOf(REQUIRED_PRICE_USD));
            }
        }

        trainer = trainerRepository.save(trainer);
        return buildProfileResponse(trainer);
    }

    @Override
    public TrainerProfileResponse submitProfile(String trainerId) {
        Trainer trainer = getTrainerOrThrow(trainerId);
        if (trainer.getStatus() != TrainerStatus.DRAFT) {
            throw new BusinessException("Trainer profile cannot be submitted");
        }

        validateSubmission(trainer);

        trainer.setStatus(TrainerStatus.SUBMITTED);
        trainer.setSubmittedAt(LocalDateTime.now());
        trainer = trainerRepository.save(trainer);

        return buildProfileResponse(trainer);
    }

    private Trainer getTrainerOrThrow(String trainerId) {
        return trainerRepository.findById(trainerId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer", trainerId));
    }

    private void applyBaseProfile(Trainer trainer, String fullName, String email, String phone, String city) {
        trainer.setName(fullName);
        if (!email.equalsIgnoreCase(trainer.getEmail()) && trainerRepository.existsByEmail(email)) {
            throw new BusinessException("Email already registered");
        }
        trainer.setEmail(email);
        trainer.setPhone(phone);
        trainer.setCity(city);
    }

    private void validateSubmission(Trainer trainer) {
        if (isBlank(trainer.getName()) ||
                isBlank(trainer.getEmail()) ||
                isBlank(trainer.getPhone()) ||
                isBlank(trainer.getCity()) ||
                isBlank(trainer.getHeadline()) ||
                isBlank(trainer.getBio()) ||
                trainer.getSpecializations() == null || trainer.getSpecializations().isEmpty() ||
                trainer.getLanguages() == null || trainer.getLanguages().isEmpty() ||
                trainer.getYearsActive() == null) {
            throw new BusinessException("Please complete all required profile fields before submitting");
        }
        if (trainer.getPricingMonthlySubscriptionUsd() == null ||
                trainer.getPricingMonthlySubscriptionUsd().intValue() != REQUIRED_PRICE_USD) {
            throw new BusinessException("Monthly subscription must be $1");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private TrainerProfileResponse buildProfileResponse(Trainer trainer) {
        List<TrainerCertificate> certificates = certificateRepository.findByTrainerIdOrderByIssuedDateDesc(trainer.getId());
        List<TrainerMedia> media = mediaRepository.findByTrainerIdOrderByDisplayOrderAsc(trainer.getId());

        TrainerStatus status = trainer.getStatus() != null ? trainer.getStatus() : TrainerStatus.DRAFT;

        return TrainerProfileResponse.builder()
                .id(trainer.getId())
                .userId(trainer.getId())
                .status(status.name().toLowerCase())
                .profile(TrainerProfileResponse.Profile.builder()
                        .fullName(trainer.getName())
                        .email(trainer.getEmail())
                        .phone(trainer.getPhone())
                        .city(trainer.getCity())
                        .headline(trainer.getHeadline())
                        .bio(trainer.getBio())
                        .profileImage(trainer.getProfileImage())
                        .coverImage(trainer.getCoverImage())
                        .specializations(trainer.getSpecializations() != null
                                ? trainer.getSpecializations().stream().sorted().collect(Collectors.toList())
                                : List.of())
                        .yearsActive(trainer.getYearsActive())
                        .languages(trainer.getLanguages() != null
                                ? trainer.getLanguages().stream().sorted().collect(Collectors.toList())
                                : List.of())
                        .pricing(TrainerProfileResponse.Pricing.builder()
                                .monthlySubscriptionUSD(REQUIRED_PRICE_USD)
                                .build())
                        .build())
                .certificates(certificates.stream()
                        .map(cert -> TrainerProfileResponse.CertificateInfo.builder()
                                .id(cert.getId())
                                .name(cert.getName())
                                .issuer(cert.getIssuer())
                                .issuedDate(cert.getIssuedDate())
                                .expiresDate(cert.getExpiresDate())
                                .fileUrl(cert.getFileUrl())
                                .build())
                        .toList())
                .media(media.stream()
                        .map(m -> TrainerProfileResponse.MediaInfo.builder()
                                .id(m.getId())
                                .type(m.getType() != null ? m.getType().name().toLowerCase() : null)
                                .url(m.getMediaUrl())
                                .thumbnailUrl(m.getThumbnailUrl())
                                .durationSec(m.getDurationSeconds())
                                .order(m.getDisplayOrder())
                                .build())
                        .toList())
                .hasDiscoverVideo(Boolean.TRUE.equals(trainer.getHasDiscoverVideo()))
                .createdAt(trainer.getCreatedAt())
                .updatedAt(trainer.getUpdatedAt())
                .build();
    }
}
