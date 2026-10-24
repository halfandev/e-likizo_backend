
package com.abe.e_likizo.leave.dto;

public record LeaveSummaryResponse(
        long approved,
        long rejected,
        long pending
) {}
