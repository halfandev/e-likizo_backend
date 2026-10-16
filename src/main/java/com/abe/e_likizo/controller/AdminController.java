
package com.abe.e_likizo.controller;

import com.abe.e_likizo.dto.AdminCreateUserRequest;
import com.abe.e_likizo.dto.UserResponse;
import com.abe.e_likizo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.abe.e_likizo.dto.OnlineUserResponse;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }


    @PatchMapping("/users/{id}/disable")
    public UserResponse disableUser(@PathVariable Long id) {
        return userService.disableUser(id);
    }

    @PatchMapping("/users/{id}/enable")
    public UserResponse enableUser(@PathVariable Long id) {
        return userService.enableUser(id);
    }


    @GetMapping("/users/online")
    public List<OnlineUserResponse> getOnlineUsers() {
        return userService.getOnlineUsers();
    }



    @PostMapping("/users/register")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody AdminCreateUserRequest request) {

        UserResponse response =
                userService.createUserByAdmin(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }


}
