
package com.abe.e_likizo.leave.controller;

import com.abe.e_likizo.employee.service.CurrentEmployeeService;
import com.abe.e_likizo.leave.dto.FamilyMemberRequest;
import com.abe.e_likizo.leave.dto.FamilyMemberResponse;
import com.abe.e_likizo.leave.service.FamilyMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees/me/family-members")
@RequiredArgsConstructor
public class FamilyMemberController {

    private final FamilyMemberService familyMemberService;
    private final CurrentEmployeeService currentEmployeeService;

    @PostMapping
    public FamilyMemberResponse addFamilyMember(
            @Valid @RequestBody FamilyMemberRequest request) {

        Long employeeId =
                currentEmployeeService.getCurrentEmployeeId();

        return familyMemberService.addFamilyMember(
                employeeId, request
        );
    }

    @GetMapping
    public List<FamilyMemberResponse> getFamilyMembers() {

        Long employeeId =
                currentEmployeeService.getCurrentEmployeeId();

        return familyMemberService.getEmployeeFamilyMembers(
                employeeId
        );
    }

    @GetMapping("/{familyMemberId}")
    public FamilyMemberResponse getFamilyMember(
            @PathVariable Long familyMemberId) {

        Long employeeId =
                currentEmployeeService.getCurrentEmployeeId();

        return familyMemberService.getFamilyMemberById(
                employeeId, familyMemberId
        );
    }

    @PutMapping("/{familyMemberId}")
    public FamilyMemberResponse updateFamilyMember(
            @PathVariable Long familyMemberId,
            @Valid @RequestBody FamilyMemberRequest request) {

        Long employeeId =
                currentEmployeeService.getCurrentEmployeeId();

        return familyMemberService.updateFamilyMember(
                employeeId, familyMemberId, request
        );
    }

    @DeleteMapping("/{familyMemberId}")
    public void deleteFamilyMember(
            @PathVariable Long familyMemberId) {

        Long employeeId =
                currentEmployeeService.getCurrentEmployeeId();

        familyMemberService.deleteFamilyMember(
                employeeId, familyMemberId
        );
    }
}
