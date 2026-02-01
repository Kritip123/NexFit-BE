package org.example.nexfit.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    
    @Id
    private String id;
    
    private String name;
    
    @Indexed(unique = true)
    private String email;
    
    private String password;
    
    private String phone;
    
    private String avatar;
    
    @Builder.Default
    private UserRole role = UserRole.USER;
    
    @Builder.Default
    private Boolean isActive = true;
    
    @Builder.Default
    private Boolean emailVerified = false;
    
    private String resetPasswordToken;
    
    private LocalDateTime resetPasswordTokenExpiry;

    private LocalDate dateOfBirth;

    private Gender gender;

    private TrainerGenderPreference trainerGenderPreference;

    @Builder.Default
    private List<String> fitnessGoals = List.of();

    @Builder.Default
    private List<String> preferredActivities = List.of();

    private ExperienceLevel experienceLevel;

    // Training category preferences (two-step onboarding)
    @Builder.Default
    private List<String> selectedCategories = List.of();

    @Builder.Default
    private List<String> selectedSubcategories = List.of();

    // User location for distance-based matching
    private Double latitude;
    private Double longitude;

    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    
    @Override
    public String getUsername() {
        return email;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return isActive;
    }
    
    public enum UserRole {
        USER, TRAINER, ADMIN
    }

    public enum Gender {
        MALE, FEMALE, NON_BINARY, PREFER_NOT_TO_SAY
    }

    public enum TrainerGenderPreference {
        MALE, FEMALE, NO_PREFERENCE
    }

    public enum ExperienceLevel {
        BEGINNER, INTERMEDIATE, ADVANCED, PROFESSIONAL
    }
}
