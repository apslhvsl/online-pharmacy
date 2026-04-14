package com.pharmacy.orderservice.controller;

import com.pharmacy.orderservice.dto.AddressDto;
import com.pharmacy.orderservice.dto.AddressRequest;
import com.pharmacy.orderservice.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @Operation(summary = "List saved addresses", description = "Returns all delivery addresses saved by the currently authenticated user")
    @GetMapping
    public ResponseEntity<List<AddressDto>> getAddresses(@Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        log.info("List addresses | userId={}", userId);
        return ResponseEntity.ok(addressService.getAddresses(userId));
    }

    @Operation(summary = "Add a new address", description = "Saves a new delivery address for the currently authenticated user")
    @PostMapping
    public ResponseEntity<AddressDto> addAddress(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AddressRequest request) {
        log.info("Add address | userId={}", userId);
        AddressDto result = addressService.addAddress(userId, request);
        log.info("Address added | userId={} addressId={}", userId, result.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Operation(summary = "Update an address", description = "Replaces all fields of an existing saved address belonging to the authenticated user")
    @PutMapping("/{id}")
    public ResponseEntity<AddressDto> updateAddress(
            @PathVariable Long id,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @RequestBody AddressRequest request) {
        log.info("Update address | userId={} addressId={}", userId, id);
        return ResponseEntity.ok(addressService.updateAddress(id, userId, request));
    }

    @Operation(summary = "Delete an address", description = "Permanently removes a saved delivery address belonging to the authenticated user")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long id,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        log.info("Delete address | userId={} addressId={}", userId, id);
        addressService.deleteAddress(id, userId);
        return ResponseEntity.noContent().build();
    }
}
