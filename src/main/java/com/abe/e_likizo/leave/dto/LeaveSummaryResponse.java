package com.abe.e_likizo.leave.dto;

public record LeaveSummaryResponse(
        long total,
        long ocs,
        long ocd,
        long rpc,
        long cp,
        long igp,
        long approved,
        long rejected
) {
}