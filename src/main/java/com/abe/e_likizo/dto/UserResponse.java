package com.abe.e_likizo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;

    private String fullName;

    private String email;

    private String role;

    private boolean enabled;

    private boolean mustChangePassword;

    private LocalDateTime lastLogin;

    private LocalDateTime lastLogout;

    private LocalDateTime createdAt;

    // Administrative information
    private Long administrativeId;
    private String administrativeName;

    // Region information
    private Long regionId;
    private String regionName;

    // District information
    private Long districtId;
    private String districtName;

    // Station information
    private Long stationId;
    private String stationName;
}