package org.example.nexfit.service;

import org.example.nexfit.model.dto.TrainerDTO;
import org.example.nexfit.model.request.TrainerMatchRequest;
import org.example.nexfit.model.request.TrainerSearchRequest;
import org.example.nexfit.model.response.MatchedTrainerResponse;
import org.example.nexfit.model.response.PageResponse;
import org.example.nexfit.model.response.TrainerPortfolioResponse;
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
