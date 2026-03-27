package com.pharmacy.admin.service;

import com.pharmacy.admin.client.AuthClient;
import com.pharmacy.admin.dto.AdminCreateUserRequest;
import com.pharmacy.admin.dto.PagedResponse;
import com.pharmacy.admin.dto.UpdateUserStatusRequest;
import com.pharmacy.admin.dto.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AuthClient authClient;

    public PagedResponse<UserProfileResponse> listUsers(String role, String status, String q, int page, int size) {
        return authClient.listUsers(role, status, q, page, size);
    }

    public UserProfileResponse getUserById(Long id) {
        return authClient.getUserById(id);
    }

    public UserProfileResponse updateUserStatus(Long id, UpdateUserStatusRequest request) {
        return authClient.updateUserStatus(id, request);
    }

    public UserProfileResponse createUser(AdminCreateUserRequest request) {
        return authClient.createUser(request);
    }
}
