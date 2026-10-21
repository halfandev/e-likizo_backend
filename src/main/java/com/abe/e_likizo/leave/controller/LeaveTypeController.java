
package com.abe.e_likizo.leave.controller;

import com.abe.e_likizo.leave.dto.LeaveTypeRequest;
import com.abe.e_likizo.leave.dto.LeaveTypeResponse;
import com.abe.e_likizo.leave.entity.LeaveType;
import com.abe.e_likizo.leave.service.LeaveTypeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class LeaveTypeController {

    private final LeaveTypeService leaveTypeService;

    // Employee: kuona aina za likizo zilizo active
    @GetMapping("/api/leaves/types")
    public List<LeaveTypeResponse> getActiveLeaveTypes() {

        return leaveTypeService.getAllActiveLeaveTypes()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Admin: kuona aina zote, pamoja na zilizozimwa
    @GetMapping("/api/admin/leave-types")
    public List<LeaveTypeResponse> getAllLeaveTypes() {

        return leaveTypeService.getAllLeaveTypes()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Admin: kuona aina moja
    @GetMapping("/api/admin/leave-types/{id}")
    public LeaveTypeResponse getLeaveTypeById(
            @PathVariable Long id) {

        return toResponse(
                leaveTypeService.getLeaveTypeById(id)
        );
    }

    // Admin: kuongeza aina mpya
    @PostMapping("/api/admin/leave-types")
    @ResponseStatus(HttpStatus.CREATED)
    public LeaveTypeResponse createLeaveType(
            @Valid @RequestBody LeaveTypeRequest request) {

        return toResponse(
                leaveTypeService.createLeaveType(request)
        );
    }

    // Admin: kusasisha aina iliyopo
    @PutMapping("/api/admin/leave-types/{id}")
    public LeaveTypeResponse updateLeaveType(
            @PathVariable Long id,
            @Valid @RequestBody LeaveTypeRequest request) {

        return toResponse(
                leaveTypeService.updateLeaveType(id, request)
        );
    }

    // Admin: kuzima aina ya likizo
    @PatchMapping("/api/admin/leave-types/{id}/deactivate")
    public LeaveTypeResponse deactivateLeaveType(
            @PathVariable Long id) {

        return toResponse(
                leaveTypeService.deactivateLeaveType(id)
        );
    }

    // Admin: kuwasha aina ya likizo
    @PatchMapping("/api/admin/leave-types/{id}/activate")
    public LeaveTypeResponse activateLeaveType(
            @PathVariable Long id) {

        return toResponse(
                leaveTypeService.activateLeaveType(id)
        );
    }

    // Kubadilisha Entity kuwa DTO
    private LeaveTypeResponse toResponse(LeaveType leaveType) {

        return new LeaveTypeResponse(
                leaveType.getId(),
                leaveType.getCode(),
                leaveType.getName(),
                leaveType.getDescription(),
                leaveType.getDefaultDays(),
                leaveType.isRequiresDocument(),
                leaveType.isActive()
        );
    }
}
