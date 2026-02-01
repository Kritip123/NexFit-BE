package org.example.nexfit.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadUrlResponse {

    private String uploadUrl;
    private String s3Key;
    private LocalDateTime expiresAt;

    // Headers that must be included in the upload request
    private String contentType;
}
