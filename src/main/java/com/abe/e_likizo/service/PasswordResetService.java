
package com.abe.e_likizo.service;

import com.abe.e_likizo.dto.ForgotPasswordRequest;
import com.abe.e_likizo.dto.ResetPasswordRequest;
import com.abe.e_likizo.entity.PasswordResetToken;
import com.abe.e_likizo.entity.User;
import com.abe.e_likizo.repository.PasswordResetTokenRepository;
import com.abe.e_likizo.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        // Jibu la jumla ili kutofichua kama email imesajiliwa.
        userRepository.findByEmail(email).ifPresent(user -> {

            if (!user.isEnabled()) {
                return;
            }

            // Futa recovery tokens za awali za mtumiaji huyu.
            tokenRepository.deleteByUser(user);

            // Tengeneza token ya random yenye usalama wa kutosha.
            byte[] randomBytes = new byte[32];
            secureRandom.nextBytes(randomBytes);

            String rawToken = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(randomBytes);

            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUser(user);
            resetToken.setTokenHash(hashToken(rawToken));
            resetToken.setCreatedAt(LocalDateTime.now());
            resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
            resetToken.setUsed(false);

            tokenRepository.saveAndFlush(resetToken);

            String resetLink = frontendUrl
                    + "/reset-password?token=" + rawToken;

            emailService.sendPasswordResetLink(
                    user.getEmail(),
                    user.getFullName(),
                    resetLink
            );
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Passwords do not match"
            );
        }

        String tokenHash = hashToken(request.getToken());

        PasswordResetToken resetToken = tokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid or expired recovery link"
                ));

        if (resetToken.isUsed()
                || !resetToken.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid or expired recovery link"
            );
        }

        User user = resetToken.getUser();

        if (!user.isEnabled()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This account cannot reset its password"
            );
        }

        if (passwordEncoder.matches(
                request.getNewPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "New password must differ from the current password"
            );
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        // Token hii haiwezi kutumika tena.
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        // Ondoa recovery tokens nyingine za mtumiaji.
        tokenRepository.deleteByUser(user);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Could not process recovery token", ex
            );
        }
    }
}
