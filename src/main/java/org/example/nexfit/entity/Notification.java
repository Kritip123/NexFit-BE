package org.example.nexfit.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "notifications")
@CompoundIndexes({
    @CompoundIndex(name = "user_read_created_idx", def = "{'userId': 1, 'read': 1, 'createdAt': -1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    private String id;
    
    @Indexed
    private String userId;
    
    @Indexed
    private NotificationType type;
    
    private String title;
    
    private String message;
    
    private Map<String, Object> data;
    
    @Builder.Default
    private Boolean read = false;
    
    private LocalDateTime readAt;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    public enum NotificationType {
        BOOKING_CONFIRMED,
        BOOKING_REMINDER,
        BOOKING_CANCELLED,
        BOOKING_RESCHEDULED,
        PAYMENT_SUCCESS,
        PAYMENT_FAILED,
        REVIEW_REQUEST,
        NEW_MESSAGE,
        TRAINER_UPDATE,
        SYSTEM_ANNOUNCEMENT,
        WELCOME
    }
}
