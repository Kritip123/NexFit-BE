package org.example.trainerhub.service;

import org.example.trainerhub.model.dto.TrainerDTO;
import org.example.trainerhub.model.request.TrainerMatchRequest;
import org.example.trainerhub.model.request.TrainerSearchRequest;
import org.example.trainerhub.model.response.MatchedTrainerResponse;
import org.example.trainerhub.model.response.PageResponse;
import org.example.trainerhub.model.response.TrainerPortfolioResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TrainerService {
    
    PageResponse<TrainerDTO> searchTrainers(TrainerSearchRequest request, Pageable pageable);
    
    TrainerDTO getTrainerById(String trainerId);

    PageResponse<MatchedTrainerResponse> getMatchedTrainers(TrainerMatchRequest request, Pageable pageable);

    TrainerPortfolioResponse getTrainerPortfolio(String trainerId);
    
    Map<String, List<String>> getTrainerAvailability(String trainerId, LocalDate date);
    
    List<String> getAvailableTimeSlots(String trainerId, LocalDate date);
}
