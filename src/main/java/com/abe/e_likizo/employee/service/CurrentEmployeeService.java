
package com.abe.e_likizo.employee.service;

import com.abe.e_likizo.employee.entity.Employee;
import com.abe.e_likizo.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CurrentEmployeeService {

    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public Employee getCurrentEmployee() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {

            throw new AuthenticationCredentialsNotFoundException(
                    "Please login to access this resource"
            );
        }

        String email = authentication.getName();

        return employeeRepository.findByUser_Email(email)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No employee record is linked to this account"
                        )
                );
    }

    @Transactional(readOnly = true)
    public Long getCurrentEmployeeId() {
        return getCurrentEmployee().getId();
    }
}
