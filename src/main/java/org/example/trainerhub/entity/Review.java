package org.example.trainerhub.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    
    @Id
    private String id;
    
    @Indexed
    private String trainerId;

    @Indexed
    private String userId;
    
    private Integer rating; // 1-5
    
    private String comment;
    
    @CreatedDate
    private LocalDateTime createdAt;
}
