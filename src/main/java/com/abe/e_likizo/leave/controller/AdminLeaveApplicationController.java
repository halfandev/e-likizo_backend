
package com.abe.e_likizo.leave.controller;

import com.abe.e_likizo.leave.dto.LeaveApplicationResponse;
import com.abe.e_likizo.leave.dto.LeaveWorkflowActionRequest;
import com.abe.e_likizo.leave.service.LeaveApplicationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/leaves")
@RequiredArgsConstructor
public class AdminLeaveApplicationController {

    private final LeaveApplicationService leaveApplicationService;

    @GetMapping
    public List<LeaveApplicationResponse> getAllApplications() {
        return leaveApplicationService.getAllApplications();
    }

    @GetMapping("/pending")
    public List<LeaveApplicationResponse> getPendingApplications() {
        return leaveApplicationService.getPendingApplications();
    }

    @GetMapping("/{applicationId}")
    public LeaveApplicationResponse getApplicationById(
            @PathVariable Long applicationId
    ) {
        return leaveApplicationService.getApplicationById(applicationId);
    }

    // OCS -> OCD
    @PatchMapping("/{applicationId}/forward-ocs")
    public LeaveApplicationResponse forwardFromOCSToOCD(
            @PathVariable Long applicationId,
            @Valid @RequestBody LeaveWorkflowActionRequest request
    ) {
        return leaveApplicationService.forwardFromOCSToOCD(
                applicationId,
                request.comment()
        );
    }

    // OCD -> RPC
    @PatchMapping("/{applicationId}/forward-ocd")
    public LeaveApplicationResponse forwardFromOCDToRPC(
            @PathVariable Long applicationId,
            @Valid @RequestBody LeaveWorkflowActionRequest request
    ) {
        return leaveApplicationService.forwardFromOCDToRPC(
                applicationId,
                request.comment()
        );
    }

    // RPC -> CP au COMPLETED
    @PatchMapping("/{applicationId}/process-rpc")
    public LeaveApplicationResponse processAtRPC(
            @PathVariable Long applicationId,
            @Valid @RequestBody LeaveWorkflowActionRequest request
    ) {
        return leaveApplicationService.processAtRPC(
                applicationId,
                request.comment()
        );
    }

    // CP -> IGP
    @PatchMapping("/{applicationId}/forward-cp")
    public LeaveApplicationResponse forwardFromCPToIGP(
            @PathVariable Long applicationId,
            @Valid @RequestBody LeaveWorkflowActionRequest request
    ) {
        return leaveApplicationService.forwardFromCPToIGP(
                applicationId,
                request.comment()
        );
    }

    // IGP -> COMPLETED
    @PatchMapping("/{applicationId}/approve-igp")
    public LeaveApplicationResponse approveAtIGP(
            @PathVariable Long applicationId,
            @Valid @RequestBody LeaveWorkflowActionRequest request
    ) {
        return leaveApplicationService.approveAtIGP(
                applicationId,
                request.comment()
        );
    }

    // REJECT
    @PatchMapping("/{applicationId}/reject")
    public LeaveApplicationResponse rejectApplication(
            @PathVariable Long applicationId,
            @Valid @RequestBody LeaveWorkflowActionRequest request
    ) {
        return leaveApplicationService.rejectApplication(
                applicationId,
                request.comment()
        );
    }
}
