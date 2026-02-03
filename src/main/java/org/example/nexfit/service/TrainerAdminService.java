package org.example.nexfit.service;

import org.example.nexfit.entity.Trainer;

public interface TrainerAdminService {

    Trainer approveTrainer(String trainerId);

    Trainer rejectTrainer(String trainerId);
}
