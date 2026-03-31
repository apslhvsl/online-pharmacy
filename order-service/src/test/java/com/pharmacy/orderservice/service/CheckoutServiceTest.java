package com.pharmacy.orderservice.service;

import com.pharmacy.orderservice.client.CatalogClient;
import com.pharmacy.orderservice.dto.*;
import com.pharmacy.orderservice.entity.*;
import com.pharmacy.orderservice.exception.InsufficientStockException;
import com.pharmacy.orderservice.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock CartService cartService;
    @Mock OrderRepository orderRepository;
    @Mock OrderStatusLogRepository statusLogRepository;
    @Mock AddressService addressService;
    @Mock CatalogClient catalogClient;
    @Mock OrderService orderService;
    @InjectMocks CheckoutService checkoutService;

    private Cart cartWithItem(boolean requiresRx) {
        Cart cart = Cart.builder().id(10L).userId(1L).items(new ArrayList<>()).build();
        CartItem item = CartItem.builder()
                .id(1L).cart(cart).batchId(20L).medicineId(5L)
                .medicineName("Paracetamol").unitPrice(new BigDecimal("100.00"))
                .quantity(2).requiresPrescription(requiresRx)
                .build();
        cart.getItems().add(item);
        return cart;
    }

    private Order savedOrder(Long id, Long userId, OrderStatus status) {
        Order o = new Order();
        o.setId(id);
        o.setUserId(userId);
        o.setStatus(status);
        o.setCreatedAt(LocalDateTime.now());
        o.setUpdatedAt(LocalDateTime.now());
        return o;
    }

    // ── startCheckout ────────────────────────────────────────────────

    @Test
    void startCheckout_emptyCart_throwsIllegalState() {
        Cart emptyCart = Cart.builder().userId(1L).items(new ArrayList<>()).build();
        when(cartService.getCartEntity(1L)).thenReturn(emptyCart);

        assertThatThrownBy(() -> checkoutService.startCheckout(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cart is empty");
    }

    @Test
    void startCheckout_noCart_throwsEntityNotFound() {
        when(cartService.getCartEntity(1L))
                .thenThrow(new EntityNotFoundException("Cart is empty"));

        assertThatThrownBy(() -> checkoutService.startCheckout(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void startCheckout_withItems_createsOrder() {
        Cart cart = cartWithItem(false);
        Order order = savedOrder(1L, 1L, OrderStatus.CHECKOUT_STARTED);
        order.setCreatedAt(LocalDateTime.now());

        when(cartService.getCartEntity(1L)).thenReturn(cart);
        when(orderRepository.save(any())).thenReturn(order);

        var session = checkoutService.startCheckout(1L);

        assertThat(session.getOrderId()).isEqualTo(1L);
        assertThat(session.getStatus()).isEqualTo(OrderStatus.CHECKOUT_STARTED);
        assertThat(session.getRequiresPrescription()).isFalse();
    }

    @Test
    void startCheckout_rxItem_flagsRequiresPrescription() {
        Cart cart = cartWithItem(true);
        Order order = savedOrder(1L, 1L, OrderStatus.CHECKOUT_STARTED);
        order.setCreatedAt(LocalDateTime.now());

        when(cartService.getCartEntity(1L)).thenReturn(cart);
        when(orderRepository.save(any())).thenReturn(order);

        var session = checkoutService.startCheckout(1L);

        assertThat(session.getRequiresPrescription()).isTrue();
    }

    // ── setAddress ───────────────────────────────────────────────────

    @Test
    void setAddress_withAddressId_setsAddress() {
        Order order = savedOrder(1L, 1L, OrderStatus.CHECKOUT_STARTED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderService.toDto(any())).thenReturn(new OrderDto());

        CheckoutRequest req = new CheckoutRequest();
        req.setAddressId(5L);

        checkoutService.setAddress(1L, 1L, req);

        assertThat(order.getAddressId()).isEqualTo(5L);
    }

    @Test
    void setAddress_noAddress_throwsIllegalArgument() {
        Order order = savedOrder(1L, 1L, OrderStatus.CHECKOUT_STARTED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        CheckoutRequest req = new CheckoutRequest();
        // no addressId, no inlineAddress

        assertThatThrownBy(() -> checkoutService.setAddress(1L, 1L, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Address is required");
    }

    @Test
    void setAddress_wrongUser_throwsEntityNotFound() {
        Order order = savedOrder(1L, 10L, OrderStatus.CHECKOUT_STARTED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        CheckoutRequest req = new CheckoutRequest();
        req.setAddressId(5L);

        assertThatThrownBy(() -> checkoutService.setAddress(1L, 99L, req))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ── linkPrescription ─────────────────────────────────────────────

    @Test
    void linkPrescription_setsIdAndStatus() {
        Order order = savedOrder(1L, 1L, OrderStatus.CHECKOUT_STARTED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderService.toDto(any())).thenReturn(new OrderDto());

        checkoutService.linkPrescription(1L, 1L, 77L);

        assertThat(order.getPrescriptionId()).isEqualTo(77L);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PRESCRIPTION_APPROVED);
    }

    // ── confirmOrder ─────────────────────────────────────────────────

    @Test
    void confirmOrder_noRx_sufficientStock_confirmsOrder() {
        Order order = savedOrder(1L, 1L, OrderStatus.CHECKOUT_STARTED);
        Cart cart = cartWithItem(false);

        StockCheckResponse inStock = new StockCheckResponse();
        inStock.setAvailable(true);
        inStock.setAvailableQuantity(10);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(cartService.getCartEntity(1L)).thenReturn(cart);
        when(catalogClient.checkBatchStock(20L, 2)).thenReturn(inStock);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderService.toDto(any())).thenReturn(new OrderDto());

        checkoutService.confirmOrder(1L, 1L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        verify(catalogClient).deductBatchStock(20L, 2);
        verify(cartService).clearCart(1L);
    }

    @Test
    void confirmOrder_insufficientStock_throwsException() {
        Order order = savedOrder(1L, 1L, OrderStatus.CHECKOUT_STARTED);
        Cart cart = cartWithItem(false);

        StockCheckResponse noStock = new StockCheckResponse();
        noStock.setAvailable(false);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(cartService.getCartEntity(1L)).thenReturn(cart);
        when(catalogClient.checkBatchStock(20L, 2)).thenReturn(noStock);

        assertThatThrownBy(() -> checkoutService.confirmOrder(1L, 1L))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void confirmOrder_rxRequired_noPrescriptionLinked_throwsIllegalState() {
        Order order = savedOrder(1L, 1L, OrderStatus.CHECKOUT_STARTED);
        // no prescriptionId set
        Cart cart = cartWithItem(true);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(cartService.getCartEntity(1L)).thenReturn(cart);

        assertThatThrownBy(() -> checkoutService.confirmOrder(1L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("prescription");
    }

    @Test
    void confirmOrder_rxRequired_prescriptionNotApproved_throwsIllegalState() {
        Order order = savedOrder(1L, 1L, OrderStatus.CHECKOUT_STARTED);
        order.setPrescriptionId(77L);
        Cart cart = cartWithItem(true);

        PrescriptionInfo rx = new PrescriptionInfo();
        rx.setId(77L);
        rx.setUserId(1L);
        rx.setStatus("PENDING");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(cartService.getCartEntity(1L)).thenReturn(cart);
        when(catalogClient.getPrescriptionById(77L)).thenReturn(rx);

        assertThatThrownBy(() -> checkoutService.confirmOrder(1L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not approved");
    }

    @Test
    void confirmOrder_rxRequired_expiredPrescription_throwsIllegalState() {
        Order order = savedOrder(1L, 1L, OrderStatus.CHECKOUT_STARTED);
        order.setPrescriptionId(77L);
        Cart cart = cartWithItem(true);

        PrescriptionInfo rx = new PrescriptionInfo();
        rx.setId(77L);
        rx.setUserId(1L);
        rx.setStatus("APPROVED");
        rx.setValidTill(LocalDateTime.now().minusDays(1));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(cartService.getCartEntity(1L)).thenReturn(cart);
        when(catalogClient.getPrescriptionById(77L)).thenReturn(rx);

        assertThatThrownBy(() -> checkoutService.confirmOrder(1L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void confirmOrder_emptyCart_throwsIllegalState() {
        Order order = savedOrder(1L, 1L, OrderStatus.CHECKOUT_STARTED);
        Cart emptyCart = Cart.builder().id(10L).userId(1L).items(new ArrayList<>()).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(cartService.getCartEntity(1L)).thenReturn(emptyCart);

        assertThatThrownBy(() -> checkoutService.confirmOrder(1L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cart is empty");
    }

    @Test
    void confirmOrder_calculatesCorrectTotals() {
        Order order = savedOrder(1L, 1L, OrderStatus.CHECKOUT_STARTED);
        Cart cart = cartWithItem(false); // 2 x 100.00 = 200.00

        StockCheckResponse inStock = new StockCheckResponse();
        inStock.setAvailable(true);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(cartService.getCartEntity(1L)).thenReturn(cart);
        when(catalogClient.checkBatchStock(20L, 2)).thenReturn(inStock);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderService.toDto(any())).thenReturn(new OrderDto());

        checkoutService.confirmOrder(1L, 1L);

        // subtotal=200, tax=10 (5%), delivery=50 (under 500), total=260
        assertThat(order.getSubtotal()).isEqualByComparingTo("200.00");
        assertThat(order.getTaxAmount()).isEqualByComparingTo("10.00");
        assertThat(order.getDeliveryCharge()).isEqualByComparingTo("50");
        assertThat(order.getTotalAmount()).isEqualByComparingTo("260.00");
    }
}
