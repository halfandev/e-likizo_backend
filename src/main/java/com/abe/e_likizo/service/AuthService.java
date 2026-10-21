
package com.abe.e_likizo.service;

import com.abe.e_likizo.dto.ChangePasswordRequest;
import com.abe.e_likizo.dto.LoginRequest;
import com.abe.e_likizo.dto.LoginResponse;
import com.abe.e_likizo.dto.RegisterRequest;
import com.abe.e_likizo.employee.entity.Employee;
import com.abe.e_likizo.employee.repository.EmployeeRepository;
import com.abe.e_likizo.entity.Role;
import com.abe.e_likizo.entity.User;
import com.abe.e_likizo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordGeneratorService passwordGeneratorService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // =====================================================
    // REGISTER
    // =====================================================


    @Transactional
    public void register(RegisterRequest request) {

        String checknumber = request.getChecknumber().trim();
        String forcenumber = request.getForcenumber().trim();

        String email = request.getEmail()
                .trim()
                .toLowerCase(java.util.Locale.ROOT);

        // 1. Zuia akaunti yenye email iliyotumika tayari.
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Email hii tayari ina akaunti ya login."
            );
        }

        // 2. Tafuta Employee kwa Check Number.
        Employee employee = employeeRepository
                .findByChecknumber(checknumber)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Check Number haijasajiliwa kwenye mfumo."
                ));

        // 3. Hakikisha Force Number inalingana na Employee huyo.
        if (!employee.getForcenumber().trim().equals(forcenumber)) {
            throw new IllegalArgumentException(
                    "Force Number hailingani na taarifa za mfanyakazi."
            );
        }

        // 4. Hakikisha email inalingana na rekodi ya Employee.
        if (!employee.getEmail().trim()
                .equalsIgnoreCase(email)) {
            throw new IllegalArgumentException(
                    "Email hailingani na email iliyosajiliwa na Admin/HR."
            );
        }

        // 5. Zuia Employee kuwa na akaunti zaidi ya moja.
        if (employee.getUser() != null) {
            throw new IllegalArgumentException(
                    "Mfanyakazi huyu tayari ana akaunti ya login."
            );
        }

        // 6. Tengeneza password ya muda.
        String generatedPassword =
                passwordGeneratorService.generatePassword(10);

        LocalDateTime now = LocalDateTime.now();

        // 7. Tengeneza akaunti ya User.
        User user = User.builder()
                .fullName(
                        employee.getFirstname() + " "
                                + (employee.getMiddlename() == null
                                || employee.getMiddlename().isBlank()
                                ? ""
                                : employee.getMiddlename() + " ")
                                + employee.getLastname()
                )
                .email(email)
                .password(passwordEncoder.encode(generatedPassword))
                .role(Role.EMPLOYEE)
                .enabled(true)
                .mustChangePassword(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        User savedUser = userRepository.save(user);

        // 8. Unganisha User na Employee.
        employee.setUser(savedUser);
        employeeRepository.save(employee);

        // 9. Tuma password ya muda kwenye email iliyothibitishwa.
        emailService.sendLoginCredentials(
                savedUser.getEmail(),
                savedUser.getFullName(),
                generatedPassword
        );
    }

    // =====================================================
    // LOGIN
    // =====================================================

    @Transactional
    public LoginResponse login(LoginRequest request) {



        String email = request.getEmail().trim();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "Invalid email or password"
                        )
                );

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new BadCredentialsException(
                    "Invalid email or password"
            );
        }

        if (!user.isEnabled()) {
            throw new DisabledException(
                    "Your account has been disabled. Contact administrator."
            );
        }

        LocalDateTime now = LocalDateTime.now();

        user.setLastLogin(now);
        user.setLastActivity(now);
        user.setUpdatedAt(now);


        // Tafuta Employee ID kwa kutumia User ID.
        // Tafuta Employee ID kwa kutumia email ya User.
        Long employeeId = employeeRepository
                .findByUser_Id(user.getId())
                .map(Employee::getId)
                .orElse(null);

        // DEBUG: Chunguza kama User ana Employee anayehusishwa naye.
        System.out.println("========== LOGIN DEBUG ==========");
        System.out.println("LOGIN USER ID: " + user.getId());
        System.out.println("LOGIN EMAIL: " + user.getEmail());
        System.out.println("EMPLOYEE ID BY USER ID: " + employeeId);

        // Tafuta Employee kwa email ili kujua kama rekodi ipo.
        employeeRepository.findByEmail(user.getEmail())
                .ifPresentOrElse(
                        employee -> {
                            System.out.println(
                                    "EMPLOYEE RECORD ID: " + employee.getId()
                            );

                            System.out.println(
                                    "EMPLOYEE EMAIL: " + employee.getEmail()
                            );

                            System.out.println(
                                    "LINKED USER ID: "
                                            + (employee.getUser() != null
                                            ? employee.getUser().getId()
                                            : null)
                            );
                        },
                        () -> System.out.println(
                                "HAKUNA EMPLOYEE MWENYE EMAIL HII"
                        )
                );

        System.out.println("=================================");

        String token = jwtService.generateToken(user);


        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .employeeId(employeeId)
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .mustChangePassword(user.isMustChangePassword())
                .build();
    }



    // =====================================================
    // CHANGE PASSWORD
    // =====================================================

    @Transactional
    public void changePassword(ChangePasswordRequest request) {

        if (!request.getNewPassword().equals(
                request.getConfirmPassword())) {

            throw new IllegalArgumentException(
                    "New password and confirmation do not match"
            );
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {

            throw new BadCredentialsException(
                    "User is not authenticated"
            );
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException(
                        "Authenticated user was not found"
                ));

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword())) {

            throw new BadCredentialsException(
                    "Current password is incorrect"
            );
        }

        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPassword())) {

            throw new IllegalArgumentException(
                    "New password must be different from current password"
            );
        }

        user.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        user.setMustChangePassword(false);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    // =====================================================
    // LOGOUT
    // =====================================================

    @Transactional
    public void logout() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {

            throw new BadCredentialsException(
                    "User is not authenticated"
            );
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        LocalDateTime now = LocalDateTime.now();

        user.setLastLogout(now);
        user.setUpdatedAt(now);

        userRepository.save(user);

        SecurityContextHolder.clearContext();
    }
}
