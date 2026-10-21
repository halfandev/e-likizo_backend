
package com.abe.e_likizo.leave.dto;

import com.abe.e_likizo.leave.enums.FamilyRelationship;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record FamilyMemberRequest(

        @NotBlank(message = "Full name is required")
        @Size(max = 200, message = "Full name cannot exceed 200 characters")
        String fullName,

        @NotNull(message = "Relationship is required")
        FamilyRelationship relationship,

        LocalDate dateOfBirth,

        @Size(max = 500, message = "Notes cannot exceed 500 characters")
        String notes

) {}
