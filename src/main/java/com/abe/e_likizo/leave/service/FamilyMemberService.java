
package com.abe.e_likizo.leave.service;

import com.abe.e_likizo.employee.entity.Employee;
import com.abe.e_likizo.employee.repository.EmployeeRepository;
import com.abe.e_likizo.leave.dto.FamilyMemberRequest;
import com.abe.e_likizo.leave.dto.FamilyMemberResponse;
import com.abe.e_likizo.leave.entity.FamilyMember;
import com.abe.e_likizo.leave.repository.FamilyMemberRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FamilyMemberService {

    private final FamilyMemberRepository familyMemberRepository;
    private final EmployeeRepository employeeRepository;

    public FamilyMemberService(
            FamilyMemberRepository familyMemberRepository,
            EmployeeRepository employeeRepository
    ) {
        this.familyMemberRepository = familyMemberRepository;
        this.employeeRepository = employeeRepository;
    }

    // 1. Kusajili mwanafamilia
    public FamilyMemberResponse addFamilyMember(
            Long employeeId,
            FamilyMemberRequest request
    ) {
        Employee employee = findEmployee(employeeId);

        FamilyMember member = new FamilyMember();
        member.setEmployee(employee);
        member.setFullName(request.fullName().trim());
        member.setRelationship(request.relationship());
        member.setDateOfBirth(request.dateOfBirth());
        member.setNotes(request.notes());

        FamilyMember savedMember =
                familyMemberRepository.save(member);

        return mapToResponse(savedMember);
    }

    // 2. Kuona wanafamilia wote wa mfanyakazi
    @Transactional(readOnly = true)
    public List<FamilyMemberResponse> getEmployeeFamilyMembers(
            Long employeeId
    ) {
        findEmployee(employeeId);

        return familyMemberRepository.findByEmployeeId(employeeId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // 3. Kuona mwanafamilia mmoja
    @Transactional(readOnly = true)
    public FamilyMemberResponse getFamilyMemberById(
            Long employeeId,
            Long familyMemberId
    ) {
        return mapToResponse(
                findFamilyMember(employeeId, familyMemberId)
        );
    }

    // 4. Kubadilisha taarifa za mwanafamilia
    public FamilyMemberResponse updateFamilyMember(
            Long employeeId,
            Long familyMemberId,
            FamilyMemberRequest request
    ) {
        FamilyMember member =
                findFamilyMember(employeeId, familyMemberId);

        member.setFullName(request.fullName().trim());
        member.setRelationship(request.relationship());
        member.setDateOfBirth(request.dateOfBirth());
        member.setNotes(request.notes());

        FamilyMember updatedMember =
                familyMemberRepository.save(member);

        return mapToResponse(updatedMember);
    }

    // 5. Kufuta mwanafamilia
    public void deleteFamilyMember(
            Long employeeId,
            Long familyMemberId
    ) {
        FamilyMember member =
                findFamilyMember(employeeId, familyMemberId);

        familyMemberRepository.delete(member);
    }

    // Tafuta mfanyakazi
    private Employee findEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Employee not found: " + employeeId
                        )
                );
    }

    // Hakikisha mwanafamilia ni wa mfanyakazi husika
    private FamilyMember findFamilyMember(
            Long employeeId,
            Long familyMemberId
    ) {
        return familyMemberRepository
                .findByIdAndEmployee_Id(
                        familyMemberId,
                        employeeId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Family member not found for employee: "
                                        + employeeId
                        )
                );
    }

    // Kubadilisha Entity kuwa Response DTO
    private FamilyMemberResponse mapToResponse(
            FamilyMember member
    ) {
        return new FamilyMemberResponse(
                member.getId(),
                member.getEmployee().getId(),
                member.getFullName(),
                member.getRelationship(),
                member.getDateOfBirth(),
                member.getNotes()
        );
    }
}
