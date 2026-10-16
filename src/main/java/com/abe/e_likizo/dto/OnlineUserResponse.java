
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
public class OnlineUserResponse {

    private Long id;
    private String fullName;
    private String email;
    private LocalDateTime lastLogin;
    private LocalDateTime lastActivity;
    private long onlineDurationMinutes;
}
