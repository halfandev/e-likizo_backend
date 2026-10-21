
package com.abe.e_likizo.leave.service;

import com.abe.e_likizo.employee.entity.Employee;
import com.abe.e_likizo.employee.repository.EmployeeRepository;
import com.abe.e_likizo.leave.dto.LeaveApplicationRequest;
import com.abe.e_likizo.leave.dto.LeaveApplicationResponse;
import com.abe.e_likizo.leave.entity.FamilyMember;
import com.abe.e_likizo.leave.entity.LeaveApplication;
import com.abe.e_likizo.leave.entity.LeaveType;
import com.abe.e_likizo.leave.enums.LeaveStatus;
import com.abe.e_likizo.leave.repository.FamilyMemberRepository;
import com.abe.e_likizo.leave.repository.LeaveApplicationRepository;
import com.abe.e_likizo.leave.repository.LeaveTypeRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LeaveApplicationService {

    private final LeaveApplicationRepository leaveApplicationRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final EmployeeRepository employeeRepository;
    private final FamilyMemberRepository familyMemberRepository;

    public LeaveApplicationService(
            LeaveApplicationRepository leaveApplicationRepository,
            LeaveTypeRepository leaveTypeRepository,
            EmployeeRepository employeeRepository,
            FamilyMemberRepository familyMemberRepository
    ) {
        this.leaveApplicationRepository = leaveApplicationRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.employeeRepository = employeeRepository;
        this.familyMemberRepository = familyMemberRepository;
    }

    // 1. Mfanyakazi anaomba likizo
    public LeaveApplicationResponse applyForLeave(
            Long employeeId,
            LeaveApplicationRequest request
    ) {

        // Hakiki mfanyakazi
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Employee not found: " + employeeId
                        )
                );

        // Hakiki aina ya likizo
        LeaveType leaveType = leaveTypeRepository
                .findById(request.leaveTypeId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Leave type not found: "
                                        + request.leaveTypeId()
                        )
                );

        if (!leaveType.isActive()) {
            throw new IllegalArgumentException(
                    "This leave type is currently inactive"
            );
        }

        // Hakiki tarehe
        LocalDate today = LocalDate.now();

        if (request.startDate().isBefore(today)) {
            throw new IllegalArgumentException(
                    "Leave start date cannot be in the past"
            );
        }

        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException(
                    "End date cannot be before start date"
            );
        }

        // Hesabu siku pamoja na siku ya kuanza na kumaliza
        int requestedDays = Math.toIntExact(
                ChronoUnit.DAYS.between(
                        request.startDate(),
                        request.endDate()
                ) + 1
        );

        if (requestedDays > leaveType.getDefaultDays()) {
            throw new IllegalArgumentException(
                    "Requested days (" + requestedDays
                            + ") exceed the allowed days ("
                            + leaveType.getDefaultDays() + ")"
            );
        }

        // Hakiki mwanafamilia kwa annual leave ya siku 28
        boolean annualLeave28Days =
                "ANNUAL".equalsIgnoreCase(leaveType.getCode())
                        && requestedDays == 28;

        FamilyMember familyMember = null;

        if (annualLeave28Days) {

            if (request.familyMemberId() == null) {
                throw new IllegalArgumentException(
                        "Please select a family member for annual leave of 28 days"
                );
            }

            familyMember = familyMemberRepository
                    .findByIdAndEmployee_Id(
                            request.familyMemberId(),
                            employeeId
                    )
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Selected family member does not belong "
                                            + "to this employee"
                            )
                    );

        } else if (request.familyMemberId() != null) {

            throw new IllegalArgumentException(
                    "Family member is only accepted for annual leave of 28 days"
            );
        }

        // Tengeneza ombi la likizo
        LeaveApplication application = new LeaveApplication();

        application.setEmployee(employee);
        application.setLeaveType(leaveType);
        application.setStartDate(request.startDate());
        application.setEndDate(request.endDate());
        application.setRequestedDays(requestedDays);
        application.setReason(request.reason());
        application.setFamilyMember(familyMember);
        application.setStatus(LeaveStatus.PENDING);

        // Hifadhi ombi
        LeaveApplication savedApplication =
                leaveApplicationRepository.save(application);

        return mapToResponse(savedApplication);
    }

    // 2. Pata maombi yote ya mfanyakazi
    @Transactional(readOnly = true)
    public List<LeaveApplicationResponse> getEmployeeApplications(
            Long employeeId
    ) {

        employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Employee not found: " + employeeId
                        )
                );

        return leaveApplicationRepository.findAll()
                .stream()
                .filter(application ->
                        application.getEmployee()
                                .getId()
                                .equals(employeeId)
                )
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 3. Pata maombi yote ya likizo
    @Transactional(readOnly = true)
    public List<LeaveApplicationResponse> getAllApplications() {

        return leaveApplicationRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 4. Pata maombi yanayosubiri
    @Transactional(readOnly = true)
    public List<LeaveApplicationResponse> getPendingApplications() {

        return leaveApplicationRepository.findAll()
                .stream()
                .filter(application ->
                        application.getStatus() == LeaveStatus.PENDING
                )
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 5. Pata ombi kwa ID
    @Transactional(readOnly = true)
    public LeaveApplicationResponse getApplicationById(Long applicationId) {

        LeaveApplication application = findApplication(applicationId);

        return mapToResponse(application);
    }


    // 6. Weka ombi kwenye hatua ya kukaguliwa.
    // Ombi lazima liwe PENDING kabla ya kuanza review.
    public LeaveApplicationResponse markUnderReview(Long applicationId) {

        LeaveApplication application = findApplication(applicationId);

        if (application.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only pending applications can be moved to review"
            );
        }

        application.setStatus(LeaveStatus.UNDER_REVIEW);

        return mapToResponse(
                leaveApplicationRepository.save(application)
        );
    }

    // 7. Idhinisha ombi.
    // Admin anaweza kuidhinisha ombi la PENDING au UNDER_REVIEW.
    public LeaveApplicationResponse approveApplication(Long applicationId) {

        LeaveApplication application = findApplication(applicationId);

        validateApplicationCanBeProcessed(application);

        application.setStatus(LeaveStatus.APPROVED);

        return mapToResponse(
                leaveApplicationRepository.save(application)
        );
    }

    // 8. Kataa ombi.
    // Admin anaweza kukataa ombi la PENDING au UNDER_REVIEW.
    public LeaveApplicationResponse rejectApplication(Long applicationId) {

        LeaveApplication application = findApplication(applicationId);

        validateApplicationCanBeProcessed(application);

        application.setStatus(LeaveStatus.REJECTED);

        return mapToResponse(
                leaveApplicationRepository.save(application)
        );
    }

    // 9. Hakikisha ombi linaweza kuidhinishwa au kukataliwa.
    private void validateApplicationCanBeProcessed(
            LeaveApplication application
    ) {

        LeaveStatus status = application.getStatus();

        if (status != LeaveStatus.PENDING
                && status != LeaveStatus.UNDER_REVIEW) {

            throw new IllegalArgumentException(
                    "Only pending or under-review applications "
                            + "can be approved or rejected"
            );
        }
    }


    // 10. Tafuta ombi kwa ID
    private LeaveApplication findApplication(Long applicationId) {

        return leaveApplicationRepository.findById(applicationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Leave application not found: " + applicationId
                        )
                );
    }

    // 11. Geuza Entity kuwa Response DTO

    private LeaveApplicationResponse mapToResponse(
            LeaveApplication application
    ) {
        return new LeaveApplicationResponse(
                application.getId(),
                application.getEmployee().getId(),
                application.getLeaveType().getId(),
                application.getLeaveType().getName(),
                application.getStartDate(),
                application.getEndDate(),
                application.getRequestedDays(),
                application.getReason(),
                application.getStatus(),
                application.getFamilyMember() != null
                        ? application.getFamilyMember().getId()
                        : null
        );
    }

}
