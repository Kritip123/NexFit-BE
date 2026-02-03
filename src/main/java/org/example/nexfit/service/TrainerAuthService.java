package org.example.nexfit.service;

import org.example.nexfit.model.request.TrainerAuthRequest;
import org.example.nexfit.model.response.TrainerAuthResponse;

public interface TrainerAuthService {

    TrainerAuthResponse register(TrainerAuthRequest.RegisterRequest request);

    TrainerAuthResponse login(TrainerAuthRequest.LoginRequest request);
}
