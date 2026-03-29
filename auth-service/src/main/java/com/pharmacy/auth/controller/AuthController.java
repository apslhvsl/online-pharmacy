package com.pharmacy.auth.controller;

import com.pharmacy.auth.dto.*;
import com.pharmacy.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ── Public endpoints ──────────────────────────────────────────────

    @Operation(summary = "Register a new user", description = "Creates a new customer account and returns an authentication token pair")
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
    }

    @Operation(summary = "Authenticate a user", description = "Validates credentials and returns an access token and refresh token")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Refresh access token", description = "Issues a new access token using a valid refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @Operation(summary = "Request a password reset", description = "Sends a password reset link to the provided email address if it exists in the system")
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(Map.of("message", "If the email exists, a reset link has been sent"));
    }

    @Operation(summary = "Reset password using token", description = "Resets the user's password using a valid password reset token received via email")
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }

    // ── Authenticated endpoints (CUSTOMER + ADMIN) ────────────────────

    @Operation(summary = "Log out the current user", description = "Invalidates the user's refresh token, effectively ending the session")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        authService.logout(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get current user profile", description = "Returns the profile information of the currently authenticated user")
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile(
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(authService.getProfile(userId));
    }

    @Operation(summary = "Update user profile", description = "Updates the name, phone, or other profile fields of the currently authenticated user")
    @PutMapping("/update-profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(authService.updateProfile(userId, request));
    }

    @Operation(summary = "Change password", description = "Allows the authenticated user to change their password by providing the current and new password")
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userId, request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}
