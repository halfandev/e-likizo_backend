package com.abe.e_likizo.leave.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkflowActionRequest {

    @NotBlank(message = "Maoni yanahitajika")
    private String comment;
}