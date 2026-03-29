package com.pharmacy.auth.controller;

import com.pharmacy.auth.dto.AdminCreateUserRequest;
import com.pharmacy.auth.dto.UpdateUserStatusRequest;
import com.pharmacy.auth.dto.UserProfileResponse;
import com.pharmacy.auth.entity.Role;
import com.pharmacy.auth.entity.UserStatus;
import com.pharmacy.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal auth endpoints — NOT gateway-routed.
 * Called exclusively by Admin Service via Feign.
 */
@RestController
@RequestMapping("/api/auth/internal")
@RequiredArgsConstructor
public class InternalAuthController {

    private final AuthService authService;

    @GetMapping("/users")
    public ResponseEntity<Page<UserProfileResponse>> listUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Role roleEnum   = (role   != null) ? Role.valueOf(role.toUpperCase())         : null;
        UserStatus statusEnum = (status != null) ? UserStatus.valueOf(status.toUpperCase()) : null;
        return ResponseEntity.ok(authService.listUsers(roleEnum, statusEnum, q, page, size));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserProfileResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(authService.getUserById(id));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<UserProfileResponse> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        return ResponseEntity.ok(authService.updateUserStatus(id, request));
    }

    @PostMapping("/users")
    public ResponseEntity<UserProfileResponse> createUser(
            @Valid @RequestBody AdminCreateUserRequest request) {
        return ResponseEntity.status(201).body(authService.adminCreateUser(request));
    }
}
