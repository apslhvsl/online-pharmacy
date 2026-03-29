package com.pharmacy.auth.service;

import com.pharmacy.auth.config.RabbitMQConfig;
import com.pharmacy.auth.dto.*;
import com.pharmacy.auth.entity.*;
import com.pharmacy.auth.exception.DuplicateEmailException;
import com.pharmacy.auth.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RabbitTemplate rabbitTemplate;

    // ── Signup ────────────────────────────────────────────────────────
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateEmailException("Email already registered: " + request.getEmail());
        }
        if (userRepository.findByMobile(request.getMobile()).isPresent()) {
            throw new DuplicateEmailException("Mobile number already registered: " + request.getMobile());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .mobile(request.getMobile())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        User saved = userRepository.save(user);
        return buildAuthResponse(saved);
    }

    // ── Login ─────────────────────────────────────────────────────────
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadCredentialsException("Account is not active");
        }

        return buildAuthResponse(user);
    }

    // ── Refresh token ─────────────────────────────────────────────────
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Refresh token expired or revoked");
        }

        // Rotate: revoke old, issue new
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return buildAuthResponse(stored.getUser());
    }

    // ── Logout ────────────────────────────────────────────────────────
    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
    }

    // ── Get own profile ───────────────────────────────────────────────
    public UserProfileResponse getProfile(Long userId) {
        User user = findUserById(userId);
        return toProfileResponse(user);
    }

    // ── Update own profile ────────────────────────────────────────────
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findUserById(userId);
        user.setName(request.getName());
        user.setMobile(request.getMobile());
        return toProfileResponse(userRepository.save(user));
    }

    // ── Change password ───────────────────────────────────────────────
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findUserById(userId);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        // Invalidate all refresh tokens on password change
        refreshTokenRepository.deleteAllByUserId(userId);
    }

    // ── Forgot password ───────────────────────────────────────────────
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            // Remove any existing reset tokens
            passwordResetTokenRepository.deleteAllByUserId(user.getId());

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .token(UUID.randomUUID().toString())
                    .expiresAt(LocalDateTime.now().plusMinutes(15))
                    .build();
            passwordResetTokenRepository.save(resetToken);

            PasswordResetEvent event = PasswordResetEvent.builder()
                    .userId(user.getId())
                    .userEmail(user.getEmail())
                    .userName(user.getName() != null ? user.getName() : user.getEmail())
                    .resetToken(resetToken.getToken())
                    .expiresAt(resetToken.getExpiresAt())
                    .build();
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.PASSWORD_ROUTING_KEY,
                    event
            );
        });
        // Always return success to avoid email enumeration
    }

    // ── Reset password ────────────────────────────────────────────────
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired reset token"));

        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Reset token expired or already used");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        refreshTokenRepository.deleteAllByUserId(user.getId());
    }

    // ── Admin: list users ─────────────────────────────────────────────
    public Page<UserProfileResponse> listUsers(Role role, UserStatus status, String q, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAllWithFilters(role, status, q, pageable)
                .map(this::toProfileResponse);
    }

    // ── Admin: get user by id ─────────────────────────────────────────
    public UserProfileResponse getUserById(Long id) {
        return toProfileResponse(findUserById(id));
    }

    // ── Admin: update user status ─────────────────────────────────────
    @Transactional
    public UserProfileResponse updateUserStatus(Long id, UpdateUserStatusRequest request) {
        User user = findUserById(id);
        user.setStatus(request.getStatus());
        return toProfileResponse(userRepository.save(user));
    }

    // ── Admin: create user ────────────────────────────────────────────
    @Transactional
    public UserProfileResponse adminCreateUser(AdminCreateUserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateEmailException("Email already registered: " + request.getEmail());
        }
        if (userRepository.findByMobile(request.getMobile()).isPresent()) {
            throw new DuplicateEmailException("Mobile number already registered: " + request.getMobile());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .mobile(request.getMobile())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(UserStatus.ACTIVE)
                .build();

        return toProfileResponse(userRepository.save(user));
    }

    // ── Helpers ───────────────────────────────────────────────────────
    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        String rawRefresh = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(rawRefresh)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefresh)
                .expiresIn(jwtService.getExpirationMs() / 1000)
                .userId(user.getId())
                .userRole(user.getRole().name())
                .build();
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    private UserProfileResponse toProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
