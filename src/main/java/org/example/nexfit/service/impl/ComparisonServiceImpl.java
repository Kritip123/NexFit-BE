package org.example.nexfit.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nexfit.entity.SavedTrainer;
import org.example.nexfit.entity.Trainer;
import org.example.nexfit.entity.TrainerInteraction;
import org.example.nexfit.entity.enums.InteractionType;
import org.example.nexfit.model.response.ComparisonResponse;
import org.example.nexfit.model.response.TrainerComparisonCard;
import org.example.nexfit.model.response.TrainerFeedCard;
import org.example.nexfit.repository.SavedTrainerRepository;
import org.example.nexfit.repository.TrainerRepository;
import org.example.nexfit.service.ComparisonService;
import org.example.nexfit.service.TrainerInteractionService;
import org.example.nexfit.util.DistanceCalculator;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComparisonServiceImpl implements ComparisonService {

    private final SavedTrainerRepository savedTrainerRepository;
    private final TrainerRepository trainerRepository;
    private final TrainerInteractionService interactionService;

    private static final int MIN_RECONSIDERATION_SCORE = 80;
    private static final int RECENT_VIEWS_HOURS = 48;

    @Override
    public ComparisonResponse getComparison(String userId, Double latitude, Double longitude) {
        log.debug("Getting comparison data for user: {}", userId);

        // For anonymous users, return empty comparison
        if (userId == null) {
            return ComparisonResponse.builder()
                    .saved(List.of())
                    .suggestedReconsiderations(List.of())
                    .recentlyViewed(List.of())
                    .build();
        }

        // Get saved trainers
        List<SavedTrainer> savedTrainers = savedTrainerRepository.findByUserIdOrderBySavedAtDesc(userId);
        Set<String> savedTrainerIds = savedTrainers.stream()
                .map(SavedTrainer::getTrainerId)
                .collect(Collectors.toSet());

        // Get trainers data
        Map<String, Trainer> trainerMap = trainerRepository.findAllById(savedTrainerIds).stream()
                .collect(Collectors.toMap(Trainer::getId, t -> t));

        // Build saved cards
        List<TrainerComparisonCard> savedCards = savedTrainers.stream()
                .filter(st -> trainerMap.containsKey(st.getTrainerId()))
                .map(st -> buildComparisonCard(
                        trainerMap.get(st.getTrainerId()),
                        st.getMatchPercentage(),
                        latitude,
                        longitude,
                        st.getSavedAt(),
                        null,
                        null
                ))
                .toList();

        // Get suggested reconsiderations (skipped with high match score)
        List<TrainerInteraction> highScoreSkipped = interactionService.getSkippedWithHighMatchScore(userId, MIN_RECONSIDERATION_SCORE);
        Set<String> reconsiderationIds = highScoreSkipped.stream()
                .map(TrainerInteraction::getTrainerId)
                .filter(id -> !savedTrainerIds.contains(id)) // Exclude already saved
                .collect(Collectors.toSet());

        Map<String, TrainerInteraction> skippedInteractionMap = highScoreSkipped.stream()
                .collect(Collectors.toMap(
                        TrainerInteraction::getTrainerId,
                        i -> i,
                        (a, b) -> a.getCreatedAt().isAfter(b.getCreatedAt()) ? a : b // Keep most recent
                ));

        Map<String, Trainer> reconsiderationTrainerMap = trainerRepository.findAllById(reconsiderationIds).stream()
                .filter(Trainer::getIsActive)
                .collect(Collectors.toMap(Trainer::getId, t -> t));

        List<TrainerComparisonCard> reconsiderationCards = reconsiderationIds.stream()
                .filter(reconsiderationTrainerMap::containsKey)
                .map(id -> {
                    TrainerInteraction interaction = skippedInteractionMap.get(id);
                    return buildComparisonCard(
                            reconsiderationTrainerMap.get(id),
                            interaction.getMatchScore(),
                            latitude,
                            longitude,
                            null,
                            null,
                            interaction.getCreatedAt()
                    );
                })
                .sorted((a, b) -> Integer.compare(
                        b.getMatchScore() != null ? b.getMatchScore() : 0,
                        a.getMatchScore() != null ? a.getMatchScore() : 0
                ))
                .toList();

        // Get recently viewed (optional, for richer UI)
        List<TrainerInteraction> recentViews = interactionService.getRecentInteractions(userId, InteractionType.VIEWED, RECENT_VIEWS_HOURS);
        Set<String> viewedIds = recentViews.stream()
                .map(TrainerInteraction::getTrainerId)
                .filter(id -> !savedTrainerIds.contains(id) && !reconsiderationIds.contains(id))
                .collect(Collectors.toSet());

        Map<String, TrainerInteraction> viewedInteractionMap = recentViews.stream()
                .collect(Collectors.toMap(
                        TrainerInteraction::getTrainerId,
                        i -> i,
                        (a, b) -> a.getCreatedAt().isAfter(b.getCreatedAt()) ? a : b
                ));

        Map<String, Trainer> viewedTrainerMap = trainerRepository.findAllById(viewedIds).stream()
                .filter(Trainer::getIsActive)
                .collect(Collectors.toMap(Trainer::getId, t -> t));

        List<TrainerComparisonCard> viewedCards = viewedIds.stream()
                .filter(viewedTrainerMap::containsKey)
                .map(id -> {
                    TrainerInteraction interaction = viewedInteractionMap.get(id);
                    return buildComparisonCard(
                            viewedTrainerMap.get(id),
                            interaction.getMatchScore(),
                            latitude,
                            longitude,
                            null,
                            interaction.getCreatedAt(),
                            null
                    );
                })
                .limit(10) // Limit recently viewed
                .toList();

        return ComparisonResponse.builder()
                .saved(savedCards)
                .suggestedReconsiderations(reconsiderationCards)
                .recentlyViewed(viewedCards)
                .build();
    }

    private TrainerComparisonCard buildComparisonCard(Trainer trainer, Integer matchScore,
                                                       Double userLat, Double userLng,
                                                       java.time.LocalDateTime savedAt,
                                                       java.time.LocalDateTime viewedAt,
                                                       java.time.LocalDateTime skippedAt) {
        // Get primary contact
        TrainerFeedCard.ContactInfo primaryContact = null;
        if (trainer.getContactMethods() != null) {
            var primary = trainer.getContactMethods().stream()
                    .filter(cm -> Boolean.TRUE.equals(cm.getIsPrimary()))
                    .findFirst()
                    .orElse(trainer.getContactMethods().isEmpty() ? null : trainer.getContactMethods().get(0));

            if (primary != null) {
                primaryContact = TrainerFeedCard.ContactInfo.builder()
                        .type(primary.getType())
                        .value(primary.getValue())
                        .label(primary.getLabel())
                        .build();
            }
        }

        // Calculate distance
        Double distance = null;
        if (userLat != null && userLng != null && trainer.getLatitude() != null && trainer.getLongitude() != null) {
            distance = DistanceCalculator.calculateDistance(userLat, userLng, trainer.getLatitude(), trainer.getLongitude());
        }

        return TrainerComparisonCard.builder()
                .id(trainer.getId())
                .name(trainer.getName())
                .profileImage(trainer.getProfileImage())
                .bio(trainer.getBio())
                .specializations(trainer.getSpecializations())
                .rating(trainer.getRating())
                .reviewCount(trainer.getReviewCount())
                .hourlyRate(trainer.getHourlyRate())
                .distance(distance)
                .matchScore(matchScore)
                .experience(trainer.getExperience())
                .city(trainer.getCity())
                .state(trainer.getState())
                .savedAt(savedAt)
                .viewedAt(viewedAt)
                .skippedAt(skippedAt)
                .primaryContact(primaryContact)
                .build();
    }
}
