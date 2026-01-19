package org.example.trainerhub.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "saved_trainers")
@CompoundIndexes({
    @CompoundIndex(name = "user_trainer_unique_idx", def = "{'userId': 1, 'trainerId': 1}", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedTrainer {
    
    @Id
    private String id;
    
    private String userId;
    
    private String trainerId;
    
    private Integer matchPercentage;
    
    private Boolean isSuperLike;
    
    @CreatedDate
    private LocalDateTime savedAt;
}
