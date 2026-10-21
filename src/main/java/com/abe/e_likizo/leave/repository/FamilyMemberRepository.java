
package com.abe.e_likizo.leave.repository;

import com.abe.e_likizo.leave.entity.FamilyMember;
import com.abe.e_likizo.leave.enums.FamilyRelationship;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FamilyMemberRepository
        extends JpaRepository<FamilyMember, Long> {

    // Kupata wanafamilia wote wa mfanyakazi
    List<FamilyMember> findByEmployeeId(Long employeeId);

    // Kupata mke/mume au watoto wa mfanyakazi
    List<FamilyMember> findByEmployeeIdAndRelationship(
            Long employeeId,
            FamilyRelationship relationship
    );

    // Kuhesabu watoto au wenzi wa mfanyakazi
    long countByEmployeeIdAndRelationship(
            Long employeeId,
            FamilyRelationship relationship
    );
}
