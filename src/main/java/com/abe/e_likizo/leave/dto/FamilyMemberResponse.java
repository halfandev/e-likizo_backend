
package com.abe.e_likizo.leave.dto;

import com.abe.e_likizo.leave.enums.FamilyRelationship;

import java.time.LocalDate;

public record FamilyMemberResponse(
        Long id,
        Long employeeId,
        String fullName,
        FamilyRelationship relationship,
        LocalDate dateOfBirth,
        String notes
) {}
