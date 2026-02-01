package org.example.nexfit.service;

import org.example.nexfit.entity.TrainerMedia;
import org.example.nexfit.model.request.ConfirmUploadRequest;
import org.example.nexfit.model.request.UploadUrlRequest;
import org.example.nexfit.model.response.UploadUrlResponse;

import java.util.List;

public interface TrainerMediaService {

    List<TrainerMedia> getTrainerMedia(String trainerId);

    UploadUrlResponse generateUploadUrl(String trainerId, UploadUrlRequest request);

    TrainerMedia confirmUpload(String trainerId, ConfirmUploadRequest request);

    void deleteMedia(String trainerId, String mediaId);

    List<TrainerMedia> getMediaForTrainers(List<String> trainerIds);
}
