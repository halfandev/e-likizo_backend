
package com.abe.e_likizo.leave.dto;

import com.abe.e_likizo.leave.enums.LeaveStatus;
import java.time.LocalDate;

public record LeaveApplicationResponse(
        Long id,
        Long employeeId,
        Long leaveTypeId,
        String leaveTypeName,
        LocalDate startDate,
        LocalDate endDate,
        Integer requestedDays,
        String reason,
        LeaveStatus status,
        Long familyMemberId
) {}
