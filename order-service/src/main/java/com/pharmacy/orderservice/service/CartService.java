package com.pharmacy.orderservice.service;


import com.pharmacy.orderservice.dto.CartDto;
import com.pharmacy.orderservice.entity.Cart;
import com.pharmacy.orderservice.repository.CartRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    public CartDto getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(Cart.builder().userId(userId).build());
        return toDto(cart);
    }

    public CartDto addItem(Long userId, Long medicineId, Integer quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(Cart.builder().userId(userId).build());
        cart.getItems().merge(medicineId, quantity, Integer::sum);
        return toDto(cartRepository.save(cart));
    }

    public CartDto removeItem(Long userId, Long medicineId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found"));
        cart.getItems().remove(medicineId);
        return toDto(cartRepository.save(cart));
    }

    public void clearCart(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }

    private CartDto toDto(Cart cart) {
        return CartDto.builder()
                .userId(cart.getUserId())
                .items(cart.getItems())
                .build();
    }
}