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
public class TrainerMediaUploadUrlRequest {

    @NotBlank(message = "File name is required")
    private String fileName;

    @NotBlank(message = "Content type is required")
    private String contentType;

    @NotNull(message = "File size is required")
    private Long sizeBytes;

    @NotNull(message = "Media type is required")
    private MediaType type;
}
