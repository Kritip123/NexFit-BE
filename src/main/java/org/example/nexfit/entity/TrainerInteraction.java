package org.example.nexfit.entity;

import lombok.*;
import org.example.nexfit.entity.enums.InteractionSource;
import org.example.nexfit.entity.enums.InteractionType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "trainer_interactions")
@CompoundIndexes({
    @CompoundIndex(name = "user_trainer_idx", def = "{'userId': 1, 'trainerId': 1}"),
    @CompoundIndex(name = "user_type_date_idx", def = "{'userId': 1, 'interactionType': 1, 'createdAt': -1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerInteraction {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String trainerId;

    private InteractionType interactionType;

    private InteractionSource source;

    private Integer matchScore; // 0-100

    private String sessionId; // For tracking feed sessions

    private Long viewDurationMs; // Time spent viewing

    @CreatedDate
    private LocalDateTime createdAt;
}
