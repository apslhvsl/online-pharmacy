package com.pharmacy.orderservice.service;

import com.pharmacy.orderservice.dto.AddressRequest;
import com.pharmacy.orderservice.entity.Address;
import com.pharmacy.orderservice.repository.AddressRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock AddressRepository addressRepository;
    @InjectMocks AddressService addressService;

    private Address address(Long id, Long userId) {
        return Address.builder()
                .id(id).userId(userId).label("Home")
                .line1("123 Main St").city("Mumbai").state("MH").pincode("400001")
                .isDefault(false)
                .build();
    }

    private AddressRequest request(boolean isDefault) {
        AddressRequest r = new AddressRequest();
        r.setLabel("Home");
        r.setLine1("123 Main St");
        r.setCity("Mumbai");
        r.setState("MH");
        r.setPincode("400001");
        r.setIsDefault(isDefault);
        return r;
    }

    // ── getAddresses ─────────────────────────────────────────────────

    @Test
    void getAddresses_returnsAllForUser() {
        when(addressRepository.findByUserId(1L)).thenReturn(List.of(address(1L, 1L), address(2L, 1L)));

        var result = addressService.getAddresses(1L);

        assertThat(result).hasSize(2);
    }

    // ── addAddress ───────────────────────────────────────────────────

    @Test
    void addAddress_nonDefault_savesAddress() {
        Address saved = address(1L, 1L);
        when(addressRepository.save(any())).thenReturn(saved);

        var dto = addressService.addAddress(1L, request(false));

        assertThat(dto.getId()).isEqualTo(1L);
        verify(addressRepository, never()).findByUserId(any());
    }

    @Test
    void addAddress_setAsDefault_clearsExistingDefaults() {
        Address existing = address(5L, 1L);
        existing.setIsDefault(true);
        when(addressRepository.findByUserId(1L)).thenReturn(List.of(existing));
        when(addressRepository.save(any())).thenAnswer(inv -> {
            Address a = inv.getArgument(0);
            if (a.getId() == null) a = Address.builder().id(10L).userId(1L).label("Home")
                    .line1("123 Main St").city("Mumbai").state("MH").pincode("400001").isDefault(true).build();
            return a;
        });

        addressService.addAddress(1L, request(true));

        assertThat(existing.getIsDefault()).isFalse();
    }

    // ── updateAddress ────────────────────────────────────────────────

    @Test
    void updateAddress_ownedAddress_updatesFields() {
        Address addr = address(1L, 1L);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(addr));
        when(addressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AddressRequest req = request(false);
        req.setCity("Pune");
        var dto = addressService.updateAddress(1L, 1L, req);

        assertThat(dto.getCity()).isEqualTo("Pune");
    }

    @Test
    void updateAddress_notFound_throwsEntityNotFound() {
        when(addressRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.updateAddress(99L, 1L, request(false)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateAddress_wrongUser_throwsSecurityException() {
        Address addr = address(1L, 10L);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(addr));

        assertThatThrownBy(() -> addressService.updateAddress(1L, 99L, request(false)))
                .isInstanceOf(SecurityException.class);
    }

    // ── deleteAddress ────────────────────────────────────────────────

    @Test
    void deleteAddress_ownedAddress_deletes() {
        Address addr = address(1L, 1L);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(addr));

        addressService.deleteAddress(1L, 1L);

        verify(addressRepository).delete(addr);
    }

    @Test
    void deleteAddress_wrongUser_throwsSecurityException() {
        Address addr = address(1L, 10L);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(addr));

        assertThatThrownBy(() -> addressService.deleteAddress(1L, 99L))
                .isInstanceOf(SecurityException.class);
    }
}
