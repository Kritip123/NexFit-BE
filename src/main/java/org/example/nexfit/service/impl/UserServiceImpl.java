package org.example.nexfit.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nexfit.entity.User;
import org.example.nexfit.exception.BusinessException;
import org.example.nexfit.exception.ResourceNotFoundException;
import org.example.nexfit.model.dto.UserDTO;
import org.example.nexfit.model.request.SavedTrainerRequest;
import org.example.nexfit.model.request.SkippedTrainerRequest;
import org.example.nexfit.model.request.UpdateUserRequest;
import org.example.nexfit.model.request.UserPreferencesRequest;
import org.example.nexfit.model.response.SavedTrainerResponse;
import org.example.nexfit.model.response.UserPreferencesResponse;
import org.example.nexfit.entity.SavedTrainer;
import org.example.nexfit.entity.Trainer;
import org.example.nexfit.repository.SavedTrainerRepository;
import org.example.nexfit.repository.SkippedTrainerRepository;
import org.example.nexfit.repository.TrainerRepository;
import org.example.nexfit.repository.UserRepository;
import org.example.nexfit.service.UserService;
import org.example.nexfit.util.DistanceCalculator;
import org.example.nexfit.util.FileUploadUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final SavedTrainerRepository savedTrainerRepository;
    private final SkippedTrainerRepository skippedTrainerRepository;
    private final TrainerRepository trainerRepository;
    private final FileUploadUtil fileUploadUtil;
    
    @Override
    public UserDTO getCurrentUser(String userId) {
        // userId is actually the email from JWT token
        User user = userRepository.findByEmail(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .createdAt(user.getCreatedAt())
                .emailVerified(user.getEmailVerified())
                .build();
    }
    
    @Override
    public UserDTO updateProfile(String userId, UpdateUserRequest request) {
        User user = userRepository.findByEmail(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        
        user = userRepository.save(user);
        
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .createdAt(user.getCreatedAt())
                .emailVerified(user.getEmailVerified())
                .build();
    }
    
    @Override
    public String uploadAvatar(String userId, MultipartFile file) {
        User user = userRepository.findByEmail(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        
        try {
            String fileName = fileUploadUtil.saveFile("avatars", file);
            user.setAvatar(fileName);
            userRepository.save(user);
            return fileName;
        } catch (Exception e) {
            log.error("Failed to upload avatar for user {}", userId, e);
            throw new BusinessException("Failed to upload avatar");
        }
    }
    
    @Override
    public void deleteAccount(String userId) {
        User user = userRepository.findByEmail(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        
        // Soft delete - just deactivate the account
        user.setIsActive(false);
        userRepository.save(user);
        
        log.info("User account deactivated: {}", userId);
    }

    @Override
    public UserPreferencesResponse getPreferences(String userId) {
        User user = userRepository.findByEmail(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        return UserPreferencesResponse.builder()
                .selectedActivities(user.getPreferredActivities() != null ? user.getPreferredActivities() : List.of())
                .fitnessGoals(user.getFitnessGoals() != null ? user.getFitnessGoals() : List.of())
                .trainerGenderPreference(user.getTrainerGenderPreference())
                .experienceLevel(user.getExperienceLevel())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .selectedCategories(user.getSelectedCategories() != null ? user.getSelectedCategories() : List.of())
                .selectedSubcategories(user.getSelectedSubcategories() != null ? user.getSelectedSubcategories() : List.of())
                .latitude(user.getLatitude())
                .longitude(user.getLongitude())
                .build();
    }

    @Override
    public UserPreferencesResponse updatePreferences(String userId, UserPreferencesRequest request) {
        User user = userRepository.findByEmail(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (request.getSelectedActivities() != null) {
            user.setPreferredActivities(request.getSelectedActivities());
        }
        if (request.getFitnessGoals() != null) {
            user.setFitnessGoals(request.getFitnessGoals());
        }
        if (request.getTrainerGenderPreference() != null) {
            user.setTrainerGenderPreference(request.getTrainerGenderPreference());
        }
        if (request.getExperienceLevel() != null) {
            user.setExperienceLevel(request.getExperienceLevel());
        }
        if (request.getSelectedCategories() != null) {
            user.setSelectedCategories(request.getSelectedCategories());
        }
        if (request.getSelectedSubcategories() != null) {
            user.setSelectedSubcategories(request.getSelectedSubcategories());
        }
        if (request.getLatitude() != null) {
            user.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            user.setLongitude(request.getLongitude());
        }

        userRepository.save(user);

        return getPreferences(userId);
    }

    @Override
    public SavedTrainerResponse saveTrainer(String userId, SavedTrainerRequest request) {
        User user = userRepository.findByEmail(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        var trainer = trainerRepository.findById(request.getTrainerId())
                .orElseThrow(() -> new ResourceNotFoundException("Trainer", request.getTrainerId()));
        if (!trainer.getIsActive()) {
            throw new BusinessException("Trainer is not active");
        }
        
        if (skippedTrainerRepository.findByUserIdAndTrainerId(user.getId(), request.getTrainerId()).isPresent()) {
            throw new BusinessException("You cannot save a trainer you have skipped");
        }
        
        if (savedTrainerRepository.findByUserIdAndTrainerId(user.getId(), request.getTrainerId()).isPresent()) {
            throw new BusinessException("Trainer already saved");
        }
        
        int matchPercentage = request.getMatchPercentage() != null ? request.getMatchPercentage() : 0;
        var saved = savedTrainerRepository.save(org.example.nexfit.entity.SavedTrainer.builder()
                .userId(user.getId())
                .trainerId(request.getTrainerId())
                .matchPercentage(matchPercentage)
                .isSuperLike(request.getIsSuperLike() != null && request.getIsSuperLike())
                .build());
        
        return SavedTrainerResponse.builder()
                .trainerId(saved.getTrainerId())
                .savedAt(saved.getSavedAt())
                .matchPercentage(saved.getMatchPercentage())
                .isSuperLike(saved.getIsSuperLike())
                .build();
    }

    @Override
    public List<SavedTrainerResponse> getSavedTrainers(String userId) {
        return getSavedTrainersSorted(userId, null, null, null);
    }

    @Override
    public List<SavedTrainerResponse> getSavedTrainersSorted(String userId, String sortBy, Double latitude, Double longitude) {
        User user = userRepository.findByEmail(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        List<SavedTrainer> savedTrainers = savedTrainerRepository.findByUserIdOrderBySavedAtDesc(user.getId());

        if (savedTrainers.isEmpty()) {
            return List.of();
        }

        // Fetch trainer details for sorting
        Set<String> trainerIds = savedTrainers.stream()
                .map(SavedTrainer::getTrainerId)
                .collect(Collectors.toSet());
        Map<String, Trainer> trainerMap = trainerRepository.findAllById(trainerIds).stream()
                .collect(Collectors.toMap(Trainer::getId, t -> t));

        // Build response with trainer details
        List<SavedTrainerWithDetails> detailedList = savedTrainers.stream()
                .filter(st -> trainerMap.containsKey(st.getTrainerId()))
                .map(st -> new SavedTrainerWithDetails(st, trainerMap.get(st.getTrainerId())))
                .toList();

        // Apply sorting
        List<SavedTrainerWithDetails> sorted = sortSavedTrainers(detailedList, sortBy, latitude, longitude);

        return sorted.stream()
                .map(this::buildSavedTrainerResponse)
                .toList();
    }

    private List<SavedTrainerWithDetails> sortSavedTrainers(List<SavedTrainerWithDetails> list, String sortBy,
                                                            Double lat, Double lng) {
        if (sortBy == null || sortBy.isBlank()) {
            return list; // Default: already sorted by savedAt desc
        }

        Comparator<SavedTrainerWithDetails> comparator = switch (sortBy.toLowerCase()) {
            case "price" -> Comparator.comparing(
                    d -> d.trainer.getHourlyRate() != null ? d.trainer.getHourlyRate().doubleValue() : Double.MAX_VALUE
            );
            case "rating" -> Comparator.comparing(
                    (SavedTrainerWithDetails d) -> d.trainer.getRating() != null ? d.trainer.getRating().doubleValue() : 0.0
            ).reversed();
            case "distance" -> {
                if (lat == null || lng == null) {
                    yield Comparator.comparing(d -> 0); // No sorting if no location
                }
                yield Comparator.comparing(d -> {
                    if (d.trainer.getLatitude() != null && d.trainer.getLongitude() != null) {
                        return DistanceCalculator.calculateDistance(lat, lng, d.trainer.getLatitude(), d.trainer.getLongitude());
                    }
                    return Double.MAX_VALUE;
                });
            }
            case "match" -> Comparator.comparing(
                    (SavedTrainerWithDetails d) -> d.saved.getMatchPercentage() != null ? d.saved.getMatchPercentage() : 0
            ).reversed();
            case "specialty" -> Comparator.comparing(
                    (SavedTrainerWithDetails d) -> d.trainer.getSpecializations() != null && !d.trainer.getSpecializations().isEmpty()
                            ? d.trainer.getSpecializations().iterator().next()
                            : "zzz"
            );
            default -> Comparator.comparing((SavedTrainerWithDetails d) -> d.saved.getSavedAt()).reversed();
        };

        return list.stream().sorted(comparator).toList();
    }

    private SavedTrainerResponse buildSavedTrainerResponse(SavedTrainerWithDetails details) {
        Trainer t = details.trainer;
        return SavedTrainerResponse.builder()
                .trainerId(details.saved.getTrainerId())
                .savedAt(details.saved.getSavedAt())
                .matchPercentage(details.saved.getMatchPercentage())
                .isSuperLike(details.saved.getIsSuperLike())
                .trainerName(t.getName())
                .trainerProfileImage(t.getProfileImage())
                .trainerRating(t.getRating())
                .trainerHourlyRate(t.getHourlyRate())
                .trainerSpecializations(t.getSpecializations())
                .trainerCity(t.getCity())
                .trainerExperience(t.getExperience())
                .build();
    }

    private record SavedTrainerWithDetails(SavedTrainer saved, Trainer trainer) {}

    @Override
    public void removeSavedTrainer(String userId, String trainerId) {
        User user = userRepository.findByEmail(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        
        savedTrainerRepository.deleteByUserIdAndTrainerId(user.getId(), trainerId);
    }

    @Override
    public void skipTrainer(String userId, SkippedTrainerRequest request) {
        User user = userRepository.findByEmail(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        var trainer = trainerRepository.findById(request.getTrainerId())
                .orElseThrow(() -> new ResourceNotFoundException("Trainer", request.getTrainerId()));
        if (!trainer.getIsActive()) {
            throw new BusinessException("Trainer is not active");
        }
        
        if (savedTrainerRepository.findByUserIdAndTrainerId(user.getId(), request.getTrainerId()).isPresent()) {
            throw new BusinessException("You cannot skip a trainer you have saved");
        }
        
        if (skippedTrainerRepository.findByUserIdAndTrainerId(user.getId(), request.getTrainerId()).isPresent()) {
            return;
        }
        
        skippedTrainerRepository.save(org.example.nexfit.entity.SkippedTrainer.builder()
                .userId(user.getId())
                .trainerId(request.getTrainerId())
                .build());
    }
}
