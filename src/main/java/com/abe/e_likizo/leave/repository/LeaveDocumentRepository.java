
package com.abe.e_likizo.leave.repository;

import com.abe.e_likizo.leave.entity.LeaveDocument;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveDocumentRepository
        extends JpaRepository<LeaveDocument, Long> {

    // Kupata nyaraka zote za ombi fulani la likizo
    List<LeaveDocument> findByLeaveApplicationId(Long applicationId);

    // Kuhesabu nyaraka za ombi fulani
    long countByLeaveApplicationId(Long applicationId);
}
