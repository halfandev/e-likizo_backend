package com.abe.e_likizo.leave.repository;

import com.abe.e_likizo.leave.entity.LeaveApplication;
import com.abe.e_likizo.leave.enums.LeaveStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveApplicationRepository
        extends JpaRepository<LeaveApplication, Long> {

    // Kuhesabu maombi yote ya mfanyakazi bila kujali status
    long countByEmployee_Id(Long employeeId);

    // Maombi yote ya mfanyakazi mmoja
    List<LeaveApplication> findByEmployeeId(Long employeeId);

    // Maombi ya mfanyakazi kwa status maalumu
    List<LeaveApplication> findByEmployeeIdAndStatus(
            Long employeeId,
            LeaveStatus status
    );

    // Maombi yote kwa status maalumu
    List<LeaveApplication> findByStatus(LeaveStatus status);

    // Maombi kwa status zaidi ya moja
    List<LeaveApplication> findByStatusIn(List<LeaveStatus> statuses);

    // Maombi yaliyowasilishwa ndani ya kipindi cha tarehe
    List<LeaveApplication> findByCreatedAtBetween(
            LocalDate startDate,
            LocalDate endDate
    );

    // Kuhesabu maombi ya mfanyakazi kwa status maalumu
    long countByEmployee_IdAndStatus(
            Long employeeId,
            LeaveStatus status
    );

    List<LeaveApplication> findByEmployee_Station_IdAndStatus(
            Long stationId,
            LeaveStatus status
    );

    List<LeaveApplication> findByEmployee_District_IdAndStatus(
            Long districtId,
            LeaveStatus status
    );

    List<LeaveApplication> findByEmployee_Region_IdAndStatus(
            Long regionId,
            LeaveStatus status
    );

    List<LeaveApplication> findByEmployee_Administrative_IdAndStatus(
            Long administrativeId,
            LeaveStatus status
    );

    // Kuhesabu maombi ya District kwa status maalumu
    long countByEmployee_District_IdAndStatus(
            Long districtId,
            LeaveStatus status
    );

    // Kuhesabu maombi ya Station kwa status maalumu
    long countByEmployee_Station_IdAndStatus(
            Long stationId,
            LeaveStatus status
    );

}