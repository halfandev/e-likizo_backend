
package com.abe.e_likizo.employee.repository;

import com.abe.e_likizo.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByChecknumber(String checknumber);

    boolean existsByForcenumber(String forcenumber);

    boolean existsByEmail(String email);

    boolean existsByUser_Email(String email);

    Optional<Employee> findByChecknumber(String checknumber);

    Optional<Employee> findByForcenumber(String forcenumber);

    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByUser_Email(String email);

    // Pata Employee kwa kutumia ID ya User aliyeunganishwa naye
    Optional<Employee> findByUser_Id(Long userId);
}
