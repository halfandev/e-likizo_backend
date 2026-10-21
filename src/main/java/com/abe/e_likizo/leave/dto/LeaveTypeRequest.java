
package com.abe.e_likizo.leave.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LeaveTypeRequest(

        @NotBlank(message = "Leave type code is required")
        @Size(max = 50, message = "Code must not exceed 50 characters")
        String code,

        @NotBlank(message = "Leave type name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @NotNull(message = "Default days is required")
        @Min(value = 1, message = "Default days must be at least 1")
        Integer defaultDays,

        boolean requiresDocument
) {
}
