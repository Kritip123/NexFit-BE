package org.example.nexfit.entity;

import lombok.*;
import org.example.nexfit.entity.enums.AnalyticsEventType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "trainer_analytics_events")
@CompoundIndexes({
    @CompoundIndex(name = "trainer_event_date_idx", def = "{'trainerId': 1, 'eventType': 1, 'createdAt': -1}"),
    @CompoundIndex(name = "user_event_date_idx", def = "{'userId': 1, 'eventType': 1, 'createdAt': -1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerAnalyticsEvent {

    @Id
    private String id;

    @Indexed
    private String trainerId;

    @Indexed
    private String userId; // Can be null for anonymous views

    private AnalyticsEventType eventType;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    private String sessionId; // For session tracking

    private String source; // Where the event originated (feed, search, profile, etc.)

    @CreatedDate
    private LocalDateTime createdAt;
}
