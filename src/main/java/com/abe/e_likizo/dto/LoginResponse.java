
package com.abe.e_likizo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String tokenType;

    // ID ya akaunti ya login
    private Long userId;

    // ID ya mfanyakazi kwenye employees table
    private Long employeeId;

    private String fullName;
    private String email;
    private String role;
    private boolean mustChangePassword;
}
