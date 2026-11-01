package com.abe.e_likizo.leave.dto;

import com.abe.e_likizo.leave.enums.LeaveStatus;
import java.time.LocalDateTime;

public record LeaveWorkflowHistoryResponse(
        Long id,
        String actorEmail,
        String action,
        LeaveStatus previousStatus,
        LeaveStatus newStatus,
        String comment,
        LocalDateTime actionAt
) {
}