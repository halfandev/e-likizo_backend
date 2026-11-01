package com.abe.e_likizo.leave.controller;

import com.abe.e_likizo.employee.service.CurrentEmployeeService;
import com.abe.e_likizo.leave.dto.*;
import com.abe.e_likizo.leave.service.LeaveApplicationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.abe.e_likizo.leave.service.LeaveApprovalPdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;
import java.nio.charset.StandardCharsets;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveApplicationController {

    private final LeaveApplicationService leaveApplicationService;
    private final CurrentEmployeeService currentEmployeeService;
    private final LeaveApprovalPdfService leaveApprovalPdfService;

    // Employee anaomba likizo
    @PostMapping("/apply")
    public LeaveApplicationResponse applyForLeave(
            @Valid @RequestBody LeaveApplicationRequest request) {

        Long employeeId =
                currentEmployeeService.getCurrentEmployeeId();

        return leaveApplicationService.applyForLeave(
                employeeId, request
        );
    }

    // Employee anaona maombi yake
    @GetMapping("/my-applications")
    public List<LeaveApplicationResponse> getMyApplications() {

        Long employeeId =
                currentEmployeeService.getCurrentEmployeeId();

        return leaveApplicationService.getEmployeeApplications(
                employeeId
        );
    }

    // Employee anaona summary yake
    @GetMapping("/my-summary")
    public LeaveSummaryResponse getMyLeaveSummary() {

        Long employeeId =
                currentEmployeeService.getCurrentEmployeeId();

        return leaveApplicationService.getEmployeeLeaveSummary(
                employeeId
        );
    }

    // ==========================================
    // OCS ENDPOINTS
    // ==========================================

    // OCS anaona maombi ya kituo chake
    @GetMapping("/ocs/inbox")
    public List<LeaveApplicationResponse> getOCSApplications() {

        return leaveApplicationService.getOCSApplications();
    }

    // OCS anapeleka ombi kwa OCD
    @PostMapping("/ocs/{applicationId}/forward")
    public LeaveApplicationResponse forwardFromOCSToOCD(
            @PathVariable Long applicationId,
            @Valid @RequestBody WorkflowActionRequest request) {

        return leaveApplicationService.forwardFromOCSToOCD(
                applicationId,
                request.getComment()
        );
    }

    // OCS anakataa ombi
    @PostMapping("/ocs/{applicationId}/reject")
    public LeaveApplicationResponse rejectOCSApplication(
            @PathVariable Long applicationId,
            @Valid @RequestBody WorkflowActionRequest request) {

        return leaveApplicationService.rejectFromOCS(
                applicationId,
                request.getComment()
        );
    }

    @GetMapping("/ocs/forwarded")
    public List<LeaveApplicationResponse> getOCSForwardedApplications() {
        return leaveApplicationService.getOCSForwardedApplications();
    }


    // ==========================================
// OCD ENDPOINTS
// ==========================================

    // OCD anaona maombi yaliyotumwa na OCS
    @GetMapping("/ocd/inbox")
    public List<LeaveApplicationResponse> getOCDApplications() {

        return leaveApplicationService.getOCDApplications();
    }

    // OCD dashboard counts
    @GetMapping("/ocd/dashboard-counts")
    public LeaveDashboardCountsResponse getOCDDashboardCounts() {

        return leaveApplicationService.getOCDDashboardCounts();
    }

    // OCD anapeleka ombi kwa RPC
    @PostMapping("/ocd/{applicationId}/forward")
    public LeaveApplicationResponse forwardFromOCDToRPC(
            @PathVariable Long applicationId,
            @Valid @RequestBody WorkflowActionRequest request) {

        return leaveApplicationService.forwardFromOCDToRPC(
                applicationId,
                request.getComment()
        );
    }

    // OCD anakataa ombi
    @PostMapping("/ocd/{applicationId}/reject")
    public LeaveApplicationResponse rejectFromOCD(
            @PathVariable Long applicationId,
            @Valid @RequestBody WorkflowActionRequest request) {

        return leaveApplicationService.rejectFromOCD(
                applicationId,
                request.getComment()
        );
    }

    // ==========================================
// RPC ENDPOINTS
// ==========================================

    // RPC anaona maombi yaliyofika katika Region yake
    @GetMapping("/rpc/inbox")
    public List<LeaveApplicationResponse> getRPCApplications() {

        return leaveApplicationService.getRPCApplications();
    }


    // RPC anaidhinisha au kupeleka ombi hatua inayofuata
    @PostMapping("/rpc/{applicationId}/approve")
    public LeaveApplicationResponse approveAtRPC(
            @PathVariable Long applicationId,
            @Valid @RequestBody WorkflowActionRequest request) {

        return leaveApplicationService.processAtRPC(
                applicationId,
                request.getComment()
        );
    }


    // RPC anakataa ombi
    @PostMapping("/rpc/{applicationId}/reject")
    public LeaveApplicationResponse rejectAtRPC(
            @PathVariable Long applicationId,
            @Valid @RequestBody WorkflowActionRequest request) {

        return leaveApplicationService.rejectAtRPC(
                applicationId,
                request.getComment()
        );
    }


    // RPC anaona maombi yaliyokamilika
    @GetMapping("/rpc/approved")
    public List<LeaveApplicationResponse> getRPCApprovedApplications() {

        return leaveApplicationService.getRPCApprovedApplications();
    }


    // RPC anaona maombi aliyoyakataa
    @GetMapping("/rpc/rejected")
    public List<LeaveApplicationResponse> getRPCRejectedApplications() {

        return leaveApplicationService.getRPCRejectedApplications();
    }

    @GetMapping("/my-applications/{applicationId}/workflow")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<LeaveWorkflowTrackingResponse> getMyWorkflowHistory(
            @PathVariable Long applicationId
    ) {

        return ResponseEntity.ok(
                leaveApplicationService.getMyWorkflowHistory(applicationId)
        );
    }

    // Employee anapakua barua ya kibali cha likizo
    @GetMapping("/my-applications/{applicationId}/approval-letter")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<byte[]> downloadApprovalLetter(
            @PathVariable Long applicationId
    ) {

        Long employeeId =
                currentEmployeeService.getCurrentEmployeeId();

        byte[] pdf =
                leaveApprovalPdfService.generateApprovalLetter(
                        applicationId,
                        employeeId
                );

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_PDF);

        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(
                                "Leave-Approval-" + applicationId + ".pdf",
                                StandardCharsets.UTF_8
                        )
                        .build()
        );

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(pdf);
    }

    @GetMapping("/summary/{employeeId}")
    public ResponseEntity<LeaveSummaryResponse> getEmployeeLeaveSummary(
            @PathVariable Long employeeId
    ) {
        return ResponseEntity.ok(
                leaveApplicationService.getEmployeeLeaveSummary(employeeId)
        );
    }

    // OCS Dashboard Counts
    @GetMapping("/ocs/dashboard-counts")
    public ResponseEntity<OcsDashboardCountsResponse> getOCSDashboardCounts() {

        return ResponseEntity.ok(
                leaveApplicationService.getOCSDashboardCounts()
        );
    }
}