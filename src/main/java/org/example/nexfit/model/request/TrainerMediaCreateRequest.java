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
public class TrainerMediaCreateRequest {

    @NotNull(message = "Media type is required")
    private MediaType type;

    @NotBlank(message = "URL is required")
    private String url;

    private String thumbnailUrl;

    private Integer durationSec;

    private Integer order;

    @NotBlank(message = "File key is required")
    private String fileKey;

    private Long sizeBytes;
}
