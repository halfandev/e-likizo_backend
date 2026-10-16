
package com.abe.e_likizo.controller;

import com.abe.e_likizo.dto.ForgotPasswordRequest;
import com.abe.e_likizo.dto.ResetPasswordRequest;
import com.abe.e_likizo.service.PasswordResetService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        passwordResetService.forgotPassword(request);

        return ResponseEntity.ok(Map.of(
                "message",
                "If the account exists, password recovery instructions will be sent to its email."
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        passwordResetService.resetPassword(request);

        return ResponseEntity.ok(Map.of(
                "message",
                "Password has been reset successfully. You can now log in."
        ));
    }
}
