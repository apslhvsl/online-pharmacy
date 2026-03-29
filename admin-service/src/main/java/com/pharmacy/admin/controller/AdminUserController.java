package com.pharmacy.admin.controller;

import com.pharmacy.admin.dto.AdminCreateUserRequest;
import com.pharmacy.admin.dto.PagedResponse;
import com.pharmacy.admin.dto.UpdateUserStatusRequest;
import com.pharmacy.admin.dto.UserProfileResponse;
import com.pharmacy.admin.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "List all users", description = "Returns a paginated list of all users with optional filters for role, status, and search query")
    @GetMapping
    public ResponseEntity<PagedResponse<UserProfileResponse>> listUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminUserService.listUsers(role, status, q, page, size));
    }

    @Operation(summary = "Get user by ID", description = "Returns the profile details of a specific user by their ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.getUserById(id));
    }

    @Operation(summary = "Update user status", description = "Activates or deactivates a user account, effectively controlling their access to the platform")
    @PatchMapping("/{id}/status")
    public ResponseEntity<UserProfileResponse> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        return ResponseEntity.ok(adminUserService.updateUserStatus(id, request));
    }

    @Operation(summary = "Create a new user", description = "Creates a new user account with a specified role (e.g., ADMIN or CUSTOMER) on behalf of an administrator")
    @PostMapping
    public ResponseEntity<UserProfileResponse> createUser(
            @Valid @RequestBody AdminCreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminUserService.createUser(request));
    }
}
