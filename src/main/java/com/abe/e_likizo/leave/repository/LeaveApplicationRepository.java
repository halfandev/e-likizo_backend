
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

    // Kupata maombi yote ya likizo ya mfanyakazi mmoja
    List<LeaveApplication> findByEmployeeId(Long employeeId);

    // Kupata maombi ya mfanyakazi kwa status maalumu
    List<LeaveApplication> findByEmployeeIdAndStatus(
            Long employeeId,
            LeaveStatus status
    );

    // Kupata maombi yote yenye status maalumu
    List<LeaveApplication> findByStatus(LeaveStatus status);

    // Kupata maombi yaliyowasilishwa ndani ya kipindi cha tarehe
    List<LeaveApplication> findByCreatedAtBetween(
            LocalDate startDate,
            LocalDate endDate
    );

    // Kuhesabu maombi ya mfanyakazi kwa status maalumu
    long countByEmployeeIdAndStatus(
            Long employeeId,
            LeaveStatus status
    );
}
