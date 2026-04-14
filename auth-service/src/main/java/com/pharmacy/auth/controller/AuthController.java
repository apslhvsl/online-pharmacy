package com.pharmacy.auth.controller;

import com.pharmacy.auth.dto.*;
import com.pharmacy.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ── Public endpoints ──────────────────────────────────────────────

    @Operation(summary = "Register a new user", description = "Creates a new customer account and returns an authentication token pair")
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        log.info("Signup request | email={}", request.getEmail());
        AuthResponse response = authService.signup(request);
        log.info("Signup successful | userId={}", response.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Authenticate a user", description = "Validates credentials and returns an access token and refresh token")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request | email={}", request.getEmail());
        AuthResponse response = authService.login(request);
        log.info("Login successful | userId={} role={}", response.getUserId(), response.getUserRole());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Refresh access token", description = "Issues a new access token using a valid refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request");
        return ResponseEntity.ok(authService.refresh(request));
    }

    @Operation(summary = "Request a password reset", description = "Sends a password reset link to the provided email address if it exists in the system")
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Forgot password request | email={}", request.getEmail());
        authService.forgotPassword(request);
        return ResponseEntity.ok(Map.of("message", "If the email exists, a reset link has been sent"));
    }

    @Operation(summary = "Reset password using token", description = "Resets the user's password using a valid password reset token received via email")
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Password reset attempt");
        authService.resetPassword(request);
        log.info("Password reset successful");
        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }

    // ── Authenticated endpoints (CUSTOMER + ADMIN) ────────────────────

    @Operation(summary = "Log out the current user", description = "Invalidates the user's refresh token, effectively ending the session")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        log.info("Logout | userId={}", userId);
        authService.logout(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get current user profile", description = "Returns the profile information of the currently authenticated user")
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile(
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            log.warn("Get profile with no userId");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("Get profile | userId={}", userId);
        return ResponseEntity.ok(authService.getProfile(userId));
    }

    @Operation(summary = "Update user profile", description = "Updates the name, phone, or other profile fields of the currently authenticated user")
    @PutMapping("/update-profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        log.info("Update profile | userId={}", userId);
        return ResponseEntity.ok(authService.updateProfile(userId, request));
    }

    @Operation(summary = "Change password", description = "Allows the authenticated user to change their password by providing the current and new password")
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("Change password | userId={}", userId);
        authService.changePassword(userId, request);
        log.info("Password changed | userId={}", userId);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}
