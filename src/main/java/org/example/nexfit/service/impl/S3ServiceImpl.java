package org.example.nexfit.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.nexfit.exception.BusinessException;
import org.example.nexfit.model.response.UploadUrlResponse;
import org.example.nexfit.service.S3Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
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

    @Value("${aws.s3.required:true}")
    private boolean required;

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
        if (required && !enabled) {
            throw new IllegalStateException("S3 is required but aws.s3.enabled is false");
        }
        if (!enabled) {
            log.info("S3 service is disabled.");
            return;
        }
        try {
            AwsCredentialsProvider credentialsProvider;
            if (!accessKey.isEmpty() && !secretKey.isEmpty()) {
                log.info("Using static AWS credentials for S3");
                credentialsProvider = StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey));
            } else {
                log.info("Using default AWS credential chain (IAM role) for S3");
                credentialsProvider = DefaultCredentialsProvider.create();
            }

            this.s3Client = S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(credentialsProvider)
                    .build();

            this.s3Presigner = S3Presigner.builder()
                    .region(Region.of(region))
                    .credentialsProvider(credentialsProvider)
                    .build();

            this.s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            log.info("S3 service initialized successfully with bucket: {}", bucket);
        } catch (Exception e) {
            log.error("Failed to initialize S3 service", e);
            enabled = false;
            if (required) {
                throw new IllegalStateException("S3 initialization failed. Check credentials and bucket access.", e);
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled && s3Client != null;
    }

    @Override
    public UploadUrlResponse generatePresignedUploadUrl(String key, String contentType) {
        if (!isEnabled()) {
            throw new BusinessException("S3 service is not enabled or not initialized");
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

    @Override
    public void uploadObject(String s3Key, byte[] content, String contentType) {
        if (!isEnabled()) {
            throw new BusinessException("S3 service is not enabled or not initialized");
        }
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(s3Key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(content)
            );
        } catch (Exception e) {
            log.error("Failed to upload S3 object: {}", s3Key, e);
            throw new BusinessException("Failed to upload file to S3");
        }
    }

    @Override
    public java.util.List<String> listKeys(String prefix) {
        if (!isEnabled()) {
            throw new BusinessException("S3 service is not enabled or not initialized");
        }

        java.util.List<String> keys = new java.util.ArrayList<>();
        String continuationToken = null;
        do {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(prefix)
                    .continuationToken(continuationToken)
                    .build();
            ListObjectsV2Response response = s3Client.listObjectsV2(request);
            response.contents().forEach(obj -> keys.add(obj.key()));
            continuationToken = response.nextContinuationToken();
        } while (continuationToken != null && !continuationToken.isBlank());

        return keys;
    }
}
