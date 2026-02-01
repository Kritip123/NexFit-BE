package org.example.nexfit.repository;

import org.example.nexfit.entity.TrainerInteraction;
import org.example.nexfit.entity.enums.InteractionType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerInteractionRepository extends MongoRepository<TrainerInteraction, String> {

    List<TrainerInteraction> findByUserIdAndInteractionTypeOrderByCreatedAtDesc(
            String userId, InteractionType interactionType);

    List<TrainerInteraction> findByUserIdAndInteractionTypeAndCreatedAtAfter(
            String userId, InteractionType interactionType, LocalDateTime after);

    Optional<TrainerInteraction> findFirstByUserIdAndTrainerIdAndInteractionTypeOrderByCreatedAtDesc(
            String userId, String trainerId, InteractionType interactionType);

    List<TrainerInteraction> findByUserIdOrderByCreatedAtDesc(String userId);

    List<TrainerInteraction> findByTrainerIdOrderByCreatedAtDesc(String trainerId);

    List<TrainerInteraction> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
            String userId, LocalDateTime after);

    // For finding skipped trainers with high match scores (reconsiderations)
    List<TrainerInteraction> findByUserIdAndInteractionTypeAndMatchScoreGreaterThanEqual(
            String userId, InteractionType interactionType, Integer matchScore);

    long countByTrainerIdAndInteractionType(String trainerId, InteractionType interactionType);

    boolean existsByUserIdAndTrainerIdAndInteractionTypeAndCreatedAtAfter(
            String userId, String trainerId, InteractionType interactionType, LocalDateTime after);
}
