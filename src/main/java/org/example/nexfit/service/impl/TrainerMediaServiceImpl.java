package org.example.nexfit.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nexfit.entity.TrainerMedia;
import org.example.nexfit.exception.BusinessException;
import org.example.nexfit.exception.ResourceNotFoundException;
import org.example.nexfit.repository.TrainerRepository;
import org.example.nexfit.model.request.ConfirmUploadRequest;
import org.example.nexfit.model.request.UploadUrlRequest;
import org.example.nexfit.model.response.UploadUrlResponse;
import org.example.nexfit.repository.TrainerMediaRepository;
import org.example.nexfit.service.S3Service;
import org.example.nexfit.service.TrainerMediaService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerMediaServiceImpl implements TrainerMediaService {

    private final TrainerMediaRepository mediaRepository;
    private final S3Service s3Service;
    private final TrainerRepository trainerRepository;

    @Override
    public List<TrainerMedia> getTrainerMedia(String trainerId) {
        return mediaRepository.findByTrainerIdOrderByDisplayOrderAsc(trainerId);
    }

    @Override
    public UploadUrlResponse generateUploadUrl(String trainerId, UploadUrlRequest request) {
        if (!s3Service.isEnabled()) {
            throw new BusinessException("S3 uploads are not enabled. Please use demo mode or configure AWS S3.");
        }

        // Generate S3 key
        String extension = getExtension(request.getFilename());
        String folder = request.getMediaType().name().toLowerCase();
        String s3Key = String.format("trainers/%s/media/%s/%s%s",
                trainerId,
                folder,
                UUID.randomUUID(),
                extension);

        return s3Service.generatePresignedUploadUrl(s3Key, request.getContentType());
    }

    @Override
    public TrainerMedia confirmUpload(String trainerId, ConfirmUploadRequest request) {
        // Verify the upload exists in S3 (if S3 is enabled)
        if (s3Service.isEnabled() && !s3Service.objectExists(request.getS3Key())) {
            throw new BusinessException("Upload not found. Please upload the file first.");
        }

        if (request.getMediaType() == org.example.nexfit.entity.enums.MediaType.VIDEO) {
            if (request.getDurationSeconds() == null) {
                throw new BusinessException("Video duration is required");
            }
            if (request.getDurationSeconds() < 10 || request.getDurationSeconds() > 15) {
                throw new BusinessException("Video duration must be between 10 and 15 seconds");
            }
        }

        // Get the media URL
        String mediaUrl = s3Service.isEnabled()
                ? s3Service.getMediaUrl(request.getS3Key())
                : request.getS3Key(); // For demo mode, s3Key might be a direct URL

        // Get current max display order
        long count = mediaRepository.countByTrainerId(trainerId);
        int displayOrder = request.getDisplayOrder() != null ? request.getDisplayOrder() : (int) count;

        TrainerMedia media = TrainerMedia.builder()
                .trainerId(trainerId)
                .type(request.getMediaType())
                .s3Key(request.getS3Key())
                .mediaUrl(mediaUrl)
                .thumbnailUrl(request.getThumbnailUrl() != null
                        ? request.getThumbnailUrl()
                        : (request.getMediaType() == org.example.nexfit.entity.enums.MediaType.VIDEO
                            ? generateThumbnailUrl(mediaUrl) : mediaUrl))
                .title(request.getTitle())
                .description(request.getDescription())
                .beforeImageUrl(request.getBeforeImageUrl())
                .afterImageUrl(request.getAfterImageUrl())
                .fileSizeBytes(request.getFileSizeBytes())
                .mimeType(request.getMimeType())
                .durationSeconds(request.getDurationSeconds())
                .width(request.getWidth())
                .height(request.getHeight())
                .displayOrder(displayOrder)
                .isDemo(false)
                .createdAt(LocalDateTime.now())
                .build();

        TrainerMedia saved = mediaRepository.save(media);
        updateDiscoverVideoFlag(trainerId);
        return saved;
    }

    @Override
    public void deleteMedia(String trainerId, String mediaId) {
        TrainerMedia media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media", mediaId));

        if (!media.getTrainerId().equals(trainerId)) {
            throw new BusinessException("Media does not belong to this trainer");
        }

        // Delete from S3 if it's not demo content
        if (!Boolean.TRUE.equals(media.getIsDemo()) && media.getS3Key() != null && s3Service.isEnabled()) {
            s3Service.deleteObject(media.getS3Key());
        }

        mediaRepository.delete(media);
        updateDiscoverVideoFlag(trainerId);
        log.info("Deleted media: {} for trainer: {}", mediaId, trainerId);
    }

    @Override
    public List<TrainerMedia> getMediaForTrainers(List<String> trainerIds) {
        return mediaRepository.findByTrainerIdInOrderByDisplayOrderAsc(trainerIds);
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private String generateThumbnailUrl(String videoUrl) {
        // For demo purposes, we'll just use the video URL as thumbnail
        // In production, you'd generate an actual thumbnail
        return videoUrl;
    }

    private void updateDiscoverVideoFlag(String trainerId) {
        trainerRepository.findById(trainerId).ifPresent(trainer -> {
            boolean hasDiscoverVideo = mediaRepository.findByTrainerIdAndTypeOrderByDisplayOrderAsc(
                            trainerId,
                            org.example.nexfit.entity.enums.MediaType.VIDEO
                    ).stream()
                    .anyMatch(media -> media.getDurationSeconds() != null
                            && media.getDurationSeconds() >= 10
                            && media.getDurationSeconds() <= 15);

            trainer.setHasDiscoverVideo(hasDiscoverVideo);
            trainerRepository.save(trainer);
        });
    }
}
