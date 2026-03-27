package com.pharmacy.orderservice.service;

import com.pharmacy.orderservice.client.CatalogClient;
import com.pharmacy.orderservice.dto.CartDto;
import com.pharmacy.orderservice.dto.CartItemDto;
import com.pharmacy.orderservice.dto.MedicineInfo;
import com.pharmacy.orderservice.entity.Cart;
import com.pharmacy.orderservice.entity.CartItem;
import com.pharmacy.orderservice.exception.InsufficientStockException;
import com.pharmacy.orderservice.repository.CartItemRepository;
import com.pharmacy.orderservice.repository.CartRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CatalogClient catalogClient;

    public CartDto getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> Cart.builder().userId(userId).build());
        return toDto(cart);
    }

    @Transactional
    public CartDto addItem(Long userId, Long batchId, Integer quantity) {
        // Validate batch stock via catalog
        var stockCheck = catalogClient.checkBatchStock(batchId, quantity);
        if (!Boolean.TRUE.equals(stockCheck.getAvailable())) {
            throw new InsufficientStockException("Insufficient stock. Available: " + stockCheck.getAvailableQuantity());
        }

        // Fetch medicine info for name/price snapshot (using medicineId from stock check)
        Long medicineId = stockCheck.getMedicineId();
        MedicineInfo medicine = catalogClient.getMedicineById(medicineId);
        if (!Boolean.TRUE.equals(medicine.getActive())) {
            throw new IllegalArgumentException("Medicine is not available");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(Cart.builder().userId(userId).build()));

        cartItemRepository.findByCartIdAndBatchId(cart.getId(), batchId)
                .ifPresentOrElse(
                        item -> {
                            item.setQuantity(item.getQuantity() + quantity);
                            cartItemRepository.save(item);
                        },
                        () -> cartItemRepository.save(CartItem.builder()
                                .cart(cart)
                                .batchId(batchId)
                                .medicineId(medicineId)
                                .medicineName(medicine.getName())
                                .unitPrice(medicine.getPrice())
                                .quantity(quantity)
                                .requiresPrescription(medicine.getRequiresPrescription())
                                .build())
                );

        return toDto(cartRepository.findByUserId(userId).orElseThrow());
    }

    @Transactional
    public CartDto updateItem(Long userId, Long batchId, Integer quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found"));

        if (quantity == 0) {
            cartItemRepository.findByCartIdAndBatchId(cart.getId(), batchId)
                    .ifPresent(cartItemRepository::delete);
        } else {
            CartItem item = cartItemRepository.findByCartIdAndBatchId(cart.getId(), batchId)
                    .orElseThrow(() -> new EntityNotFoundException("Item not in cart"));
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        return toDto(cartRepository.findByUserId(userId).orElseThrow());
    }

    @Transactional
    public CartDto removeItem(Long userId, Long batchId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found"));
        cartItemRepository.findByCartIdAndBatchId(cart.getId(), batchId)
                .ifPresent(cartItemRepository::delete);
        return toDto(cartRepository.findByUserId(userId).orElseThrow());
    }

    @Transactional
    public void clearCart(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }

    public Cart getCartEntity(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Cart is empty"));
    }

    private CartDto toDto(Cart cart) {
        List<CartItemDto> itemDtos = cart.getItems().stream().map(i -> CartItemDto.builder()
                .batchId(i.getBatchId())
                .medicineId(i.getMedicineId())
                .medicineName(i.getMedicineName())
                .unitPrice(i.getUnitPrice())
                .quantity(i.getQuantity())
                .requiresPrescription(i.getRequiresPrescription())
                .lineTotal(i.getUnitPrice() != null ? i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())) : BigDecimal.ZERO)
                .build()).toList();

        BigDecimal subTotal = itemDtos.stream()
                .map(CartItemDto::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxAmount = subTotal.multiply(new BigDecimal("0.05")); // 5% GST
        boolean requiresRx = itemDtos.stream().anyMatch(i -> Boolean.TRUE.equals(i.getRequiresPrescription()));

        return CartDto.builder()
                .userId(cart.getUserId())
                .items(itemDtos)
                .subTotal(subTotal)
                .taxAmount(taxAmount)
                .total(subTotal.add(taxAmount))
                .requiresPrescription(requiresRx)
                .build();
    }
}
