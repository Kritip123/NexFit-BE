package org.example.nexfit.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "training_subcategories")
@CompoundIndex(name = "category_order_idx", def = "{'categoryId': 1, 'displayOrder': 1}")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingSubcategory {

    @Id
    private String id;

    @Indexed
    private String categoryId;

    private String name;

    private String description;

    private String icon; // Optional icon

    @Builder.Default
    private Integer displayOrder = 0;

    @Builder.Default
    private Boolean active = true;

    @CreatedDate
    private LocalDateTime createdAt;
}
