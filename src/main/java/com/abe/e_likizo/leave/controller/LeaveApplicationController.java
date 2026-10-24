
package com.abe.e_likizo.leave.controller;

import com.abe.e_likizo.employee.service.CurrentEmployeeService;
import com.abe.e_likizo.leave.dto.LeaveApplicationRequest;
import com.abe.e_likizo.leave.dto.LeaveApplicationResponse;
import com.abe.e_likizo.leave.dto.LeaveSummaryResponse;
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
    private final CurrentEmployeeService currentEmployeeService;

    @PostMapping("/apply")
    public LeaveApplicationResponse applyForLeave(
            @Valid @RequestBody LeaveApplicationRequest request) {

        Long employeeId =
                currentEmployeeService.getCurrentEmployeeId();

        return leaveApplicationService.applyForLeave(
                employeeId, request
        );
    }

    @GetMapping("/my-applications")
    public List<LeaveApplicationResponse> getMyApplications() {

        Long employeeId =
                currentEmployeeService.getCurrentEmployeeId();

        return leaveApplicationService.getEmployeeApplications(
                employeeId
        );
    }


    @GetMapping("/my-summary")
    public LeaveSummaryResponse getMyLeaveSummary() {

        Long employeeId =
                currentEmployeeService.getCurrentEmployeeId();

        return leaveApplicationService.getEmployeeLeaveSummary(
                employeeId
        );
    }

}
