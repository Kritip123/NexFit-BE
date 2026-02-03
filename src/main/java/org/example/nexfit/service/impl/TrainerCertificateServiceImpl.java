package org.example.nexfit.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nexfit.entity.Trainer;
import org.example.nexfit.entity.TrainerCertificate;
import org.example.nexfit.exception.BusinessException;
import org.example.nexfit.exception.ResourceNotFoundException;
import org.example.nexfit.model.request.TrainerCertificateCreateRequest;
import org.example.nexfit.model.request.TrainerCertificateUploadUrlRequest;
import org.example.nexfit.model.response.TrainerUploadUrlResponse;
import org.example.nexfit.repository.TrainerCertificateRepository;
import org.example.nexfit.repository.TrainerRepository;
import org.example.nexfit.service.S3Service;
import org.example.nexfit.service.TrainerCertificateService;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerCertificateServiceImpl implements TrainerCertificateService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png"
    );

    private final TrainerRepository trainerRepository;
    private final TrainerCertificateRepository certificateRepository;
    private final S3Service s3Service;

    @Override
    public TrainerUploadUrlResponse generateUploadUrl(String trainerId, TrainerCertificateUploadUrlRequest request) {
        if (!s3Service.isEnabled()) {
            throw new BusinessException("S3 uploads are not enabled. Please configure AWS S3.");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(request.getContentType())) {
            throw new BusinessException("Invalid content type. Only PDF, JPG, or PNG are allowed.");
        }

        String extension = getExtension(request.getFileName());
        String s3Key = String.format("trainers/%s/certificates/%s%s",
                trainerId,
                UUID.randomUUID(),
                extension
        );

        var upload = s3Service.generatePresignedUploadUrl(s3Key, request.getContentType());
        return TrainerUploadUrlResponse.builder()
                .uploadUrl(upload.getUploadUrl())
                .fileKey(s3Key)
                .fileUrl(s3Service.getMediaUrl(s3Key))
                .build();
    }

    @Override
    public String createCertificate(String trainerId, TrainerCertificateCreateRequest request) {
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer", trainerId));

        TrainerCertificate certificate = TrainerCertificate.builder()
                .trainerId(trainerId)
                .name(request.getName())
                .issuer(request.getIssuer())
                .issuedDate(request.getIssuedDate())
                .expiresDate(request.getExpiresDate())
                .fileUrl(request.getFileUrl())
                .fileKey(request.getFileKey())
                .build();

        certificate = certificateRepository.save(certificate);

        if (trainer.getCertifications() != null) {
            trainer.getCertifications().add(request.getName());
        }
        trainerRepository.save(trainer);

        return certificate.getId();
    }

    @Override
    public void deleteCertificate(String trainerId, String certificateId) {
        TrainerCertificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate", certificateId));

        if (!certificate.getTrainerId().equals(trainerId)) {
            throw new BusinessException("Certificate does not belong to this trainer");
        }

        if (certificate.getFileKey() != null && s3Service.isEnabled()) {
            s3Service.deleteObject(certificate.getFileKey());
        }

        certificateRepository.delete(certificate);

        trainerRepository.findById(trainerId).ifPresent(trainer -> {
            if (trainer.getCertifications() != null) {
                trainer.getCertifications().remove(certificate.getName());
                trainerRepository.save(trainer);
            }
        });
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
