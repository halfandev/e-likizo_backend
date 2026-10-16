
package com.abe.e_likizo.service;

import com.abe.e_likizo.dto.UserResponse;
import com.abe.e_likizo.entity.User;
import com.abe.e_likizo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;
import com.abe.e_likizo.dto.AdminCreateUserRequest;
import com.abe.e_likizo.entity.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import com.abe.e_likizo.dto.OnlineUserResponse;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordGeneratorService passwordGeneratorService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;


    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private UserResponse mapToResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .enabled(user.isEnabled())
                .mustChangePassword(user.isMustChangePassword())
                .lastLogin(user.getLastLogin())
                .lastLogout(user.getLastLogout())
                .createdAt(user.getCreatedAt())
                .build();
    }


    public UserResponse disableUser(Long id) {
        return updateUserStatus(id, false);
    }

    public UserResponse enableUser(Long id) {
        return updateUserStatus(id, true);
    }

    private UserResponse updateUserStatus(Long id, boolean enabled) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with ID: " + id
                ));

        user.setEnabled(enabled);
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }


    public void recordActivity(User user) {
        user.setLastActivity(LocalDateTime.now());
        userRepository.save(user);
    }

    public List<OnlineUserResponse> getOnlineUsers() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minusMinutes(5);

        return userRepository.findAll()
                .stream()
                .filter(User::isEnabled)
                .filter(user -> user.getLastLogin() != null)
                .filter(user -> user.getLastActivity() != null)
                .filter(user -> user.getLastActivity().isAfter(cutoff))
                .filter(user -> user.getLastLogout() == null
                        || user.getLastLogout().isBefore(user.getLastLogin()))
                .map(user -> OnlineUserResponse.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .lastLogin(user.getLastLogin())
                        .lastActivity(user.getLastActivity())
                        .onlineDurationMinutes(
                                Math.max(0, Duration.between(
                                        user.getLastLogin(), now
                                ).toMinutes())
                        )
                        .build())
                .toList();
    }


    @Transactional
    public UserResponse createUserByAdmin(AdminCreateUserRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists"
            );
        }

        Role role = request.getRole();

        // ADMIN mpya hawezi kuundwa kupitia endpoint hii.
        if (role == null || role == Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid role for this operation"
            );
        }

        String temporaryPassword =
                passwordGeneratorService.generatePassword(12);

        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(email)
                .password(passwordEncoder.encode(temporaryPassword))
                .role(role)
                .enabled(true)
                .mustChangePassword(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        User savedUser = userRepository.save(user);

        // Tumia method ileile ya EmailService ambayo
        // AuthService yako hutumia kutuma credentials.
        // Badilisha jina la method/arguments kulingana na
        // signature halisi ya EmailService yako.
        emailService.sendLoginCredentials(
                savedUser.getEmail(),
                savedUser.getFullName(),
                temporaryPassword
        );

        return mapToResponse(savedUser);
    }



}
