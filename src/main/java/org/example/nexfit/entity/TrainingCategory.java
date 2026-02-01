package org.example.nexfit.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "training_categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingCategory {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private String icon; // Icon identifier or URL

    private String description;

    private String imageUrl; // Category image for UI

    @Builder.Default
    private Integer displayOrder = 0;

    @Builder.Default
    private Boolean active = true;

    @CreatedDate
    private LocalDateTime createdAt;
}
