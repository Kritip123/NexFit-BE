package org.example.nexfit.service;

import org.example.nexfit.entity.Trainer;

public interface TrainerVisibilityService {

    boolean isVisibleToUsers(Trainer trainer);
}
