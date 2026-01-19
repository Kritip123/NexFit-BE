package org.example.trainerhub.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.trainerhub.entity.User;
import org.example.trainerhub.exception.BusinessException;
import org.example.trainerhub.exception.ResourceNotFoundException;
import org.example.trainerhub.model.dto.UserDTO;
import org.example.trainerhub.model.request.SavedTrainerRequest;
import org.example.trainerhub.model.request.SkippedTrainerRequest;
import org.example.trainerhub.model.request.UpdateUserRequest;
import org.example.trainerhub.model.request.UserPreferencesRequest;
import org.example.trainerhub.model.response.SavedTrainerResponse;
import org.example.trainerhub.model.response.UserPreferencesResponse;
import org.example.trainerhub.repository.SavedTrainerRepository;
import org.example.trainerhub.repository.SkippedTrainerRepository;
import org.example.trainerhub.repository.TrainerRepository;
import org.example.trainerhub.repository.UserRepository;
import org.example.trainerhub.service.UserService;
import org.example.trainerhub.util.FileUploadUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
                .build();
    }

    @Override
    public UserPreferencesResponse updatePreferences(String userId, UserPreferencesRequest request) {
        User user = userRepository.findByEmail(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        
        user.setPreferredActivities(request.getSelectedActivities());
        user.setFitnessGoals(request.getFitnessGoals());
        user.setTrainerGenderPreference(request.getTrainerGenderPreference());
        user.setExperienceLevel(request.getExperienceLevel());
        
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
        var saved = savedTrainerRepository.save(org.example.trainerhub.entity.SavedTrainer.builder()
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
        User user = userRepository.findByEmail(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        
        return savedTrainerRepository.findByUserIdOrderBySavedAtDesc(user.getId()).stream()
                .map(saved -> SavedTrainerResponse.builder()
                        .trainerId(saved.getTrainerId())
                        .savedAt(saved.getSavedAt())
                        .matchPercentage(saved.getMatchPercentage())
                        .isSuperLike(saved.getIsSuperLike())
                        .build())
                .toList();
    }

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
        
        skippedTrainerRepository.save(org.example.trainerhub.entity.SkippedTrainer.builder()
                .userId(user.getId())
                .trainerId(request.getTrainerId())
                .build());
    }
}
