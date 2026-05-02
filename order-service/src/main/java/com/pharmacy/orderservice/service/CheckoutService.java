package com.pharmacy.orderservice.service;

import com.pharmacy.orderservice.client.CatalogClient;
import com.pharmacy.orderservice.dto.CheckoutRequest;
import com.pharmacy.orderservice.dto.CheckoutSessionDto;
import com.pharmacy.orderservice.dto.OrderDto;
import com.pharmacy.orderservice.dto.PrescriptionInfo;
import com.pharmacy.orderservice.entity.*;
import com.pharmacy.orderservice.exception.InsufficientStockException;
import com.pharmacy.orderservice.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final OrderStatusLogRepository statusLogRepository;
    private final AddressService addressService;
    private final CatalogClient catalogClient;
    private final OrderService orderService;

    private static final AtomicInteger orderCounter = new AtomicInteger(1);

    /** Step 1 — initiate checkout from cart */
    @Transactional
    public CheckoutSessionDto startCheckout(Long userId) {
        Cart cart = cartService.getCartEntity(userId);
        if (cart.getItems().isEmpty()) throw new IllegalStateException("Cart is empty");

        boolean requiresRx = cart.getItems().stream()
                .anyMatch(i -> Boolean.TRUE.equals(i.getRequiresPrescription()));

        // Cancel any existing incomplete checkout orders to prevent abandoned orders from cluttering admin panel
        List<OrderStatus> incompleteStatuses = List.of(
                OrderStatus.CHECKOUT_STARTED,
                OrderStatus.PAYMENT_PENDING
        );
        var existingOrders = orderRepository.findByUserIdAndStatusIn(
                userId,
                incompleteStatuses,
                org.springframework.data.domain.PageRequest.of(0, 100)
        ).getContent();

        for (Order existing : existingOrders) {
            OrderStatus previousStatus = existing.getStatus();
            existing.setStatus(OrderStatus.CUSTOMER_CANCELLED);
            statusLogRepository.save(OrderStatusLog.builder()
                    .order(existing)
                    .fromStatus(previousStatus.name())
                    .toStatus(OrderStatus.CUSTOMER_CANCELLED.name())
                    .changedBy(userId)
                    .note("Auto-cancelled: new checkout started")
                    .build());
            orderRepository.save(existing);
        }

        Order order = Order.builder()
                .userId(userId)
                .status(OrderStatus.CHECKOUT_STARTED)
                .build();
        order = orderRepository.save(order);

        // Generate order number
        order.setOrderNumber("RX-" + order.getCreatedAt().getYear() + "-" + String.format("%05d", order.getId()));
        order = orderRepository.save(order);

        return CheckoutSessionDto.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .requiresPrescription(requiresRx)
                .build();
    }

    /** Step 2 — set delivery address */
    @Transactional
    public OrderDto setAddress(Long orderId, Long userId, CheckoutRequest request) {
        Order order = getOwnedOrder(orderId, userId);

        Long addressId = (request.getAddressId() != null && request.getAddressId() > 0) ? request.getAddressId() : null;
        if (addressId == null && request.getInlineAddress() != null) {
            var saved = addressService.addAddress(userId, request.getInlineAddress());
            addressId = saved.getId();
        }
        if (addressId == null) throw new IllegalArgumentException("Address is required");

        order.setAddressId(addressId);
        return orderService.toDto(orderRepository.save(order));
    }

    /** Step 3 — link approved prescription */
    @Transactional
    public OrderDto linkPrescription(Long orderId, Long userId, Long prescriptionId) {
        Order order = getOwnedOrder(orderId, userId);
        order.setPrescriptionId(prescriptionId);
        order.setStatus(OrderStatus.PRESCRIPTION_APPROVED);
        return orderService.toDto(orderRepository.save(order));
    }

    /** Step 4 — confirm order: validate stock availability, snapshot prices */
    @Transactional
    public OrderDto confirmOrder(Long orderId, Long userId) {
        Order order = getOwnedOrder(orderId, userId);
        Cart cart = cartService.getCartEntity(userId);

        if (cart.getItems().isEmpty()) throw new IllegalStateException("Cart is empty");

        // ── Stock validation & order item snapshot ────────────────────
        // Prescription enforcement is intentionally deferred to admin approval.
        // Customers may order Rx items and pay; admin reviews and approves/rejects.
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            var stockCheck = catalogClient.checkBatchStock(cartItem.getBatchId(), cartItem.getQuantity());
            if (!Boolean.TRUE.equals(stockCheck.getAvailable())) {
                throw new InsufficientStockException("Insufficient stock for: " + cartItem.getMedicineName());
            }
            BigDecimal lineTotal = cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            orderItems.add(OrderItem.builder()
                    .order(order)
                    .batchId(cartItem.getBatchId())
                    .medicineId(cartItem.getMedicineId())
                    .medicineName(cartItem.getMedicineName())
                    .unitPrice(cartItem.getUnitPrice())
                    .quantity(cartItem.getQuantity())
                    .requiresPrescription(cartItem.getRequiresPrescription())
                    .lineTotal(lineTotal)
                    .build());
            subtotal = subtotal.add(lineTotal);
        }

        BigDecimal taxAmount = subtotal.multiply(new BigDecimal("0.05"));
        BigDecimal deliveryCharge = subtotal.compareTo(new BigDecimal("500")) >= 0 ? BigDecimal.ZERO : new BigDecimal("50");
        BigDecimal total = subtotal.add(taxAmount).add(deliveryCharge);

        order.setItems(orderItems);
        order.setSubtotal(subtotal);
        order.setTaxAmount(taxAmount);
        order.setDeliveryCharge(deliveryCharge);
        order.setTotalAmount(total);
        order.setStatus(OrderStatus.PAYMENT_PENDING);

        // Stock is NOT deducted here — deduction happens when admin approves (PENDING_APPROVAL → PACKED)
        // Cart is NOT cleared here — it is cleared after successful payment so the user
        // can refresh the payment page without losing their cart data.
        order = orderRepository.save(order);

        return orderService.toDto(order);
    }

    private Order getOwnedOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
        if (!order.getUserId().equals(userId)) throw new EntityNotFoundException("Order not found: " + orderId);
        return order;
    }
}
