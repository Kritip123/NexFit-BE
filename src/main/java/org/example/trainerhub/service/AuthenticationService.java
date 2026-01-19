package org.example.trainerhub.service;

import org.example.trainerhub.model.request.AuthRequest;
import org.example.trainerhub.model.response.AuthResponse;

public interface AuthenticationService {
    
    AuthResponse register(AuthRequest.RegisterRequest request);
    
    AuthResponse login(AuthRequest.LoginRequest request);
    
    AuthResponse refreshToken(AuthRequest.RefreshTokenRequest request);
    
    void logout(String token);
    
    void forgotPassword(AuthRequest.ForgotPasswordRequest request);
    
    void resetPassword(AuthRequest.ResetPasswordRequest request);
}
