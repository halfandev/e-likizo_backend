
package com.abe.e_likizo.leave.controller;

import com.abe.e_likizo.leave.dto.LeaveApplicationRequest;
import com.abe.e_likizo.leave.dto.LeaveApplicationResponse;
import com.abe.e_likizo.leave.entity.LeaveApplication;
import com.abe.e_likizo.leave.service.LeaveApplicationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveApplicationController {

    private final LeaveApplicationService leaveApplicationService;

    // Employee anaomba likizo
    @PostMapping("/apply/{employeeId}")
    public LeaveApplicationResponse applyForLeave(
            @PathVariable Long employeeId,
            @Valid @RequestBody LeaveApplicationRequest request) {

        LeaveApplication application =
                leaveApplicationService.applyForLeave(employeeId, request);

        return toResponse(application);
    }

    // Kuona maombi ya employee

    @GetMapping("/employee/{employeeId}")
    public List<LeaveApplicationResponse> getEmployeeApplications(
            @PathVariable Long employeeId) {

        return leaveApplicationService
                .getEmployeeApplications(employeeId);
    }


    // Kuona maombi yanayosubiri
    @GetMapping("/pending")
    public List<LeaveApplicationResponse> getPendingApplications() {
        return leaveApplicationService.getPendingApplications()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Kuona ombi moja
    @GetMapping("/{applicationId}")
    public LeaveApplicationResponse getApplicationById(
            @PathVariable Long applicationId) {

        LeaveApplication application =
                leaveApplicationService
                        .getApplicationById(applicationId);

        return toResponse(application);
    }

    // Kubadilisha Entity kuwa DTO

    private LeaveApplicationResponse toResponse(
            LeaveApplication application) {

        return new LeaveApplicationResponse(
                application.getId(),
                application.getEmployee().getId(),
                application.getLeaveType().getId(),
                application.getLeaveType().getName(),
                application.getStartDate(),
                application.getEndDate(),
                application.getRequestedDays(),
                application.getReason(),
                application.getStatus()
        );
    }

}
