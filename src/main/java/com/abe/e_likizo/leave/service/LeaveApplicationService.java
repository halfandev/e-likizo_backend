
package com.abe.e_likizo.leave.service;

import com.abe.e_likizo.employee.entity.Employee;
import com.abe.e_likizo.employee.repository.EmployeeRepository;
import com.abe.e_likizo.leave.dto.LeaveApplicationRequest;
import com.abe.e_likizo.leave.dto.LeaveApplicationResponse;
import com.abe.e_likizo.leave.entity.LeaveApplication;
import com.abe.e_likizo.leave.entity.LeaveType;
import com.abe.e_likizo.leave.enums.LeaveStatus;
import com.abe.e_likizo.leave.repository.LeaveApplicationRepository;
import com.abe.e_likizo.leave.repository.LeaveTypeRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LeaveApplicationService {

    private final LeaveApplicationRepository leaveApplicationRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final EmployeeRepository employeeRepository;

    public LeaveApplicationService(
            LeaveApplicationRepository leaveApplicationRepository,
            LeaveTypeRepository leaveTypeRepository,
            EmployeeRepository employeeRepository
    ) {
        this.leaveApplicationRepository = leaveApplicationRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.employeeRepository = employeeRepository;
    }

    // Mfanyakazi kuwasilisha ombi jipya la likizo
    public LeaveApplication applyForLeave(
            Long employeeId,
            LeaveApplicationRequest request
    ) {

        // 1. Hakikisha mfanyakazi yupo
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Employee not found with ID: " + employeeId
                        )
                );

        // 2. Hakikisha aina ya likizo ipo na iko active
        LeaveType leaveType = leaveTypeRepository
                .findById(request.leaveTypeId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Leave type not found with ID: "
                                        + request.leaveTypeId()
                        )
                );

        if (!leaveType.isActive()) {
            throw new IllegalArgumentException(
                    "This leave type is not currently active"
            );
        }

        // 3. Hakiki tarehe
        if (request.startDate().isBefore(
                java.time.LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Leave start date cannot be in the past"
            );
        }

        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException(
                    "End date cannot be before start date"
            );
        }

        // 4. Hesabu siku kwa kujumlisha tarehe ya kuanza na kumaliza
        int requestedDays = Math.toIntExact(
                ChronoUnit.DAYS.between(
                        request.startDate(),
                        request.endDate()
                ) + 1
        );

        // 5. Hakiki idadi ya siku dhidi ya default ya aina ya likizo
        if (requestedDays > leaveType.getDefaultDays()) {
            throw new IllegalArgumentException(
                    "Requested days exceed the default allowance of "
                            + leaveType.getDefaultDays() + " days"
            );
        }

        // 6. Tengeneza ombi
        LeaveApplication application = new LeaveApplication();

        application.setEmployee(employee);
        application.setLeaveType(leaveType);
        application.setStartDate(request.startDate());
        application.setEndDate(request.endDate());
        application.setRequestedDays(requestedDays);
        application.setReason(request.reason());
        application.setStatus(LeaveStatus.PENDING);

        // 7. Hifadhi ombi kwenye database
        return leaveApplicationRepository.save(application);
    }

    // Kupata maombi ya mfanyakazi


    @Transactional(readOnly = true)
    public List<LeaveApplicationResponse> getEmployeeApplications(
            Long employeeId
    ) {
        return leaveApplicationRepository.findByEmployeeId(employeeId)
                .stream()
                .map(application -> new LeaveApplicationResponse(
                        application.getId(),
                        application.getEmployee().getId(),
                        application.getLeaveType().getId(),
                        application.getLeaveType().getName(),
                        application.getStartDate(),
                        application.getEndDate(),
                        application.getRequestedDays(),
                        application.getReason(),
                        application.getStatus()
                ))
                .toList();
    }



    // Kupata maombi yanayosubiri kuangaliwa
    @Transactional(readOnly = true)
    public List<LeaveApplication> getPendingApplications() {
        return leaveApplicationRepository.findByStatus(
                LeaveStatus.PENDING
        );
    }

    // Kupata ombi kwa ID
    @Transactional(readOnly = true)
    public LeaveApplication getApplicationById(Long applicationId) {
        return leaveApplicationRepository.findById(applicationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Leave application not found with ID: "
                                        + applicationId
                        )
                );
    }

    // Kupata maombi yote ya likizo kwa ajili ya Admin
    @Transactional(readOnly = true)
    public List<LeaveApplication> getAllApplications() {
        return leaveApplicationRepository.findAll();
    }

    // Admin kuanza kupitia ombi
    public LeaveApplication markUnderReview(Long applicationId) {

        LeaveApplication application = getApplicationById(applicationId);

        validatePendingApplication(application);

        application.setStatus(LeaveStatus.UNDER_REVIEW);

        return leaveApplicationRepository.save(application);
    }

    // Admin kuidhinisha ombi
    public LeaveApplication approveApplication(Long applicationId) {

        LeaveApplication application = getApplicationById(applicationId);

        validatePendingApplication(application);

        application.setStatus(LeaveStatus.APPROVED);

        return leaveApplicationRepository.save(application);
    }

    // Admin kukataa ombi
    public LeaveApplication rejectApplication(Long applicationId) {

        LeaveApplication application = getApplicationById(applicationId);

        validatePendingApplication(application);

        application.setStatus(LeaveStatus.REJECTED);

        return leaveApplicationRepository.save(application);
    }

    // Ruhusu kupitia, ku-approve au ku-reject maombi
// yenye status PENDING au UNDER_REVIEW pekee.
    private void validatePendingApplication(LeaveApplication application) {

        if (application.getStatus() != LeaveStatus.PENDING
                && application.getStatus() != LeaveStatus.UNDER_REVIEW) {

            throw new IllegalStateException(
                    "This application has already been processed. Current status: "
                            + application.getStatus()
            );
        }
    }
}
