package org.example.nexfit.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.nexfit.model.response.UploadUrlResponse;
import org.example.nexfit.service.S3Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@Slf4j
public class S3ServiceImpl implements S3Service {

    @Value("${aws.s3.enabled:false}")
    private boolean enabled;

    @Value("${aws.region:ap-southeast-2}")
    private String region;

    @Value("${aws.access-key:}")
    private String accessKey;

    @Value("${aws.secret-key:}")
    private String secretKey;

    @Value("${aws.s3.bucket:nexfit-media-dev}")
    private String bucket;

    @Value("${aws.s3.presigned-url-expiry:900}")
    private int presignedUrlExpiry;

    @Value("${aws.cloudfront.domain:}")
    private String cloudfrontDomain;

    @Value("${aws.cloudfront.enabled:false}")
    private boolean cloudfrontEnabled;

    private S3Client s3Client;
    private S3Presigner s3Presigner;

    @PostConstruct
    public void init() {
        if (enabled && !accessKey.isEmpty() && !secretKey.isEmpty()) {
            try {
                AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
                StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

                this.s3Client = S3Client.builder()
                        .region(Region.of(region))
                        .credentialsProvider(credentialsProvider)
                        .build();

                this.s3Presigner = S3Presigner.builder()
                        .region(Region.of(region))
                        .credentialsProvider(credentialsProvider)
                        .build();

                log.info("S3 service initialized successfully with bucket: {}", bucket);
            } catch (Exception e) {
                log.error("Failed to initialize S3 service", e);
                enabled = false;
            }
        } else {
            log.info("S3 service is disabled. Using demo mode with placeholder URLs.");
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled && s3Client != null;
    }

    @Override
    public UploadUrlResponse generatePresignedUploadUrl(String key, String contentType) {
        if (!isEnabled()) {
            throw new IllegalStateException("S3 service is not enabled");
        }

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(presignedUrlExpiry))
                .putObjectRequest(builder -> builder
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .build())
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return UploadUrlResponse.builder()
                .uploadUrl(presignedRequest.url().toString())
                .s3Key(key)
                .contentType(contentType)
                .expiresAt(LocalDateTime.now().plusSeconds(presignedUrlExpiry))
                .build();
    }

    @Override
    public String getMediaUrl(String s3Key) {
        if (cloudfrontEnabled && !cloudfrontDomain.isEmpty()) {
            return String.format("https://%s/%s", cloudfrontDomain, s3Key);
        }
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, s3Key);
    }

    @Override
    public void deleteObject(String s3Key) {
        if (!isEnabled()) {
            log.warn("S3 service is not enabled, cannot delete object");
            return;
        }

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .build());
            log.info("Deleted S3 object: {}", s3Key);
        } catch (Exception e) {
            log.error("Failed to delete S3 object: {}", s3Key, e);
        }
    }

    @Override
    public boolean objectExists(String s3Key) {
        if (!isEnabled()) {
            return false;
        }

        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Failed to check if S3 object exists: {}", s3Key, e);
            return false;
        }
    }
}
