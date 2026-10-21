
package com.abe.e_likizo.leave.repository;

import com.abe.e_likizo.leave.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeaveTypeRepository
        extends JpaRepository<LeaveType, Long> {

    // Tafuta aina ya likizo kwa code yake
    Optional<LeaveType> findByCode(String code);

    // Hakikisha code haijatumika
    boolean existsByCode(String code);

    // Pata aina zote za likizo zilizo active
    List<LeaveType> findByActiveTrue();

    // Tafuta aina ya likizo kwa jina
    Optional<LeaveType> findByNameIgnoreCase(String name);
}
