package org.example.trainerhub.service;

import org.example.trainerhub.model.dto.UserDTO;
import org.example.trainerhub.model.request.SavedTrainerRequest;
import org.example.trainerhub.model.request.SkippedTrainerRequest;
import org.example.trainerhub.model.request.UpdateUserRequest;
import org.example.trainerhub.model.request.UserPreferencesRequest;
import org.example.trainerhub.model.response.SavedTrainerResponse;
import org.example.trainerhub.model.response.UserPreferencesResponse;
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

    void removeSavedTrainer(String userId, String trainerId);

    void skipTrainer(String userId, SkippedTrainerRequest request);
}
