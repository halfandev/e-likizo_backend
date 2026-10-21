
package com.abe.e_likizo.leave.controller;

import com.abe.e_likizo.leave.dto.LeaveApplicationResponse;
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
        return leaveApplicationService.getAllApplications();
    }

    // Admin kuona maombi yanayosubiri
    @GetMapping("/pending")
    public List<LeaveApplicationResponse> getPendingApplications() {
        return leaveApplicationService.getPendingApplications();
    }

    // Admin kuona ombi moja
    @GetMapping("/{applicationId}")
    public LeaveApplicationResponse getApplicationById(
            @PathVariable Long applicationId) {
        return leaveApplicationService.getApplicationById(applicationId);
    }

    // Admin kuanza kupitia ombi
    @PatchMapping("/{applicationId}/review")
    public LeaveApplicationResponse markUnderReview(
            @PathVariable Long applicationId) {
        return leaveApplicationService.markUnderReview(applicationId);
    }

    // Admin kuidhinisha ombi
    @PatchMapping("/{applicationId}/approve")
    public LeaveApplicationResponse approveApplication(
            @PathVariable Long applicationId) {
        return leaveApplicationService.approveApplication(applicationId);
    }

    // Admin kukataa ombi
    @PatchMapping("/{applicationId}/reject")
    public LeaveApplicationResponse rejectApplication(
            @PathVariable Long applicationId) {
        return leaveApplicationService.rejectApplication(applicationId);
    }
}
