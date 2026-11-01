package com.abe.e_likizo.leave.service;
import com.abe.e_likizo.leave.dto.LeaveDashboardCountsResponse;
import org.springframework.security.access.AccessDeniedException;
import com.abe.e_likizo.leave.dto.LeaveWorkflowHistoryResponse;
import com.abe.e_likizo.leave.dto.LeaveWorkflowTrackingResponse;
import com.abe.e_likizo.employee.entity.Employee;
import com.abe.e_likizo.employee.repository.EmployeeRepository;
import com.abe.e_likizo.leave.dto.LeaveApplicationRequest;
import com.abe.e_likizo.leave.dto.LeaveApplicationResponse;
import com.abe.e_likizo.leave.dto.LeaveSummaryResponse;
import com.abe.e_likizo.leave.entity.FamilyMember;
import com.abe.e_likizo.leave.entity.LeaveApplication;
import com.abe.e_likizo.leave.entity.LeaveType;
import com.abe.e_likizo.leave.entity.LeaveWorkflowHistory;
import com.abe.e_likizo.leave.enums.LeaveStatus;
import com.abe.e_likizo.leave.repository.FamilyMemberRepository;
import com.abe.e_likizo.leave.repository.LeaveApplicationRepository;
import com.abe.e_likizo.leave.repository.LeaveTypeRepository;

import com.abe.e_likizo.leave.repository.LeaveWorkflowHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.LocalDateTime;
import com.abe.e_likizo.entity.User;
import com.abe.e_likizo.entity.Role;
import com.abe.e_likizo.repository.UserRepository;
import java.util.Objects;
@Service
@Transactional
public class LeaveApplicationService {

    private final LeaveApplicationRepository leaveApplicationRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final EmployeeRepository employeeRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final LeaveWorkflowHistoryRepository historyRepository;
    private final UserRepository userRepository;
    public LeaveApplicationService(
            LeaveApplicationRepository leaveApplicationRepository,
            LeaveTypeRepository leaveTypeRepository,
            EmployeeRepository employeeRepository,
            FamilyMemberRepository familyMemberRepository,
            LeaveWorkflowHistoryRepository historyRepository,
            UserRepository userRepository
    ) {
        this.leaveApplicationRepository = leaveApplicationRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.employeeRepository = employeeRepository;
        this.familyMemberRepository = familyMemberRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
    }

    // 1. Employee anaomba likizo
    public LeaveApplicationResponse applyForLeave(
            Long employeeId,
            LeaveApplicationRequest request
    ) {

        // Hakiki employee
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Employee not found: " + employeeId
                        )
                );

        // Hakiki leave type
        LeaveType leaveType = leaveTypeRepository
                .findById(request.leaveTypeId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Leave type not found: " + request.leaveTypeId()
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

        // Hesabu siku za likizo
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

        // Tengeneza ombi jipya
        LeaveApplication application = new LeaveApplication();

        application.setEmployee(employee);
        application.setLeaveType(leaveType);
        application.setStartDate(request.startDate());
        application.setEndDate(request.endDate());
        application.setRequestedDays(requestedDays);
        application.setReason(request.reason());
        application.setFamilyMember(familyMember);

        // Kila ombi jipya linaanzia kwa OCS
        // Kila ombi jipya linaanzia kwa OCS
        application.setStatus(LeaveStatus.OCS);

// Hifadhi ombi kwanza ili lipate ID
        LeaveApplication savedApplication =
                leaveApplicationRepository.save(application);

// Hifadhi historia ya hatua ya kwanza ya Employee
        saveWorkflowHistory(
                savedApplication,
                null,
                LeaveStatus.OCS,
                "SUBMITTED",
                "Employee submitted leave application"
        );

        return mapToResponse(savedApplication);
    }

    // 2. Employee anaona maombi yake
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

        return leaveApplicationRepository.findByEmployeeId(employeeId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 3. Pata maombi yote
    @Transactional(readOnly = true)
    public List<LeaveApplicationResponse> getAllApplications() {

        return leaveApplicationRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 4. Pata maombi yote ambayo bado yapo kwenye workflow
    @Transactional(readOnly = true)
    public List<LeaveApplicationResponse> getPendingApplications() {

        List<LeaveStatus> activeStatuses = List.of(
                LeaveStatus.OCS,
                LeaveStatus.OCD,
                LeaveStatus.RPC,
                LeaveStatus.CP,
                LeaveStatus.IGP
        );

        return leaveApplicationRepository
                .findByStatusIn(activeStatuses)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // OCS anaona maombi ya Station yake pekee
    @Transactional(readOnly = true)
    public List<LeaveApplicationResponse> getOCSApplications() {

        User user = getCurrentUser();

        if (user.getRole() != Role.OCS) {
            throw new IllegalStateException(
                    "Endpoint hii inaruhusiwa kwa OCS pekee"
            );
        }

        if (user.getStation() == null) {
            throw new IllegalStateException(
                    "OCS huyu hajapangiwa Station"
            );
        }

        return leaveApplicationRepository
                .findByEmployee_Station_IdAndStatus(
                        user.getStation().getId(),
                        LeaveStatus.OCS
                )
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 5. Pata ombi kwa ID
    @Transactional(readOnly = true)
    public LeaveApplicationResponse getApplicationById(
            Long applicationId
    ) {

        LeaveApplication application = findApplication(applicationId);

        return mapToResponse(application);
    }


    // 1. OCS anapeleka ombi kwa OCD
    public LeaveApplicationResponse forwardFromOCSToOCD(
            Long applicationId,
            String comment
    ) {

        LeaveApplication application = findApplication(applicationId);

        validateCurrentStatus(application, LeaveStatus.OCS);

        // Hakiki kama ni OCS wa Station ya mwombaji
        validateAuthority(application, Role.OCS);

        LeaveStatus previousStatus = application.getStatus();
        LeaveStatus newStatus = LeaveStatus.OCD;

        saveWorkflowHistory(
                application,
                previousStatus,
                newStatus,
                "FORWARDED_BY_OCS",
                comment
        );

        application.setStatus(newStatus);

        return mapToResponse(leaveApplicationRepository.save(application));
    }


    // 2. OCD anapeleka ombi kwa RPC
// OCD anapeleka ombi kwa RPC
    public LeaveApplicationResponse forwardFromOCDToRPC(
            Long applicationId,
            String comment
    ) {

        LeaveApplication application = findApplication(applicationId);

        validateCurrentStatus(application, LeaveStatus.OCD);

        // Hakikisha OCD ana mamlaka katika District ya mwombaji
        validateAuthority(application, Role.OCD);

        LeaveStatus previousStatus = application.getStatus();
        LeaveStatus newStatus = LeaveStatus.RPC;

        saveWorkflowHistory(
                application,
                previousStatus,
                newStatus,
                "FORWARDED_BY_OCD",
                comment
        );

        application.setStatus(newStatus);

        return mapToResponse(
                leaveApplicationRepository.save(application)
        );
    }


    // 3. RPC anafanya uamuzi kulingana na cheo cha employee
    // RPC anafanya uamuzi kulingana na cheo cha employee
    public LeaveApplicationResponse processAtRPC(
            Long applicationId,
            String comment
    ) {
        LeaveApplication application = findApplication(applicationId);

        validateCurrentStatus(application, LeaveStatus.RPC);
        validateAuthority(application, Role.RPC);

        Employee employee = application.getEmployee();

        String rank = employee.getRank().name();

        List<String> rpcFinalRanks = List.of(
                "PC",
                "CPL",
                "SGT",
                "SSGT",
                "SM"
        );

        String administrativeCode =
                employee.getAdministrative().getCode();

        LeaveStatus previousStatus = application.getStatus();
        LeaveStatus newStatus;
        String action;

        if (rpcFinalRanks.contains(rank)) {

            newStatus = LeaveStatus.COMPLETED;
            action = "APPROVED_BY_RPC";

        } else if ("TPF".equalsIgnoreCase(administrativeCode)) {

            newStatus = LeaveStatus.IGP;
            action = "FORWARDED_BY_RPC_TO_IGP";

        } else if ("ZPF".equalsIgnoreCase(administrativeCode)) {

            newStatus = LeaveStatus.CP;
            action = "FORWARDED_BY_RPC_TO_CP";

        } else {
            throw new IllegalStateException(
                    "Administrative code haijatambulika: "
                            + administrativeCode
            );
        }

        saveWorkflowHistory(
                application,
                previousStatus,
                newStatus,
                action,
                comment
        );

        application.setStatus(newStatus);

        return mapToResponse(
                leaveApplicationRepository.save(application)
        );
    }

    // RPC anakataa ombi
    public LeaveApplicationResponse rejectAtRPC(
            Long applicationId,
            String comment
    ) {

        LeaveApplication application = findApplication(applicationId);

        validateCurrentStatus(application, LeaveStatus.RPC);

        validateAuthority(application, Role.RPC);

        LeaveStatus previousStatus = application.getStatus();
        LeaveStatus newStatus = LeaveStatus.REJECTED;

        saveWorkflowHistory(
                application,
                previousStatus,
                newStatus,
                "REJECTED_BY_RPC",
                comment
        );

        application.setStatus(newStatus);

        return mapToResponse(
                leaveApplicationRepository.save(application)
        );
    }// RPC anaona maombi yaliyokamilika katika Region yake
    @Transactional(readOnly = true)
    public List<LeaveApplicationResponse> getRPCApprovedApplications() {

        User user = getCurrentUser();

        if (user.getRole() != Role.RPC || user.getRegion() == null) {
            throw new AccessDeniedException(
                    "Huna ruhusa ya kuona taarifa hizi"
            );
        }

        return leaveApplicationRepository
                .findByEmployee_Region_IdAndStatus(
                        user.getRegion().getId(),
                        LeaveStatus.COMPLETED
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // RPC anaona maombi aliyoyakataa katika Region yake
    @Transactional(readOnly = true)
    public List<LeaveApplicationResponse> getRPCRejectedApplications() {

        User user = getCurrentUser();

        if (user.getRole() != Role.RPC || user.getRegion() == null) {
            throw new AccessDeniedException(
                    "Huna ruhusa ya kuona taarifa hizi"
            );
        }

        return leaveApplicationRepository
                .findByEmployee_Region_IdAndStatus(
                        user.getRegion().getId(),
                        LeaveStatus.REJECTED
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // OCS anakataa ombi kutoka Station yake
    public LeaveApplicationResponse rejectFromOCS(
            Long applicationId,
            String comment
    ) {

        LeaveApplication application = findApplication(applicationId);

        validateCurrentStatus(application, LeaveStatus.OCS);

        validateAuthority(application, Role.OCS);

        LeaveStatus previousStatus = application.getStatus();

        application.setStatus(LeaveStatus.REJECTED);

        saveWorkflowHistory(
                application,
                previousStatus,
                LeaveStatus.REJECTED,
                "REJECTED_BY_OCS",
                comment
        );

        return mapToResponse(
                leaveApplicationRepository.save(application)
        );
    }


    // 4. CP anapeleka ombi kwa IGP
    public LeaveApplicationResponse forwardFromCPToIGP(
            Long applicationId,
            String comment
    ) {
        LeaveApplication application = findApplication(applicationId);

        validateCurrentStatus(application, LeaveStatus.CP);

        LeaveStatus previousStatus = application.getStatus();
        LeaveStatus newStatus = LeaveStatus.IGP;

        saveWorkflowHistory(
                application,
                previousStatus,
                newStatus,
                "FORWARDED_BY_CP",
                comment
        );

        application.setStatus(newStatus);

        return mapToResponse(leaveApplicationRepository.save(application));
    }


    // 5. IGP anatoa kibali cha mwisho
    public LeaveApplicationResponse approveAtIGP(
            Long applicationId,
            String comment
    ) {
        LeaveApplication application = findApplication(applicationId);

        validateCurrentStatus(application, LeaveStatus.IGP);

        LeaveStatus previousStatus = application.getStatus();
        LeaveStatus newStatus = LeaveStatus.COMPLETED;

        saveWorkflowHistory(
                application,
                previousStatus,
                newStatus,
                "APPROVED_BY_IGP",
                comment
        );

        application.setStatus(newStatus);

        return mapToResponse(leaveApplicationRepository.save(application));
    }


    // 6. Kiongozi anakataa ombi
    public LeaveApplicationResponse rejectApplication(
            Long applicationId,
            String comment
    ) {
        LeaveApplication application = findApplication(applicationId);

        if (application.getStatus() == LeaveStatus.COMPLETED
                || application.getStatus() == LeaveStatus.REJECTED
                || application.getStatus() == LeaveStatus.CANCELLED) {

            throw new IllegalArgumentException(
                    "This application cannot be rejected"
            );
        }

        LeaveStatus previousStatus = application.getStatus();
        LeaveStatus newStatus = LeaveStatus.REJECTED;

        saveWorkflowHistory(
                application,
                previousStatus,
                newStatus,
                "REJECTED",
                comment
        );

        application.setStatus(newStatus);

        return mapToResponse(leaveApplicationRepository.save(application));
    }




    // Hifadhi historia ya kila hatua ya workflow
    private void saveWorkflowHistory(
            LeaveApplication application,
            LeaveStatus previousStatus,
            LeaveStatus newStatus,
            String action,
            String comment
    ) {

        if (comment == null || comment.isBlank()) {
            throw new IllegalArgumentException(
                    "Maoni yanahitajika kabla ya kuendelea"
            );
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().equals("anonymousUser")) {

            throw new IllegalStateException(
                    "Mtumiaji lazima awe ameingia kwenye mfumo"
            );
        }

        LeaveWorkflowHistory history = new LeaveWorkflowHistory();

        history.setLeaveApplication(application);
        history.setActorEmail(authentication.getName());
        history.setAction(action);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setComment(comment.trim());
        history.setActionAt(LocalDateTime.now());

        historyRepository.save(history);
    }


    // 12. Hakiki status ya sasa kabla ya kuendelea
    private void validateCurrentStatus(
            LeaveApplication application,
            LeaveStatus expectedStatus
    ) {

        if (application.getStatus() != expectedStatus) {
            throw new IllegalArgumentException(
                    "Invalid workflow step. Current status is "
                            + application.getStatus()
                            + " but expected status is "
                            + expectedStatus
            );
        }
    }

    // 13. Tafuta ombi kwa ID
    private LeaveApplication findApplication(Long applicationId) {

        return leaveApplicationRepository.findById(applicationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Leave application not found: " + applicationId
                        )
                );
    }

    // 14. Geuza Entity kuwa Response DTO
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

    // 15. Employee leave summary
    @Transactional(readOnly = true)
    public LeaveSummaryResponse getEmployeeLeaveSummary(
            Long employeeId
    ) {

        employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Employee not found: " + employeeId
                        )
                );

        // Maombi yaliyokamilika
        long approved =
                leaveApplicationRepository
                        .countByEmployee_IdAndStatus(
                                employeeId,
                                LeaveStatus.COMPLETED
                        );

        // Maombi yaliyokataliwa
        long rejected =
                leaveApplicationRepository
                        .countByEmployee_IdAndStatus(
                                employeeId,
                                LeaveStatus.REJECTED
                        );

        // Maombi yanayoendelea
        long pending =
                leaveApplicationRepository
                        .countByEmployee_IdAndStatus(
                                employeeId, LeaveStatus.OCS
                        )
                        + leaveApplicationRepository
                        .countByEmployee_IdAndStatus(
                                employeeId, LeaveStatus.OCD
                        )
                        + leaveApplicationRepository
                        .countByEmployee_IdAndStatus(
                                employeeId, LeaveStatus.RPC
                        )
                        + leaveApplicationRepository
                        .countByEmployee_IdAndStatus(
                                employeeId, LeaveStatus.CP
                        )
                        + leaveApplicationRepository
                        .countByEmployee_IdAndStatus(
                                employeeId, LeaveStatus.IGP
                        );

        return new LeaveSummaryResponse(
                approved,
                rejected,
                pending
        );
    }


    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().equals("anonymousUser")) {

            throw new IllegalStateException(
                    "Mtumiaji lazima awe ameingia kwenye mfumo"
            );
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "User account haijapatikana"
                        )
                );
    }

    private void validateAuthority(
            LeaveApplication application,
            Role requiredRole
    ) {

        User user = getCurrentUser();

        if (user.getRole() != requiredRole) {
            throw new IllegalStateException(
                    "Huna mamlaka ya kufanya hatua hii"
            );
        }

        Employee employee = application.getEmployee();

        switch (requiredRole) {

            case OCS:

                if (user.getStation() == null
                        || employee.getStation() == null
                        || !Objects.equals(
                        user.getStation().getId(),
                        employee.getStation().getId()
                )) {

                    throw new IllegalStateException(
                            "Huwezi kushughulikia ombi la Station nyingine"
                    );
                }
                break;

            case OCD:

                if (user.getDistrict() == null
                        || employee.getDistrict() == null
                        || !Objects.equals(
                        user.getDistrict().getId(),
                        employee.getDistrict().getId()
                )) {

                    throw new IllegalStateException(
                            "Huwezi kushughulikia ombi la District nyingine"
                    );
                }
                break;

            case RPC:

                if (user.getRegion() == null
                        || employee.getRegion() == null
                        || !Objects.equals(
                        user.getRegion().getId(),
                        employee.getRegion().getId()
                )) {

                    throw new IllegalStateException(
                            "Huwezi kushughulikia ombi la Region nyingine"
                    );
                }
                break;

            case CP:

                validateAdministrative(user, employee, "ZPF");
                break;

            case IGP:

                if (user.getAdministrative() == null
                        || employee.getAdministrative() == null) {

                    throw new IllegalStateException(
                            "Administrative haijapatikana"
                    );
                }
                break;

            default:

                throw new IllegalStateException(
                        "Role hii hairuhusiwi kwenye workflow"
                );
        }
    }

    private void validateAdministrative(
            User user,
            Employee employee,
            String requiredCode
    ) {

        if (user.getAdministrative() == null
                || employee.getAdministrative() == null) {

            throw new IllegalStateException(
                    "Administrative haijapatikana"
            );
        }

        String userCode = user.getAdministrative().getCode();

        String employeeCode =
                employee.getAdministrative().getCode();

        if (!requiredCode.equalsIgnoreCase(userCode)
                || !requiredCode.equalsIgnoreCase(employeeCode)) {

            throw new IllegalStateException(
                    "Huna mamlaka ya kushughulikia ombi hili"
            );
        }
    }
    public List<LeaveApplicationResponse> getOCSForwardedApplications() {

        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.OCS) {
            throw new AccessDeniedException(
                    "Huna ruhusa ya kuona maombi haya."
            );
        }

        if (currentUser.getStation() == null) {
            throw new IllegalStateException(
                    "OCS huyu hajapangiwa kituo."
            );
        }

        return leaveApplicationRepository
                .findByEmployee_Station_IdAndStatus(
                        currentUser.getStation().getId(),
                        LeaveStatus.OCD
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // OCD anaona maombi yaliyotumwa na OCS katika District yake
    @Transactional(readOnly = true)
    public List<LeaveApplicationResponse> getOCDApplications() {

        User user = getCurrentUser();

        if (user.getRole() != Role.OCD) {
            throw new AccessDeniedException(
                    "Endpoint hii inaruhusiwa kwa OCD pekee"
            );
        }

        if (user.getDistrict() == null) {
            throw new IllegalStateException(
                    "OCD huyu hajapangiwa District"
            );
        }

        return leaveApplicationRepository
                .findByEmployee_District_IdAndStatus(
                        user.getDistrict().getId(),
                        LeaveStatus.OCD
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // OCD anakataa ombi lililopo kwenye inbox yake
    public LeaveApplicationResponse rejectFromOCD(
            Long applicationId,
            String comment
    ) {

        LeaveApplication application = findApplication(applicationId);

        validateCurrentStatus(application, LeaveStatus.OCD);

        // Hakikisha OCD ni wa District ya mwombaji
        validateAuthority(application, Role.OCD);

        LeaveStatus previousStatus = application.getStatus();
        LeaveStatus newStatus = LeaveStatus.REJECTED;

        saveWorkflowHistory(
                application,
                previousStatus,
                newStatus,
                "REJECTED_BY_OCD",
                comment
        );

        application.setStatus(newStatus);

        return mapToResponse(
                leaveApplicationRepository.save(application)
        );
    }

    // OCD dashboard: hesabu maombi katika kila hatua
    @Transactional(readOnly = true)
    public LeaveDashboardCountsResponse getOCDDashboardCounts() {

        User user = getCurrentUser();

        if (user.getRole() != Role.OCD) {
            throw new AccessDeniedException(
                    "Dashboard hii inaruhusiwa kwa OCD pekee"
            );
        }

        if (user.getDistrict() == null) {
            throw new IllegalStateException(
                    "OCD huyu hajapangiwa District"
            );
        }

        Long districtId = user.getDistrict().getId();

        long waitingForOCD =
                leaveApplicationRepository
                        .countByEmployee_District_IdAndStatus(
                                districtId,
                                LeaveStatus.OCD
                        );

        long waitingForRPC =
                leaveApplicationRepository
                        .countByEmployee_District_IdAndStatus(
                                districtId,
                                LeaveStatus.RPC
                        );

        long waitingForCP =
                leaveApplicationRepository
                        .countByEmployee_District_IdAndStatus(
                                districtId,
                                LeaveStatus.CP
                        );

        long waitingForIGP =
                leaveApplicationRepository
                        .countByEmployee_District_IdAndStatus(
                                districtId,
                                LeaveStatus.IGP
                        );

        return new LeaveDashboardCountsResponse(
                waitingForOCD,
                waitingForRPC,
                waitingForCP,
                waitingForIGP
        );
    }

    @Transactional(readOnly = true)
    public List<LeaveApplicationResponse> getRPCApplications() {

        User user = getCurrentUser();

        if (user.getRole() != Role.RPC) {
            throw new RuntimeException(
                    "Huna ruhusa ya kuona maombi ya RPC"
            );
        }

        if (user.getRegion() == null) {
            throw new RuntimeException(
                    "RPC hajapangiwa Region"
            );
        }

        return leaveApplicationRepository
                .findByEmployee_Region_IdAndStatus(
                        user.getRegion().getId(),
                        LeaveStatus.RPC
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LeaveWorkflowTrackingResponse getMyWorkflowHistory(
            Long applicationId
    ) {

        // Pata mtumiaji aliyeingia
        User user = getCurrentUser();

        // Hakikisha ni Employee
        if (user.getRole() != Role.EMPLOYEE) {
            throw new AccessDeniedException(
                    "Huduma hii inaruhusiwa kwa Employee pekee"
            );
        }

        // Tafuta ombi
        LeaveApplication application = findApplication(applicationId);

        // Hakikisha ombi ni la Employee aliyeingia
        if (application.getEmployee().getUser() == null
                || !Objects.equals(
                application.getEmployee().getUser().getId(),
                user.getId()
        )) {

            throw new AccessDeniedException(
                    "Huna ruhusa ya kuona workflow ya ombi hili"
            );
        }

        // Pata historia yote kwa mpangilio wa muda
        List<LeaveWorkflowHistoryResponse> history =
                historyRepository
                        .findByLeaveApplication_IdOrderByActionAtAsc(
                                applicationId
                        )
                        .stream()
                        .map(item -> new LeaveWorkflowHistoryResponse(
                                item.getId(),
                                item.getActorEmail(),
                                item.getAction(),
                                item.getPreviousStatus(),
                                item.getNewStatus(),
                                item.getComment(),
                                item.getActionAt()
                        ))
                        .toList();

        return new LeaveWorkflowTrackingResponse(
                mapToResponse(application),
                history
        );
    }
}
