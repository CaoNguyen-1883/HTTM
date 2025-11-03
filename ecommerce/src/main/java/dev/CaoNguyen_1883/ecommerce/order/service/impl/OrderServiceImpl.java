package dev.CaoNguyen_1883.ecommerce.order.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.CaoNguyen_1883.ecommerce.cart.entity.Cart;
import dev.CaoNguyen_1883.ecommerce.cart.entity.CartItem;
import dev.CaoNguyen_1883.ecommerce.cart.repository.CartRepository;
import dev.CaoNguyen_1883.ecommerce.cart.service.ICartService;
import dev.CaoNguyen_1883.ecommerce.common.exception.BadRequestException;
import dev.CaoNguyen_1883.ecommerce.common.exception.ForbiddenException;
import dev.CaoNguyen_1883.ecommerce.common.exception.ResourceNotFoundException;
import dev.CaoNguyen_1883.ecommerce.order.dto.*;
import dev.CaoNguyen_1883.ecommerce.order.entity.Order;
import dev.CaoNguyen_1883.ecommerce.order.entity.OrderItem;
import dev.CaoNguyen_1883.ecommerce.order.entity.OrderStatus;
import dev.CaoNguyen_1883.ecommerce.order.entity.PaymentStatus;
import dev.CaoNguyen_1883.ecommerce.order.mapper.OrderMapper;
import dev.CaoNguyen_1883.ecommerce.order.repository.OrderItemRepository;
import dev.CaoNguyen_1883.ecommerce.order.repository.OrderRepository;
import dev.CaoNguyen_1883.ecommerce.order.service.IOrderService;
import dev.CaoNguyen_1883.ecommerce.product.entity.Product;
import dev.CaoNguyen_1883.ecommerce.product.entity.ProductVariant;
import dev.CaoNguyen_1883.ecommerce.product.repository.ProductVariantRepository;
import dev.CaoNguyen_1883.ecommerce.user.entity.User;
import dev.CaoNguyen_1883.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository variantRepository;
    private final ICartService cartService;
    private final OrderMapper orderMapper;
    private final ObjectMapper objectMapper;

    // Thread-safe counter for order number generation
    private static final AtomicLong orderCounter = new AtomicLong(0);

    @Override
    @Transactional
    public OrderDto createOrderFromCart(UUID userId, CreateOrderRequest request) {
        log.debug("Creating order from cart for user: {}", userId);

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Get cart with items
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new BadRequestException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty. Cannot create order.");
        }

        // Validate all items are still available
        for (CartItem cartItem : cart.getItems()) {
            ProductVariant variant = cartItem.getVariant();
            
            if (!variant.getIsActive() || !variant.getProduct().getIsActive()) {
                throw new BadRequestException("Product '" + variant.getProduct().getName() + "' is no longer available");
            }

            if (variant.getAvailableStock() < cartItem.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product: " + variant.getProduct().getName());
            }
        }

        // Create order
        Order order = Order.builder()
                .user(user)
                .orderNumber(generateOrderNumber())
                .paymentMethod(request.getPaymentMethod())
                .shippingAddress(request.getShippingAddress())
                .shippingCity(request.getShippingCity())
                .shippingDistrict(request.getShippingDistrict())
                .shippingWard(request.getShippingWard())
                .shippingPhone(request.getShippingPhone())
                .shippingRecipient(request.getShippingRecipient())
                .billingAddress(request.getBillingAddress() != null ? request.getBillingAddress() : request.getShippingAddress())
                .billingCity(request.getBillingCity() != null ? request.getBillingCity() : request.getShippingCity())
                .billingDistrict(request.getBillingDistrict() != null ? request.getBillingDistrict() : request.getShippingDistrict())
                .billingWard(request.getBillingWard() != null ? request.getBillingWard() : request.getShippingWard())
                .notes(request.getNotes())
                .subtotal(BigDecimal.ZERO)
                .shippingFee(calculateShippingFee(request.getShippingCity()))
                .tax(BigDecimal.ZERO)
                .discount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .build();

        // Convert cart items to order items
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            ProductVariant variant = cartItem.getVariant();
            Product product = variant.getProduct();

            // Create product snapshot
            String productSnapshot = createProductSnapshot(product, variant);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .variant(variant)
                    .productName(product.getName())
                    .variantSku(variant.getSku())
                    .variantName(variant.getName())
                    .productImage(getPrimaryImageUrl(variant))
                    .productSnapshot(productSnapshot)
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getPriceAtAdd())
                    .subtotal(cartItem.getSubtotal())
                    .build();

            order.addItem(orderItem);
            subtotal = subtotal.add(orderItem.getSubtotal());

            // Decrease stock (convert reserved to sold)
            variant.decreaseStock(cartItem.getQuantity());

            // Update product purchase count for recommendation system
            product.setPurchaseCount(product.getPurchaseCount() + cartItem.getQuantity());
        }

        // Calculate totals
        order.setSubtotal(subtotal);
        order.calculateTotalAmount();

        // Save order
        order = orderRepository.save(order);

        // Save variants with updated stock
        for (CartItem cartItem : cart.getItems()) {
            variantRepository.save(cartItem.getVariant());
        }

        // Clear cart
        cartService.clearCart(userId);

        log.info("Order created successfully. Order number: {}, User: {}, Total: {}",
                order.getOrderNumber(), userId, order.getTotalAmount());

        return orderMapper.toDto(order);
    }

    @Override
    public OrderDto getOrderById(UUID orderId) {
        log.debug("Fetching order by ID: {}", orderId);

        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        return orderMapper.toDto(order);
    }

    @Override
    public OrderDto getOrderByOrderNumber(String orderNumber) {
        log.debug("Fetching order by order number: {}", orderNumber);

        Order order = orderRepository.findByOrderNumberWithDetails(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));

        return orderMapper.toDto(order);
    }

    @Override
    public Page<OrderDto> getUserOrders(UUID userId, Pageable pageable) {
        log.debug("Fetching orders for user: {}", userId);

        return orderRepository.findByUserId(userId, pageable)
                .map(orderMapper::toDto);
    }

    @Override
    public Page<OrderDto> getAllOrders(Pageable pageable) {
        log.debug("Fetching all orders");

        return orderRepository.findAll(pageable)
                .map(orderMapper::toDto);
    }

    @Override
    public Page<OrderDto> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        log.debug("Fetching orders by status: {}", status);

        return orderRepository.findByOrderStatus(status, pageable)
                .map(orderMapper::toDto);
    }

    @Override
    public Page<OrderDto> getOrdersByPaymentStatus(PaymentStatus status, Pageable pageable) {
        log.debug("Fetching orders by payment status: {}", status);

        return orderRepository.findByPaymentStatus(status, pageable)
                .map(orderMapper::toDto);
    }

    @Override
    public Page<OrderDto> getPendingOrders(Pageable pageable) {
        log.debug("Fetching pending orders");

        return orderRepository.findPendingOrders(pageable)
                .map(orderMapper::toDto);
    }

    @Override
    public Page<OrderDto> searchOrders(String keyword, Pageable pageable) {
        log.debug("Searching orders with keyword: {}", keyword);

        return orderRepository.searchOrders(keyword, pageable)
                .map(orderMapper::toDto);
    }

    @Override
    public Page<OrderDto> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.debug("Fetching orders by date range: {} to {}", startDate, endDate);

        return orderRepository.findByDateRange(startDate, endDate, pageable)
                .map(orderMapper::toDto);
    }

    @Override
    @Transactional
    public OrderDto confirmOrder(UUID orderId, UUID confirmedBy) {
        log.debug("Confirming order: {}", orderId);

        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        User confirmedByUser = userRepository.findById(confirmedBy)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", confirmedBy));

        order.confirm(confirmedByUser);
        order = orderRepository.save(order);

        log.info("Order confirmed. Order number: {}, Confirmed by: {}",
                order.getOrderNumber(), confirmedByUser.getEmail());

        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public OrderDto markAsProcessing(UUID orderId) {
        log.debug("Marking order as processing: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.markAsProcessing();
        order = orderRepository.save(order);

        log.info("Order marked as processing. Order number: {}", order.getOrderNumber());

        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public OrderDto shipOrder(UUID orderId) {
        log.debug("Shipping order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.ship();
        order = orderRepository.save(order);

        log.info("Order shipped. Order number: {}", order.getOrderNumber());

        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public OrderDto deliverOrder(UUID orderId) {
        log.debug("Delivering order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.deliver();
        order = orderRepository.save(order);

        log.info("Order delivered. Order number: {}", order.getOrderNumber());

        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public OrderDto cancelOrder(UUID orderId, CancelOrderRequest request, UUID cancelledBy) {
        log.debug("Cancelling order: {}", orderId);

        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        User cancelledByUser = userRepository.findById(cancelledBy)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", cancelledBy));

        // Release stock back
        for (OrderItem item : order.getItems()) {
            if (item.getVariant() != null) {
                ProductVariant variant = item.getVariant();
                variant.increaseStock(item.getQuantity());
                variantRepository.save(variant);

                // Decrease purchase count
                Product product = variant.getProduct();
                product.setPurchaseCount(Math.max(0, product.getPurchaseCount() - item.getQuantity()));
            }
        }

        order.cancel(request.getReason(), cancelledByUser);
        order = orderRepository.save(order);

        log.info("Order cancelled. Order number: {}, Cancelled by: {}, Reason: {}",
                order.getOrderNumber(), cancelledByUser.getEmail(), request.getReason());

        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public OrderDto updateOrderStatus(UUID orderId, UpdateOrderStatusRequest request, UUID updatedBy) {
        log.debug("Updating order status: {} to {}", orderId, request.getStatus());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        User updatedByUser = userRepository.findById(updatedBy)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", updatedBy));

        // Update status based on request
        switch (request.getStatus()) {
            case CONFIRMED:
                order.confirm(updatedByUser);
                break;
            case PROCESSING:
                order.markAsProcessing();
                break;
            case SHIPPED:
                order.ship();
                break;
            case DELIVERED:
                order.deliver();
                break;
            case CANCELLED:
                throw new BadRequestException("Use cancel order endpoint to cancel order");
            default:
                throw new BadRequestException("Invalid order status");
        }

        if (request.getNotes() != null) {
            order.setAdminNotes(request.getNotes());
        }

        order = orderRepository.save(order);

        log.info("Order status updated. Order number: {}, New status: {}",
                order.getOrderNumber(), request.getStatus());

        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public OrderDto markPaymentAsPaid(UUID orderId) {
        log.debug("Marking payment as paid for order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.markPaymentAsPaid();
        order = orderRepository.save(order);

        log.info("Payment marked as paid. Order number: {}", order.getOrderNumber());

        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public OrderDto markPaymentAsFailed(UUID orderId) {
        log.debug("Marking payment as failed for order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.markPaymentAsFailed();
        order = orderRepository.save(order);

        log.info("Payment marked as failed. Order number: {}", order.getOrderNumber());

        return orderMapper.toDto(order);
    }

    @Override
    public OrderStatistics getOrderStatistics() {
        log.debug("Fetching order statistics");

        return OrderStatistics.builder()
                .totalOrders(orderRepository.count())
                .pendingOrders(orderRepository.countByOrderStatus(OrderStatus.PENDING))
                .confirmedOrders(orderRepository.countByOrderStatus(OrderStatus.CONFIRMED))
                .processingOrders(orderRepository.countByOrderStatus(OrderStatus.PROCESSING))
                .shippedOrders(orderRepository.countByOrderStatus(OrderStatus.SHIPPED))
                .deliveredOrders(orderRepository.countByOrderStatus(OrderStatus.DELIVERED))
                .cancelledOrders(orderRepository.countByOrderStatus(OrderStatus.CANCELLED))
                .totalRevenue(orderRepository.getTotalRevenue())
                .build();
    }

    @Override
    public Double getTotalRevenue() {
        return orderRepository.getTotalRevenue();
    }

    @Override
    public Double getRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.getTotalRevenueByDateRange(startDate, endDate);
    }

    // ===== HELPER METHODS =====

    private String generateOrderNumber() {
        // Format: ORD-YYYYMMDD-XXXXX
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long counter = orderCounter.incrementAndGet();
        String orderNumber = String.format("ORD-%s-%05d", datePart, counter % 100000);

        // Ensure uniqueness
        while (orderRepository.existsByOrderNumber(orderNumber)) {
            counter = orderCounter.incrementAndGet();
            orderNumber = String.format("ORD-%s-%05d", datePart, counter % 100000);
        }

        return orderNumber;
    }

    private BigDecimal calculateShippingFee(String city) {
        // Simple shipping fee calculation based on city
        // In real app, use shipping provider API
        if (city != null && (city.equalsIgnoreCase("Hanoi") || city.equalsIgnoreCase("Ho Chi Minh"))) {
            return new BigDecimal("30000"); // 30k for major cities
        }
        return new BigDecimal("50000"); // 50k for other cities
    }

    private String getPrimaryImageUrl(ProductVariant variant) {
        if (variant.getImages() != null && !variant.getImages().isEmpty()) {
            return variant.getImages().stream()
                    .filter(img -> img.getIsPrimary() != null && img.getIsPrimary())
                    .findFirst()
                    .map(img -> img.getImageUrl())
                    .orElseGet(() -> variant.getImages().stream()
                            .findFirst()
                            .map(img -> img.getImageUrl())
                            .orElse(null));
        }
        return null;
    }

    private String createProductSnapshot(Product product, ProductVariant variant) {
        // Create JSON snapshot for ML/analytics
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("productId", product.getId().toString());
        snapshot.put("productName", product.getName());
        snapshot.put("categoryId", product.getCategory() != null ? product.getCategory().getId().toString() : null);
        snapshot.put("categoryName", product.getCategory() != null ? product.getCategory().getName() : null);
        snapshot.put("brandId", product.getBrand() != null ? product.getBrand().getId().toString() : null);
        snapshot.put("brandName", product.getBrand() != null ? product.getBrand().getName() : null);
        snapshot.put("variantId", variant.getId().toString());
        snapshot.put("variantName", variant.getName());

        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            log.error("Error creating product snapshot", e);
            return "{}";
        }
    }
}