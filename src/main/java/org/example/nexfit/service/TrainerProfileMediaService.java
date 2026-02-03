package org.example.nexfit.service;

import org.example.nexfit.model.request.TrainerMediaCreateRequest;
import org.example.nexfit.model.request.TrainerMediaUploadUrlRequest;
import org.example.nexfit.model.response.TrainerUploadUrlResponse;

public interface TrainerProfileMediaService {

    TrainerUploadUrlResponse generateUploadUrl(String trainerId, TrainerMediaUploadUrlRequest request);

    String createMedia(String trainerId, TrainerMediaCreateRequest request);

    void deleteMedia(String trainerId, String mediaId);
}
