package org.example.nexfit.service;

import org.example.nexfit.model.request.TrainerProfileCreateRequest;
import org.example.nexfit.model.request.TrainerProfileUpdateRequest;
import org.example.nexfit.model.response.TrainerProfileResponse;

public interface TrainerProfileService {

    TrainerProfileResponse getProfile(String trainerId);

    TrainerProfileResponse createProfile(String trainerId, TrainerProfileCreateRequest request);

    TrainerProfileResponse updateProfile(String trainerId, TrainerProfileUpdateRequest request);

    TrainerProfileResponse submitProfile(String trainerId);
}
