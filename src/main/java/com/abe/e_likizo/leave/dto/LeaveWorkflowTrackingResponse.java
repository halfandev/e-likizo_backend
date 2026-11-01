package com.abe.e_likizo.leave.dto;

import java.util.List;

public record LeaveWorkflowTrackingResponse(
        LeaveApplicationResponse application,
        List<LeaveWorkflowHistoryResponse> history
) {
}