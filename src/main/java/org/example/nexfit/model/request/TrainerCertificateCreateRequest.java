package org.example.nexfit.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerCertificateCreateRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Issuer is required")
    private String issuer;

    @NotNull(message = "Issued date is required")
    private LocalDate issuedDate;

    private LocalDate expiresDate;

    @NotBlank(message = "File URL is required")
    private String fileUrl;

    @NotBlank(message = "File key is required")
    private String fileKey;
}
