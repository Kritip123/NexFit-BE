package org.example.nexfit.repository;

import org.example.nexfit.entity.TrainerAnalyticsEvent;
import org.example.nexfit.entity.enums.AnalyticsEventType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrainerAnalyticsEventRepository extends MongoRepository<TrainerAnalyticsEvent, String> {

    List<TrainerAnalyticsEvent> findByTrainerIdOrderByCreatedAtDesc(String trainerId);

    List<TrainerAnalyticsEvent> findByTrainerIdAndEventTypeOrderByCreatedAtDesc(
            String trainerId, AnalyticsEventType eventType);

    List<TrainerAnalyticsEvent> findByTrainerIdAndCreatedAtBetween(
            String trainerId, LocalDateTime start, LocalDateTime end);

    long countByTrainerIdAndEventType(String trainerId, AnalyticsEventType eventType);

    long countByTrainerIdAndEventTypeAndCreatedAtBetween(
            String trainerId, AnalyticsEventType eventType, LocalDateTime start, LocalDateTime end);

    List<TrainerAnalyticsEvent> findByUserIdOrderByCreatedAtDesc(String userId);
}
