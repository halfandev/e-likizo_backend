
package com.abe.e_likizo.employee.dto;

import com.abe.e_likizo.employee.enums.Rank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record EmployeeRegistrationRequest(

        @NotBlank(message = "Check number is required")
        String checknumber,

        @NotBlank(message = "Force number is required")
        String forcenumber,

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        String email,

        @NotBlank(message = "First name is required")
        String firstname,

        String middlename,

        @NotBlank(message = "Last name is required")
        String lastname,

        @NotBlank(message = "Gender is required")
        @Pattern(
                regexp = "(?i)MALE|FEMALE",
                message = "Gender must be MALE or FEMALE"
        )
        String gender,

        @NotNull(message = "Administrative is required")
        @Positive(message = "Administrative ID must be positive")
        Long administrativeId,

        @Positive(message = "Region ID must be positive")
        Long regionId,

        @Positive(message = "District ID must be positive")
        Long districtId,

        @Positive(message = "Station ID must be positive")
        Long stationId,

        @NotNull(message = "Rank is required")
        Rank rank

) {}
