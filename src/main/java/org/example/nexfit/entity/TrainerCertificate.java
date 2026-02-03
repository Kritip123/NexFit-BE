package org.example.nexfit.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "trainer_certificates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerCertificate {

    @Id
    private String id;

    @Indexed
    private String trainerId;

    private String name;
    private String issuer;
    private LocalDate issuedDate;
    private LocalDate expiresDate;

    private String fileUrl;
    private String fileKey;

    @CreatedDate
    private LocalDateTime createdAt;
}
