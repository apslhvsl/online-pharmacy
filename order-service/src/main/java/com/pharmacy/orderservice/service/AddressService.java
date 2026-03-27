package com.pharmacy.orderservice.service;

import com.pharmacy.orderservice.dto.AddressDto;
import com.pharmacy.orderservice.dto.AddressRequest;
import com.pharmacy.orderservice.entity.Address;
import com.pharmacy.orderservice.repository.AddressRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    public List<AddressDto> getAddresses(Long userId) {
        return addressRepository.findByUserId(userId).stream().map(this::toDto).toList();
    }

    @Transactional
    public AddressDto addAddress(Long userId, AddressRequest request) {
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            // clear existing default
            addressRepository.findByUserId(userId).forEach(a -> {
                a.setIsDefault(false);
                addressRepository.save(a);
            });
        }
        Address address = Address.builder()
                .userId(userId)
                .label(request.getLabel())
                .line1(request.getLine1())
                .line2(request.getLine2())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .build();
        return toDto(addressRepository.save(address));
    }

    @Transactional
    public AddressDto updateAddress(Long id, Long userId, AddressRequest request) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Address not found: " + id));
        if (!address.getUserId().equals(userId)) throw new SecurityException("Access denied");

        if (request.getLabel() != null) address.setLabel(request.getLabel());
        if (request.getLine1() != null) address.setLine1(request.getLine1());
        if (request.getLine2() != null) address.setLine2(request.getLine2());
        if (request.getCity() != null) address.setCity(request.getCity());
        if (request.getState() != null) address.setState(request.getState());
        if (request.getPincode() != null) address.setPincode(request.getPincode());
        if (request.getIsDefault() != null) address.setIsDefault(request.getIsDefault());
        return toDto(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(Long id, Long userId) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Address not found: " + id));
        if (!address.getUserId().equals(userId)) throw new SecurityException("Access denied");
        addressRepository.delete(address);
    }

    public Address getAddressEntity(Long id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Address not found: " + id));
    }

    private AddressDto toDto(Address a) {
        return AddressDto.builder()
                .id(a.getId()).userId(a.getUserId()).label(a.getLabel())
                .line1(a.getLine1()).line2(a.getLine2()).city(a.getCity())
                .state(a.getState()).pincode(a.getPincode()).isDefault(a.getIsDefault())
                .build();
    }
}
