package org.example.nexfit.service;

import org.example.nexfit.entity.TrainerAnalyticsEvent;
import org.example.nexfit.entity.enums.AnalyticsEventType;

import java.util.Map;

public interface AnalyticsService {

    void trackEvent(String trainerId, String userId, AnalyticsEventType eventType,
                    String source, String sessionId, Map<String, Object> metadata);

    void trackView(String trainerId, String userId, String source, String sessionId);

    void trackSave(String trainerId, String userId, String source, String sessionId);

    void trackContact(String trainerId, String userId, String contactMethod, String sessionId);
}
