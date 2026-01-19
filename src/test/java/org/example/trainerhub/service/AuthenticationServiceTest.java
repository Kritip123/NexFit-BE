package org.example.trainerhub.service;

import org.example.trainerhub.entity.User;
import org.example.trainerhub.exception.BusinessException;
import org.example.trainerhub.model.request.AuthRequest;
import org.example.trainerhub.model.response.AuthResponse;
import org.example.trainerhub.repository.UserRepository;
import org.example.trainerhub.security.JwtService;
import org.example.trainerhub.service.impl.AuthenticationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtService jwtService;
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private EmailService emailService;
    
    @InjectMocks
    private AuthenticationServiceImpl authenticationService;
    
    private AuthRequest.RegisterRequest registerRequest;
    private AuthRequest.LoginRequest loginRequest;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        registerRequest = AuthRequest.RegisterRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .password("SecurePass123!")
                .phone("0412345678")
                .build();
        
        loginRequest = AuthRequest.LoginRequest.builder()
                .email("john@example.com")
                .password("SecurePass123!")
                .build();
        
        testUser = User.builder()
                .id("user-123")
                .name("John Doe")
                .email("john@example.com")
                .password("encodedPassword")
                .phone("0412345678")
                .role(User.UserRole.USER)
                .isActive(true)
                .emailVerified(false)
                .build();
    }
    
    @Test
    void register_Success() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(anyMap(), any(User.class))).thenReturn("jwt-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
        when(jwtService.getExpirationTime()).thenReturn(86400000L);
        
        // When
        AuthResponse response = authenticationService.register(registerRequest);
        
        // Then
        assertNotNull(response);
        assertEquals("user-123", response.getId());
        assertEquals("John Doe", response.getName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals("jwt-token", response.getToken());
        assertEquals("refresh-token", response.getRefreshToken());
        
        verify(emailService).sendWelcomeEmail(eq("john@example.com"), eq("John Doe"));
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void register_EmailAlreadyExists_ThrowsException() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        
        // When & Then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authenticationService.register(registerRequest)
        );
        
        assertEquals("Email already registered", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendWelcomeEmail(anyString(), anyString());
    }
    
    @Test
    void login_Success() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(anyMap(), any(User.class))).thenReturn("jwt-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
        when(jwtService.getExpirationTime()).thenReturn(86400000L);
        
        // When
        AuthResponse response = authenticationService.login(loginRequest);
        
        // Then
        assertNotNull(response);
        assertEquals("user-123", response.getId());
        assertEquals("John Doe", response.getName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals("jwt-token", response.getToken());
        assertEquals("refresh-token", response.getRefreshToken());
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
    
    @Test
    void login_InvalidCredentials_ThrowsException() {
        // Given
        doThrow(new BadCredentialsException("Invalid credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        
        // When & Then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authenticationService.login(loginRequest)
        );
        
        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRepository, never()).findByEmail(anyString());
    }
    
    @Test
    void refreshToken_Success() {
        // Given
        String refreshToken = "valid-refresh-token";
        AuthRequest.RefreshTokenRequest request = AuthRequest.RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();
        
        when(jwtService.extractUsername(refreshToken)).thenReturn("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(jwtService.isTokenValid(refreshToken, testUser)).thenReturn(true);
        when(jwtService.generateToken(anyMap(), any(User.class))).thenReturn("new-jwt-token");
        when(jwtService.getExpirationTime()).thenReturn(86400000L);
        
        // When
        AuthResponse response = authenticationService.refreshToken(request);
        
        // Then
        assertNotNull(response);
        assertEquals("new-jwt-token", response.getToken());
        assertEquals(refreshToken, response.getRefreshToken());
    }
    
    @Test
    void refreshToken_InvalidToken_ThrowsException() {
        // Given
        String refreshToken = "invalid-refresh-token";
        AuthRequest.RefreshTokenRequest request = AuthRequest.RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();
        
        when(jwtService.extractUsername(refreshToken)).thenReturn("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(jwtService.isTokenValid(refreshToken, testUser)).thenReturn(false);
        
        // When & Then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authenticationService.refreshToken(request)
        );
        
        assertEquals("Invalid refresh token", exception.getMessage());
    }
    
    @Test
    void forgotPassword_Success() {
        // Given
        AuthRequest.ForgotPasswordRequest request = AuthRequest.ForgotPasswordRequest.builder()
                .email("john@example.com")
                .build();
        
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        authenticationService.forgotPassword(request);
        
        // Then
        verify(emailService).sendPasswordResetEmail(
                eq("john@example.com"),
                eq("John Doe"),
                anyString()
        );
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void resetPassword_Success() {
        // Given
        String resetToken = "valid-reset-token";
        AuthRequest.ResetPasswordRequest request = AuthRequest.ResetPasswordRequest.builder()
                .token(resetToken)
                .newPassword("NewSecurePass123!")
                .build();
        
        testUser.setResetPasswordToken(resetToken);
        testUser.setResetPasswordTokenExpiry(java.time.LocalDateTime.now().plusHours(1));
        
        when(userRepository.findByResetPasswordToken(resetToken)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        authenticationService.resetPassword(request);
        
        // Then
        verify(emailService).sendPasswordChangedEmail(eq("john@example.com"), eq("John Doe"));
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void resetPassword_ExpiredToken_ThrowsException() {
        // Given
        String resetToken = "expired-reset-token";
        AuthRequest.ResetPasswordRequest request = AuthRequest.ResetPasswordRequest.builder()
                .token(resetToken)
                .newPassword("NewSecurePass123!")
                .build();
        
        testUser.setResetPasswordToken(resetToken);
        testUser.setResetPasswordTokenExpiry(java.time.LocalDateTime.now().minusHours(1));
        
        when(userRepository.findByResetPasswordToken(resetToken)).thenReturn(Optional.of(testUser));
        
        // When & Then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authenticationService.resetPassword(request)
        );
        
        assertEquals("Reset token has expired", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendPasswordChangedEmail(anyString(), anyString());
    }
}
