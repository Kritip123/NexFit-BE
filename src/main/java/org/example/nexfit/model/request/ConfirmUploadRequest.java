package org.example.nexfit.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.nexfit.entity.enums.MediaType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmUploadRequest {

    @NotBlank(message = "S3 key is required")
    private String s3Key;

    @NotNull(message = "Media type is required")
    private MediaType mediaType;

    private String title;
    private String description;

    // For transformations
    private String beforeImageUrl;
    private String afterImageUrl;

    // Media metadata
    private Long fileSizeBytes;
    private String mimeType;
    private Integer durationSeconds;
    private Integer width;
    private Integer height;
}
