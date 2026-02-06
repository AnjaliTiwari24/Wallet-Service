package com.dinoventures.backend.controller;

import com.dinoventures.backend.dto.ApiResponse;
import com.dinoventures.backend.dto.UserDTO;
import com.dinoventures.backend.service.UserService;
import com.dinoventures.backend.util.AuthenticationUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final AuthenticationUtil authenticationUtil;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserDTO>> getUserProfile() {
        log.info("Get user profile request received");

        Long userId = authenticationUtil.getCurrentUserId();
        UserDTO response = userService.getUserProfile(userId);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "User profile retrieved successfully", response));
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserDTO>> updateUserProfile(
            @Valid @RequestBody UserDTO dto) {
        log.info("Update user profile request received");

        Long userId = authenticationUtil.getCurrentUserId();
        UserDTO response = userService.updateUserProfile(userId, dto);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "User profile updated successfully", response));
    }

    @DeleteMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteUser() {
        log.info("Delete user request received");

        Long userId = authenticationUtil.getCurrentUserId();
        userService.deleteUser(userId);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "User deleted successfully"));
    }
}
