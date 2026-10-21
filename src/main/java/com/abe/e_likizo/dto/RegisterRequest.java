
package com.abe.e_likizo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Check Number is required")
    private String checknumber;

    @NotBlank(message = "Force Number is required")
    private String forcenumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    @NotBlank(message = "Full name is required")
    private String fullName;
}
