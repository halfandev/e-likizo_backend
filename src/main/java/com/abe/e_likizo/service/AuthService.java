
package com.abe.e_likizo.service;

import com.abe.e_likizo.dto.LoginRequest;
import com.abe.e_likizo.dto.LoginResponse;
import com.abe.e_likizo.dto.RegisterRequest;
import com.abe.e_likizo.entity.Role;
import com.abe.e_likizo.entity.User;
import com.abe.e_likizo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.BadCredentialsException;
import com.abe.e_likizo.dto.ChangePasswordRequest;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordGeneratorService passwordGeneratorService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }

        String generatedPassword =
                passwordGeneratorService.generatePassword(10);

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(generatedPassword))
                .role(Role.EMPLOYEE)
                .enabled(true)
                .mustChangePassword(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        emailService.sendLoginCredentials(
                user.getEmail(),
                user.getFullName(),
                generatedPassword
        );
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "Invalid email or password"
                        )
                );

        if (!passwordEncoder.matches(
                request.getPassword(), user.getPassword())) {
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

        String token = jwtService.generateToken(user);

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .mustChangePassword(user.isMustChangePassword())
                .build();
    }


    @Transactional
    public void changePassword(ChangePasswordRequest request) {

        // 1. Hakikisha password mpya na confirmation zinafanana
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException(
                    "New password and confirmation do not match"
            );
        }

        // 2. Pata email ya mtumiaji kutoka JWT authentication
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        // 3. Tafuta mtumiaji kwa email yake
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException(
                        "Authenticated user was not found"
                ));

        // 4. Thibitisha password ya sasa
        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword())) {

            throw new BadCredentialsException(
                    "Current password is incorrect"
            );
        }

        // 5. Zuia mtumiaji kutumia password ileile
        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPassword())) {

            throw new IllegalArgumentException(
                    "New password must be different from current password"
            );
        }

        // 6. Hifadhi password mpya kama BCrypt hash
        user.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        // 7. Ondoa sharti la kubadilisha password ya muda
        user.setMustChangePassword(false);

        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }


    public void logout() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setLastLogout(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        SecurityContextHolder.clearContext();
    }

}
