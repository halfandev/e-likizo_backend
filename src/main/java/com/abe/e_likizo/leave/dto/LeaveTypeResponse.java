
package com.abe.e_likizo.leave.dto;

public record LeaveTypeResponse(
        Long id,
        String code,
        String name,
        String description,
        Integer defaultDays,
        boolean requiresDocument,
        boolean active
) {
}
