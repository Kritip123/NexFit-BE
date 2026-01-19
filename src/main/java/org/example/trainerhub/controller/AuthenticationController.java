package org.example.trainerhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.trainerhub.model.request.AuthRequest;
import org.example.trainerhub.model.response.AuthResponse;
import org.example.trainerhub.service.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthenticationController {
    
    private final AuthenticationService authenticationService;
    
    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest.RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authenticationService.register(request));
    }
    
    @PostMapping("/login")
    @Operation(summary = "User login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest.LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }
    
    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh JWT token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody AuthRequest.RefreshTokenRequest request) {
        return ResponseEntity.ok(authenticationService.refreshToken(request));
    }
    
    @PostMapping("/logout")
    @Operation(summary = "User logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        authenticationService.logout(token);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/forgot-password")
    @Operation(summary = "Send password reset email")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody AuthRequest.ForgotPasswordRequest request) {
        authenticationService.forgotPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password reset email sent successfully"));
    }
    
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password with token")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody AuthRequest.ResetPasswordRequest request) {
        authenticationService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }
}
