package org.example.nexfit.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String id;
    private String name;
    private String email;
    private String token;
    private String refreshToken;
    private String role;
    private Long expiresIn;
}
