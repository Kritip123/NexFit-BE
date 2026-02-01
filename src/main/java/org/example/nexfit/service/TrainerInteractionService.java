package org.example.nexfit.service;

import org.example.nexfit.entity.TrainerInteraction;
import org.example.nexfit.entity.enums.InteractionType;
import org.example.nexfit.model.request.InteractionRequest;

import java.util.List;

public interface TrainerInteractionService {

    TrainerInteraction recordInteraction(String userId, InteractionRequest request);

    List<TrainerInteraction> getUserInteractions(String userId);

    List<TrainerInteraction> getRecentInteractions(String userId, InteractionType type, int hoursBack);

    List<TrainerInteraction> getSkippedWithHighMatchScore(String userId, int minMatchScore);

    boolean hasRecentInteraction(String userId, String trainerId, InteractionType type, int hoursBack);
}
