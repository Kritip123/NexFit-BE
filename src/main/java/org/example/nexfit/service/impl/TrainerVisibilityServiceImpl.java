package org.example.nexfit.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.nexfit.entity.Trainer;
import org.example.nexfit.entity.enums.TrainerStatus;
import org.example.nexfit.service.LaunchDarklyService;
import org.example.nexfit.service.TrainerVisibilityService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrainerVisibilityServiceImpl implements TrainerVisibilityService {

    private final LaunchDarklyService launchDarklyService;

    @Override
    public boolean isVisibleToUsers(Trainer trainer) {
        if (trainer == null) {
            return false;
        }
        if (!Boolean.TRUE.equals(trainer.getIsActive())) {
            return false;
        }
        return launchDarklyService.isTrainerVerified(trainer.getEmail());
    }
}
