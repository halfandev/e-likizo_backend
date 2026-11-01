
package com.abe.e_likizo.leave.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LeaveWorkflowActionRequest(

        @NotBlank(message = "Maoni yanahitajika")
        @Size(max = 1000, message = "Maoni yasizidi herufi 1000")
        String comment

) {}
