package com.abe.e_likizo.controller;

import com.abe.e_likizo.dto.LoginRequest;
import com.abe.e_likizo.dto.LoginResponse;
import com.abe.e_likizo.dto.RegisterRequest;
import com.abe.e_likizo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.abe.e_likizo.dto.ChangePasswordRequest;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Registration successful. Login credentials have been sent to your email.");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }


    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        authService.changePassword(request);

        return ResponseEntity.ok(
                "Password changed successfully"
        );
    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout() {

        authService.logout();

        return ResponseEntity.ok("Logout successful");
    }



}
