
package com.abe.e_likizo.leave.service;

import com.abe.e_likizo.leave.dto.LeaveTypeRequest;
import com.abe.e_likizo.leave.entity.LeaveType;
import com.abe.e_likizo.leave.repository.LeaveTypeRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;

    public LeaveTypeService(LeaveTypeRepository leaveTypeRepository) {
        this.leaveTypeRepository = leaveTypeRepository;
    }

    // Kupata aina zote za likizo zilizo active
    public List<LeaveType> getAllActiveLeaveTypes() {
        return leaveTypeRepository.findByActiveTrue();
    }

    // Kupata aina ya likizo kwa ID
    public LeaveType getLeaveTypeById(Long id) {
        return leaveTypeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Leave type not found with ID: " + id
                        )
                );
    }

    // Kupata aina ya likizo kwa code
    public LeaveType getLeaveTypeByCode(String code) {
        return leaveTypeRepository.findByCode(code)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Leave type not found with code: " + code
                        )
                );
    }


    @Transactional
    public void initializeDefaultLeaveTypes() {

        createLeaveTypeIfNotExists(
                "ANNUAL",
                "Annual Leave",
                "Likizo ya mwaka",
                28,
                false
        );

        createLeaveTypeIfNotExists(
                "SICK",
                "Sick Leave",
                "Likizo ya ugonjwa",
                7,
                true
        );

        createLeaveTypeIfNotExists(
                "MATERNITY",
                "Maternity Leave",
                "Likizo ya uzazi",
                84,
                true
        );

        createLeaveTypeIfNotExists(
                "RETIREMENT",
                "Leave Pending Retirement",
                "Likizo kabla ya kustaafu",
                28,
                true
        );
    }

    private void createLeaveTypeIfNotExists(
            String code,
            String name,
            String description,
            int defaultDays,
            boolean requiresDocument
    ) {
        if (leaveTypeRepository.existsByCode(code)) {
            return;
        }

        LeaveType leaveType = new LeaveType();
        leaveType.setCode(code);
        leaveType.setName(name);
        leaveType.setDescription(description);
        leaveType.setDefaultDays(defaultDays);
        leaveType.setRequiresDocument(requiresDocument);
        leaveType.setActive(true);

        leaveTypeRepository.save(leaveType);
    }


    // Admin: kuona aina zote, hata zilizozimwa
    public List<LeaveType> getAllLeaveTypes() {
        return leaveTypeRepository.findAll();
    }

    // Admin: kuongeza aina mpya ya likizo
    @Transactional
    public LeaveType createLeaveType(LeaveTypeRequest request) {

        String code = request.code().trim().toUpperCase();

        if (leaveTypeRepository.existsByCode(code)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Leave type code already exists: " + code
            );
        }

        LeaveType leaveType = new LeaveType();
        leaveType.setCode(code);
        leaveType.setName(request.name().trim());
        leaveType.setDescription(request.description());
        leaveType.setDefaultDays(request.defaultDays());
        leaveType.setRequiresDocument(request.requiresDocument());
        leaveType.setActive(true);

        return leaveTypeRepository.save(leaveType);
    }

    // Admin: kusasisha aina iliyopo
    @Transactional
    public LeaveType updateLeaveType(
            Long id,
            LeaveTypeRequest request) {

        LeaveType leaveType = getLeaveTypeById(id);
        String code = request.code().trim().toUpperCase();

        leaveTypeRepository.findByCode(code)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Leave type code already exists: " + code
                    );
                });

        leaveType.setCode(code);
        leaveType.setName(request.name().trim());
        leaveType.setDescription(request.description());
        leaveType.setDefaultDays(request.defaultDays());
        leaveType.setRequiresDocument(request.requiresDocument());

        return leaveTypeRepository.save(leaveType);
    }

    // Admin: kuzima aina bila kufuta rekodi
    @Transactional
    public LeaveType deactivateLeaveType(Long id) {
        LeaveType leaveType = getLeaveTypeById(id);
        leaveType.setActive(false);
        return leaveTypeRepository.save(leaveType);
    }

    // Admin: kuwasha tena aina iliyozimwa
    @Transactional
    public LeaveType activateLeaveType(Long id) {
        LeaveType leaveType = getLeaveTypeById(id);
        leaveType.setActive(true);
        return leaveTypeRepository.save(leaveType);
    }

}
