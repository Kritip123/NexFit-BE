package org.example.nexfit.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerProfileUpdateRequest {

    @Valid
    private Profile profile;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Profile {
        private String fullName;

        @Email(message = "Invalid email format")
        private String email;

        private String phone;
        private String city;
        private String headline;
        private String bio;
        private String profileImage;
        private String coverImage;
        private List<String> specializations;
        private Integer yearsActive;
        private List<String> languages;
        private Pricing pricing;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pricing {
        private Integer monthlySubscriptionUSD;
    }
}
