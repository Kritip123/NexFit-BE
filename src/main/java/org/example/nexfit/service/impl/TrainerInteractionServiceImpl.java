package org.example.nexfit.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nexfit.entity.TrainerInteraction;
import org.example.nexfit.entity.enums.InteractionType;
import org.example.nexfit.model.request.InteractionRequest;
import org.example.nexfit.repository.TrainerInteractionRepository;
import org.example.nexfit.service.AnalyticsService;
import org.example.nexfit.service.TrainerInteractionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerInteractionServiceImpl implements TrainerInteractionService {

    private final TrainerInteractionRepository interactionRepository;
    private final AnalyticsService analyticsService;

    @Override
    public TrainerInteraction recordInteraction(String userId, InteractionRequest request) {
        log.debug("Recording interaction: userId={}, trainerId={}, type={}",
                userId, request.getTrainerId(), request.getInteractionType());

        TrainerInteraction interaction = TrainerInteraction.builder()
                .userId(userId)
                .trainerId(request.getTrainerId())
                .interactionType(request.getInteractionType())
                .source(request.getSource())
                .matchScore(request.getMatchScore())
                .sessionId(request.getSessionId())
                .viewDurationMs(request.getViewDurationMs())
                .createdAt(LocalDateTime.now())
                .build();

        TrainerInteraction saved = interactionRepository.save(interaction);

        // Track analytics event
        var eventType = switch (request.getInteractionType()) {
            case VIEWED -> org.example.nexfit.entity.enums.AnalyticsEventType.VIEW;
            case SAVED -> org.example.nexfit.entity.enums.AnalyticsEventType.SAVE;
            case SKIPPED -> org.example.nexfit.entity.enums.AnalyticsEventType.SKIP;
        };

        analyticsService.trackEvent(
                request.getTrainerId(),
                userId,
                eventType,
                request.getSource() != null ? request.getSource().name() : null,
                request.getSessionId(),
                null
        );

        return saved;
    }

    @Override
    public List<TrainerInteraction> getUserInteractions(String userId) {
        return interactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<TrainerInteraction> getRecentInteractions(String userId, InteractionType type, int hoursBack) {
        if (userId == null) {
            return List.of();
        }
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hoursBack);
        return interactionRepository.findByUserIdAndInteractionTypeAndCreatedAtAfter(userId, type, cutoff);
    }

    @Override
    public List<TrainerInteraction> getSkippedWithHighMatchScore(String userId, int minMatchScore) {
        if (userId == null) {
            return List.of();
        }
        return interactionRepository.findByUserIdAndInteractionTypeAndMatchScoreGreaterThanEqual(
                userId, InteractionType.SKIPPED, minMatchScore);
    }

    @Override
    public boolean hasRecentInteraction(String userId, String trainerId, InteractionType type, int hoursBack) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hoursBack);
        return interactionRepository.existsByUserIdAndTrainerIdAndInteractionTypeAndCreatedAtAfter(
                userId, trainerId, type, cutoff);
    }
}
