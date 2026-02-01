package org.example.nexfit.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nexfit.entity.TrainerAnalyticsEvent;
import org.example.nexfit.entity.enums.AnalyticsEventType;
import org.example.nexfit.repository.TrainerAnalyticsEventRepository;
import org.example.nexfit.service.AnalyticsService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

    private final TrainerAnalyticsEventRepository analyticsRepository;

    @Override
    @Async
    public void trackEvent(String trainerId, String userId, AnalyticsEventType eventType,
                          String source, String sessionId, Map<String, Object> metadata) {
        try {
            TrainerAnalyticsEvent event = TrainerAnalyticsEvent.builder()
                    .trainerId(trainerId)
                    .userId(userId)
                    .eventType(eventType)
                    .source(source)
                    .sessionId(sessionId)
                    .metadata(metadata != null ? metadata : new HashMap<>())
                    .createdAt(LocalDateTime.now())
                    .build();

            analyticsRepository.save(event);
            log.debug("Tracked analytics event: type={}, trainerId={}", eventType, trainerId);
        } catch (Exception e) {
            log.error("Failed to track analytics event", e);
        }
    }

    @Override
    @Async
    public void trackView(String trainerId, String userId, String source, String sessionId) {
        trackEvent(trainerId, userId, AnalyticsEventType.VIEW, source, sessionId, null);
    }

    @Override
    @Async
    public void trackSave(String trainerId, String userId, String source, String sessionId) {
        trackEvent(trainerId, userId, AnalyticsEventType.SAVE, source, sessionId, null);
    }

    @Override
    @Async
    public void trackContact(String trainerId, String userId, String contactMethod, String sessionId) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("contactMethod", contactMethod);
        trackEvent(trainerId, userId, AnalyticsEventType.CONTACT, "profile", sessionId, metadata);
    }
}
