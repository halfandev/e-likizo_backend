
package com.abe.e_likizo.leave.controller;

import com.abe.e_likizo.leave.dto.LeaveApplicationResponse;
import com.abe.e_likizo.leave.entity.LeaveApplication;
import com.abe.e_likizo.leave.service.LeaveApplicationService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/leaves")
@RequiredArgsConstructor
public class AdminLeaveApplicationController {

    private final LeaveApplicationService leaveApplicationService;

    // Admin kuona maombi yote ya likizo
    @GetMapping
    public List<LeaveApplicationResponse> getAllApplications() {

        return leaveApplicationService.getAllApplications()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Admin kuona maombi yanayosubiri
    @GetMapping("/pending")
    public List<LeaveApplicationResponse> getPendingApplications() {

        return leaveApplicationService.getPendingApplications()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Admin kuona ombi moja
    @GetMapping("/{applicationId}")
    public LeaveApplicationResponse getApplicationById(
            @PathVariable Long applicationId) {

        return toResponse(
                leaveApplicationService.getApplicationById(applicationId)
        );
    }

    // Admin kuanza kupitia ombi
    @PatchMapping("/{applicationId}/review")
    public LeaveApplicationResponse markUnderReview(
            @PathVariable Long applicationId) {

        return toResponse(
                leaveApplicationService.markUnderReview(applicationId)
        );
    }

    // Admin kuidhinisha ombi
    @PatchMapping("/{applicationId}/approve")
    public LeaveApplicationResponse approveApplication(
            @PathVariable Long applicationId) {

        return toResponse(
                leaveApplicationService.approveApplication(applicationId)
        );
    }

    // Admin kukataa ombi
    @PatchMapping("/{applicationId}/reject")
    public LeaveApplicationResponse rejectApplication(
            @PathVariable Long applicationId) {

        return toResponse(
                leaveApplicationService.rejectApplication(applicationId)
        );
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
