
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

        String email = request.getEmail().trim();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Email is already registered"
            );
        }

        // Tafuta employee aliyepo kwa email hii.
        Employee employee = employeeRepository
                .findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No employee record found for this email"
                ));

        // Zuia employee kuunganishwa na akaunti nyingine.
        if (employee.getUser() != null) {
            throw new IllegalArgumentException(
                    "This employee is already linked to a user account"
            );
        }

        String generatedPassword =
                passwordGeneratorService.generatePassword(10);

        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .fullName(request.getFullName())
                .email(email)
                .password(passwordEncoder.encode(generatedPassword))
                .role(Role.EMPLOYEE)
                .enabled(true)
                .mustChangePassword(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        User savedUser = userRepository.save(user);

        // Unganisha akaunti na employee.
        employee.setUser(savedUser);
        employeeRepository.save(employee);

        // Tuma credentials baada ya kuhifadhi taarifa.
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
                .findByEmail(user.getEmail())
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
