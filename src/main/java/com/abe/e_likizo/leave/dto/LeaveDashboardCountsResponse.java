package com.abe.e_likizo.leave.dto;

public record LeaveDashboardCountsResponse(
        long waitingForOCD,
        long waitingForRPC,
        long waitingForCP,
        long waitingForIGP
) {
}