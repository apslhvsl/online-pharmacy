package com.pharmacy.orderservice.controller;

import com.pharmacy.orderservice.dto.AddressDto;
import com.pharmacy.orderservice.dto.AddressRequest;
import com.pharmacy.orderservice.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @Operation(summary = "List saved addresses", description = "Returns all delivery addresses saved by the currently authenticated user")
    @GetMapping
    public ResponseEntity<List<AddressDto>> getAddresses(@Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(addressService.getAddresses(userId));
    }

    @Operation(summary = "Add a new address", description = "Saves a new delivery address for the currently authenticated user")
    @PostMapping
    public ResponseEntity<AddressDto> addAddress(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.addAddress(userId, request));
    }

    @Operation(summary = "Update an address", description = "Replaces all fields of an existing saved address belonging to the authenticated user")
    @PutMapping("/{id}")
    public ResponseEntity<AddressDto> updateAddress(
            @PathVariable Long id,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.updateAddress(id, userId, request));
    }

    @Operation(summary = "Delete an address", description = "Permanently removes a saved delivery address belonging to the authenticated user")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long id,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        addressService.deleteAddress(id, userId);
        return ResponseEntity.noContent().build();
    }
}
