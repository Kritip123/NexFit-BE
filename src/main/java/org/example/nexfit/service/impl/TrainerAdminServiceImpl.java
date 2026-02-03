package org.example.nexfit.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.nexfit.entity.Trainer;
import org.example.nexfit.entity.enums.TrainerStatus;
import org.example.nexfit.exception.BusinessException;
import org.example.nexfit.exception.ResourceNotFoundException;
import org.example.nexfit.repository.TrainerRepository;
import org.example.nexfit.service.TrainerAdminService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TrainerAdminServiceImpl implements TrainerAdminService {

    private final TrainerRepository trainerRepository;

    @Override
    public Trainer approveTrainer(String trainerId) {
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer", trainerId));

        if (trainer.getStatus() != TrainerStatus.SUBMITTED) {
            throw new BusinessException("Trainer is not in submitted status");
        }

        trainer.setStatus(TrainerStatus.APPROVED);
        trainer.setApprovedAt(LocalDateTime.now());
        return trainerRepository.save(trainer);
    }

    @Override
    public Trainer rejectTrainer(String trainerId) {
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer", trainerId));

        if (trainer.getStatus() != TrainerStatus.SUBMITTED) {
            throw new BusinessException("Trainer is not in submitted status");
        }

        trainer.setStatus(TrainerStatus.REJECTED);
        return trainerRepository.save(trainer);
    }
}
