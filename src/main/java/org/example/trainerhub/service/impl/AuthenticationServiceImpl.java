package org.example.trainerhub.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.trainerhub.entity.User;
import org.example.trainerhub.exception.BusinessException;
import org.example.trainerhub.model.request.AuthRequest;
import org.example.trainerhub.model.response.AuthResponse;
import org.example.trainerhub.repository.UserRepository;
import org.example.trainerhub.security.JwtService;
import org.example.trainerhub.service.AuthenticationService;
import org.example.trainerhub.service.EmailService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    
    @Override
    public AuthResponse register(AuthRequest.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already registered");
        }

        if (request.getRole() == User.UserRole.ADMIN) {
            throw new BusinessException("Admin registration is not allowed");
        }
        
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole())
                .isActive(true)
                .emailVerified(false)
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .trainerGenderPreference(request.getTrainerGenderPreference())
                .fitnessGoals(request.getFitnessGoals())
                .preferredActivities(request.getPreferredActivities())
                .experienceLevel(request.getExperienceLevel())
                .build();
        
        user = userRepository.save(user);
        
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
        extraClaims.put("role", user.getRole().name());
        
        String jwtToken = jwtService.generateToken(extraClaims, user);
        String refreshToken = jwtService.generateRefreshToken(user);
        
        // Send welcome email
        emailService.sendWelcomeEmail(user.getEmail(), user.getName());
        
        return AuthResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .token(jwtToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getExpirationTime())
                .build();
    }
    
    @Override
    public AuthResponse login(AuthRequest.LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            throw new BusinessException("Invalid email or password");
        }
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("User not found"));
        
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
        extraClaims.put("role", user.getRole().name());
        
        String jwtToken = jwtService.generateToken(extraClaims, user);
        String refreshToken = jwtService.generateRefreshToken(user);
        
        return AuthResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .token(jwtToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getExpirationTime())
                .build();
    }
    
    @Override
    public AuthResponse refreshToken(AuthRequest.RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        String userEmail = jwtService.extractUsername(refreshToken);
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("User not found"));
        
        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new BusinessException("Invalid refresh token");
        }
        
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
        extraClaims.put("role", user.getRole().name());
        
        String newJwtToken = jwtService.generateToken(extraClaims, user);
        
        return AuthResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .token(newJwtToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getExpirationTime())
                .build();
    }
    
    @Override
    public void logout(String token) {
        // In a production environment, you might want to blacklist the token
        // For now, logout is handled client-side by removing the token
        log.info("User logged out");
    }
    
    @Override
    public void forgotPassword(AuthRequest.ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("User not found with this email"));
        
        String resetToken = UUID.randomUUID().toString();
        user.setResetPasswordToken(resetToken);
        user.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(24));
        
        userRepository.save(user);
        
        emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), resetToken);
    }
    
    @Override
    public void resetPassword(AuthRequest.ResetPasswordRequest request) {
        User user = userRepository.findByResetPasswordToken(request.getToken())
                .orElseThrow(() -> new BusinessException("Invalid reset token"));
        
        if (user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Reset token has expired");
        }
        
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        
        userRepository.save(user);
        
        emailService.sendPasswordChangedEmail(user.getEmail(), user.getName());
    }
}
