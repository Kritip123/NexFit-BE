package org.example.nexfit.service;

import org.example.nexfit.model.response.UploadUrlResponse;

public interface S3Service {

    boolean isEnabled();

    UploadUrlResponse generatePresignedUploadUrl(String key, String contentType);

    String getMediaUrl(String s3Key);

    void deleteObject(String s3Key);

    boolean objectExists(String s3Key);
}
