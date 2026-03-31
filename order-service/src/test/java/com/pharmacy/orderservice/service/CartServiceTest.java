package com.pharmacy.orderservice.service;

import com.pharmacy.orderservice.client.CatalogClient;
import com.pharmacy.orderservice.dto.MedicineInfo;
import com.pharmacy.orderservice.dto.StockCheckResponse;
import com.pharmacy.orderservice.entity.Cart;
import com.pharmacy.orderservice.entity.CartItem;
import com.pharmacy.orderservice.exception.InsufficientStockException;
import com.pharmacy.orderservice.repository.CartItemRepository;
import com.pharmacy.orderservice.repository.CartRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock CartRepository cartRepository;
    @Mock CartItemRepository cartItemRepository;
    @Mock CatalogClient catalogClient;
    @InjectMocks CartService cartService;

    private Cart emptyCart(Long userId) {
        return Cart.builder().id(10L).userId(userId).items(new ArrayList<>()).build();
    }

    private MedicineInfo activeMedicine(Long id) {
        MedicineInfo m = new MedicineInfo();
        m.setId(id);
        m.setName("Paracetamol");
        m.setPrice(new BigDecimal("50.00"));
        m.setRequiresPrescription(false);
        m.setActive(true);
        return m;
    }

    private StockCheckResponse available(Long batchId, int qty) {
        StockCheckResponse r = new StockCheckResponse();
        r.setBatchId(batchId);
        r.setAvailable(true);
        r.setAvailableQuantity(qty);
        return r;
    }

    // ── getCart ──────────────────────────────────────────────────────

    @Test
    void getCart_existingCart_returnsDto() {
        Cart cart = emptyCart(1L);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        var dto = cartService.getCart(1L);

        assertThat(dto.getUserId()).isEqualTo(1L);
        assertThat(dto.getItems()).isEmpty();
    }

    @Test
    void getCart_noCart_returnsEmptyDto() {
        when(cartRepository.findByUserId(99L)).thenReturn(Optional.empty());

        var dto = cartService.getCart(99L);

        assertThat(dto.getUserId()).isEqualTo(99L);
        assertThat(dto.getItems()).isEmpty();
    }

    // ── addItem ──────────────────────────────────────────────────────

    @Test
    void addItem_sufficientStock_addsNewItem() {
        Long userId = 1L, medicineId = 5L, batchId = 20L;
        Cart cart = emptyCart(userId);

        when(catalogClient.checkStock(medicineId, 2)).thenReturn(available(batchId, 10));
        when(catalogClient.getMedicineById(medicineId)).thenReturn(activeMedicine(medicineId));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndBatchId(cart.getId(), batchId)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        cartService.addItem(userId, medicineId, 2);

        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addItem_insufficientStock_throwsException() {
        StockCheckResponse noStock = new StockCheckResponse();
        noStock.setAvailable(false);
        noStock.setAvailableQuantity(0);

        when(catalogClient.checkStock(5L, 3)).thenReturn(noStock);

        assertThatThrownBy(() -> cartService.addItem(1L, 5L, 3))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void addItem_inactiveMedicine_throwsIllegalArgument() {
        MedicineInfo inactive = activeMedicine(5L);
        inactive.setActive(false);

        when(catalogClient.checkStock(5L, 1)).thenReturn(available(20L, 5));
        when(catalogClient.getMedicineById(5L)).thenReturn(inactive);

        assertThatThrownBy(() -> cartService.addItem(1L, 5L, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void addItem_existingBatch_incrementsQuantity() {
        Long userId = 1L, batchId = 20L, medicineId = 5L;
        Cart cart = emptyCart(userId);
        CartItem existing = CartItem.builder().id(1L).cart(cart).batchId(batchId).quantity(2).build();

        when(catalogClient.checkStock(medicineId, 3)).thenReturn(available(batchId, 10));
        when(catalogClient.getMedicineById(medicineId)).thenReturn(activeMedicine(medicineId));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndBatchId(cart.getId(), batchId)).thenReturn(Optional.of(existing));
        when(cartItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        cartService.addItem(userId, medicineId, 3);

        assertThat(existing.getQuantity()).isEqualTo(5);
    }

    // ── updateItem ───────────────────────────────────────────────────

    @Test
    void updateItem_quantityZero_removesItem() {
        Long userId = 1L, batchId = 20L;
        Cart cart = emptyCart(userId);
        CartItem item = CartItem.builder().id(1L).cart(cart).batchId(batchId).quantity(2).build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndBatchId(cart.getId(), batchId)).thenReturn(Optional.of(item));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        cartService.updateItem(userId, batchId, 0);

        verify(cartItemRepository).delete(item);
    }

    @Test
    void updateItem_newQuantity_updatesItem() {
        Long userId = 1L, batchId = 20L;
        Cart cart = emptyCart(userId);
        CartItem item = CartItem.builder().id(1L).cart(cart).batchId(batchId).quantity(2).build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndBatchId(cart.getId(), batchId)).thenReturn(Optional.of(item));
        when(cartItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        cartService.updateItem(userId, batchId, 5);

        assertThat(item.getQuantity()).isEqualTo(5);
    }

    @Test
    void updateItem_noCart_throwsEntityNotFound() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateItem(1L, 20L, 3))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ── removeItem ───────────────────────────────────────────────────

    @Test
    void removeItem_existingItem_deletesIt() {
        Long userId = 1L, batchId = 20L;
        Cart cart = emptyCart(userId);
        CartItem item = CartItem.builder().id(1L).cart(cart).batchId(batchId).quantity(2).build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndBatchId(cart.getId(), batchId)).thenReturn(Optional.of(item));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        cartService.removeItem(userId, batchId);

        verify(cartItemRepository).delete(item);
    }

    // ── clearCart ────────────────────────────────────────────────────

    @Test
    void clearCart_clearsAllItems() {
        Cart cart = emptyCart(1L);
        cart.getItems().add(CartItem.builder().id(1L).cart(cart).batchId(1L).quantity(1).build());

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        cartService.clearCart(1L);

        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    void clearCart_noCart_doesNothing() {
        when(cartRepository.findByUserId(99L)).thenReturn(Optional.empty());
        assertThatNoException().isThrownBy(() -> cartService.clearCart(99L));
    }
}
