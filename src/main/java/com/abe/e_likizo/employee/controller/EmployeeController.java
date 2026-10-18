
package com.abe.e_likizo.employee.controller;

import com.abe.e_likizo.employee.dto.EmployeeProfileResponse;
import com.abe.e_likizo.employee.dto.EmployeeRegistrationRequest;
import com.abe.e_likizo.employee.service.EmployeeService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    // =====================================
    // ADMIN/HR: KUSAJILI TAARIFA ZA MFANYAKAZI
    // =====================================
    @PostMapping
    public ResponseEntity<EmployeeProfileResponse> registerEmployee(
            @Valid @RequestBody EmployeeRegistrationRequest request) {

        EmployeeProfileResponse response =
                employeeService.registerEmployee(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // =====================================
    // EMPLOYEE: KUPATA TAARIFA ZAKE
    // =====================================
    @GetMapping("/me")
    public ResponseEntity<EmployeeProfileResponse> getMyProfile() {

        EmployeeProfileResponse response =
                employeeService.getMyProfile();

        return ResponseEntity.ok(response);
    }

    // ADMIN/HR: ORODHA YA WAFANYAKAZI
    @GetMapping
    public ResponseEntity<List<EmployeeProfileResponse>> getAllEmployees() {
        return ResponseEntity.ok(
                employeeService.getAllEmployees()
        );
    }

    // ADMIN/HR: TAARIFA ZA MFANYAKAZI MMOJA
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeProfileResponse> getEmployeeById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                employeeService.getEmployeeById(id)
        );
    }

    // ADMIN/HR: KUBADILISHA TAARIFA
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeProfileResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRegistrationRequest request) {

        return ResponseEntity.ok(
                employeeService.updateEmployee(id, request)
        );
    }

    // ADMIN/HR: KUFUTA PROFILE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(
            @PathVariable Long id) {

        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
