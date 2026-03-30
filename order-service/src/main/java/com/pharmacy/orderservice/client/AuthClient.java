package com.pharmacy.orderservice.client;

import com.pharmacy.orderservice.dto.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service")
public interface AuthClient {

    @GetMapping("/api/auth/internal/users/{id}")
    UserInfo getUserById(@PathVariable("id") Long userId);
}
