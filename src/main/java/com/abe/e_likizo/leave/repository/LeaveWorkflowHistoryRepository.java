
package com.abe.e_likizo.leave.repository;

import com.abe.e_likizo.leave.entity.LeaveWorkflowHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveWorkflowHistoryRepository
        extends JpaRepository<LeaveWorkflowHistory, Long> {

    List<LeaveWorkflowHistory>
    findByLeaveApplication_IdOrderByActionAtAsc(Long applicationId);
}
