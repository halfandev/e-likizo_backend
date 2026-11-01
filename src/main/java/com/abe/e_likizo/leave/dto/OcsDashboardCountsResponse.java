package com.abe.e_likizo.leave.dto;

public record OcsDashboardCountsResponse(
        long waitingForOCS,
        long waitingForRPC,
        long waitingForCP,
        long waitingForIGP
) {
}