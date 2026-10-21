
package com.abe.e_likizo.leave.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record LeaveApplicationRequest(

        @NotNull(message = "Leave type ID is required")
        Long leaveTypeId,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate,

        @Size(max = 1000, message = "Reason cannot exceed 1000 characters")
        String reason,

        Long familyMemberId

) {
}
