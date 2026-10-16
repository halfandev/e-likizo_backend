
package com.abe.e_likizo.config;

import com.abe.e_likizo.entity.Role;
import com.abe.e_likizo.entity.User;
import com.abe.e_likizo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class AdminInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.full-name}")
    private String adminFullName;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Bean
    public CommandLineRunner createAdmin() {
        return args -> {

            if (userRepository.existsByEmail(adminEmail)) {
                System.out.println("Admin account already exists.");
                return;
            }

            User admin = User.builder()
                    .fullName(adminFullName)
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .enabled(true)
                    .mustChangePassword(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            userRepository.save(admin);

            System.out.println("Initial admin created successfully.");
            System.out.println("Admin email: " + adminEmail);
        };
    }
}
