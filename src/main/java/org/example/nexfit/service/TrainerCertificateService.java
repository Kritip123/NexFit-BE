package org.example.nexfit.service;

import org.example.nexfit.model.request.TrainerCertificateCreateRequest;
import org.example.nexfit.model.request.TrainerCertificateUploadUrlRequest;
import org.example.nexfit.model.response.TrainerUploadUrlResponse;

public interface TrainerCertificateService {

    TrainerUploadUrlResponse generateUploadUrl(String trainerId, TrainerCertificateUploadUrlRequest request);

    String createCertificate(String trainerId, TrainerCertificateCreateRequest request);

    void deleteCertificate(String trainerId, String certificateId);
}
