package com.pharmacy.admin.client;

import com.pharmacy.admin.dto.AdminCreateUserRequest;
import com.pharmacy.admin.dto.PagedResponse;
import com.pharmacy.admin.dto.UpdateUserStatusRequest;
import com.pharmacy.admin.dto.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "auth-service")
public interface AuthClient {

    @GetMapping("/api/auth/internal/users")
    PagedResponse<UserProfileResponse> listUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size);

    @GetMapping("/api/auth/internal/users/{id}")
    UserProfileResponse getUserById(@PathVariable Long id);

    @PatchMapping("/api/auth/internal/users/{id}/status")
    UserProfileResponse updateUserStatus(@PathVariable Long id,
                                         @RequestBody UpdateUserStatusRequest request);

    @PostMapping("/api/auth/internal/users")
    UserProfileResponse createUser(@RequestBody AdminCreateUserRequest request);
}
