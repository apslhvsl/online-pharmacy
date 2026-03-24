package com.pharmacy.orderservice.service;


import com.pharmacy.orderservice.client.CatalogClient;
import com.pharmacy.orderservice.dto.CheckoutRequest;
import com.pharmacy.orderservice.dto.OrderDto;
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
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final CatalogClient catalogClient;
    private final OrderService orderService;

    @Transactional
    public OrderDto checkout(Long userId, CheckoutRequest request) {
        // Idempotency check
        if (request.getIdempotencyKey() != null) {
            Optional<IdempotencyKey> existing =
                    idempotencyKeyRepository.findByKeyValue(request.getIdempotencyKey());
            if (existing.isPresent()) {
                return orderService.toDto(orderRepository.findById(existing.get().getOrderId())
                        .orElseThrow(() -> new EntityNotFoundException("Order not found")));
            }
        }

        // Get cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new EntityNotFoundException("Cart is empty");
        }

        // Validate prescription if needed
        if (request.getPrescriptionId() != null) {
            String status = catalogClient.getPrescriptionStatus(request.getPrescriptionId());
            if (!"APPROVED".equals(status)) {
                throw new IllegalStateException("Prescription is not approved. Current status: " + status);
            }
        }

        // Build order items and validate stock
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : cart.getItems().entrySet()) {
            Long medicineId = entry.getKey();
            Integer quantity = entry.getValue();

            // Stock check
            Map<String, Object> stockCheck = catalogClient.checkStock(medicineId, quantity);
            Boolean sufficient = (Boolean) stockCheck.get("sufficient");
            if (!sufficient) {
                throw new InsufficientStockException(
                        "Insufficient stock for medicine id: " + medicineId +
                                ". Available: " + stockCheck.get("availableQuantity")
                );
            }

            // Get medicine details
            Map<String, Object> medicine = catalogClient.getMedicineById(medicineId);
            String medicineName = (String) medicine.get("name");
            BigDecimal price = new BigDecimal(medicine.get("price").toString());
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity));

            orderItems.add(OrderItem.builder()
                    .medicineId(medicineId)
                    .medicineName(medicineName)
                    .quantity(quantity)
                    .unitPrice(price)
                    .subtotal(subtotal)
                    .build());

            total = total.add(subtotal);
        }

        // Create order
        Order order = Order.builder()
                .userId(userId)
                .status(OrderStatus.PENDING)
                .totalAmount(total)
                .prescriptionId(request.getPrescriptionId())
                .shippingAddress(request.getShippingAddress())
                .build();

        order = orderRepository.save(order);

        // Link items to order
        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }
        order.setItems(orderItems);
        order = orderRepository.save(order);

        // Save idempotency key
        if (request.getIdempotencyKey() != null) {
            idempotencyKeyRepository.save(IdempotencyKey.builder()
                    .keyValue(request.getIdempotencyKey())
                    .orderId(order.getId())
                    .build());
        }

        // Clear cart
        cart.getItems().clear();
        cartRepository.save(cart);

        return orderService.toDto(order);
    }
}