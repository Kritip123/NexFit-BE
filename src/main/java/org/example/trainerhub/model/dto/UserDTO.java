package org.example.trainerhub.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String avatar;
    private LocalDateTime createdAt;
    private Boolean emailVerified;
}
