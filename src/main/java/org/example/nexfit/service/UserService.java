package org.example.nexfit.service;

import org.example.nexfit.model.dto.UserDTO;
import org.example.nexfit.model.request.SavedTrainerRequest;
import org.example.nexfit.model.request.SkippedTrainerRequest;
import org.example.nexfit.model.request.UpdateUserRequest;
import org.example.nexfit.model.request.UserPreferencesRequest;
import org.example.nexfit.model.response.SavedTrainerResponse;
import org.example.nexfit.model.response.UserPreferencesResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    
    UserDTO getCurrentUser(String userId);
    
    UserDTO updateProfile(String userId, UpdateUserRequest request);
    
    String uploadAvatar(String userId, MultipartFile file);
    
    void deleteAccount(String userId);

    UserPreferencesResponse getPreferences(String userId);

    UserPreferencesResponse updatePreferences(String userId, UserPreferencesRequest request);

    SavedTrainerResponse saveTrainer(String userId, SavedTrainerRequest request);

    List<SavedTrainerResponse> getSavedTrainers(String userId);

    List<SavedTrainerResponse> getSavedTrainersSorted(String userId, String sortBy, Double latitude, Double longitude);

    void removeSavedTrainer(String userId, String trainerId);

    void skipTrainer(String userId, SkippedTrainerRequest request);
}
