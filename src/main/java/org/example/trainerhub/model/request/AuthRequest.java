package org.example.trainerhub.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.trainerhub.entity.User;

import java.time.LocalDate;
import java.util.List;

public class AuthRequest {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        @NotBlank(message = "Name is required")
        private String name;
        
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;
        
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",
                message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character")
        private String password;
        
        @Pattern(regexp = "^(\\+61|0)[0-9]{9}$", message = "Invalid Australian phone number")
        private String phone;

        @NotNull(message = "Role is required")
        private User.UserRole role;

        @NotNull(message = "Date of birth is required")
        private LocalDate dateOfBirth;

        @NotNull(message = "Gender is required")
        private User.Gender gender;

        @NotNull(message = "Trainer gender preference is required")
        private User.TrainerGenderPreference trainerGenderPreference;

        @NotNull(message = "Fitness goals are required")
        private List<String> fitnessGoals;

        @NotNull(message = "Preferred activities are required")
        private List<String> preferredActivities;

        @NotNull(message = "Experience level is required")
        private User.ExperienceLevel experienceLevel;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;
        
        @NotBlank(message = "Password is required")
        private String password;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshTokenRequest {
        @NotBlank(message = "Refresh token is required")
        private String refreshToken;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForgotPasswordRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResetPasswordRequest {
        @NotBlank(message = "Token is required")
        private String token;
        
        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",
                message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character")
        private String newPassword;
    }
}
