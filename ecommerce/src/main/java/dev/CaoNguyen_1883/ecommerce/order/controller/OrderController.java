package dev.CaoNguyen_1883.ecommerce.order.controller;

import dev.CaoNguyen_1883.ecommerce.auth.security.CustomUserDetails;
import dev.CaoNguyen_1883.ecommerce.common.response.ApiResponse;
import dev.CaoNguyen_1883.ecommerce.order.dto.*;
import dev.CaoNguyen_1883.ecommerce.order.entity.OrderStatus;
import dev.CaoNguyen_1883.ecommerce.order.entity.PaymentStatus;
import dev.CaoNguyen_1883.ecommerce.order.service.IOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "APIs for managing orders")
@SecurityRequirement(name = "bearer-jwt")
public class OrderController {

    private final IOrderService orderService;

    // ===== CUSTOMER ENDPOINTS =====

    @Operation(
            summary = "Create order from cart",
            description = "Create a new order from the current user's cart items"
    )
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderDto>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getId();

        OrderDto order = orderService.createOrderFromCart(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Order created successfully", order));
    }

    @Operation(
            summary = "Get my orders",
            description = "Get all orders of the current user"
    )
    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<OrderDto>>> getMyOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getId();

        Page<OrderDto> orders = orderService.getUserOrders(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orders));
    }

    @Operation(
            summary = "Get order by ID",
            description = "Get order details by order ID"
    )
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER', 'STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderById(
            @PathVariable UUID orderId,
            Authentication authentication) {
        OrderDto order = orderService.getOrderById(orderId);

        // Verify customer can only access their own orders
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        if (userDetails.hasRole("CUSTOMER") && !order.getUserId().equals(userDetails.getId())) {
            return ResponseEntity.status(403)
                    .body(ApiResponse   .error("You don't have permission to access this order", 403));
        }

        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", order));
    }

    @Operation(
            summary = "Get order by order number",
            description = "Get order details by order number"
    )
    @GetMapping("/number/{orderNumber}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER', 'STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderByOrderNumber(
            @PathVariable String orderNumber,
            Authentication authentication) {
        OrderDto order = orderService.getOrderByOrderNumber(orderNumber);

        // Verify customer can only access their own orders
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        if (userDetails.hasRole("CUSTOMER") && !order.getUserId().equals(userDetails.getId())) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("You don't have permission to access this order", 403));
        }

        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", order));
    }

    @Operation(
            summary = "Cancel order",
            description = "Cancel an order (only for PENDING or CONFIRMED status)"
    )
    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderDto>> cancelOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody CancelOrderRequest request,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Verify customer can only cancel their own orders
        if (userDetails.hasRole("CUSTOMER")) {
            OrderDto order = orderService.getOrderById(orderId);
            if (!order.getUserId().equals(userDetails.getId())) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("You don't have permission to cancel this order", 403));
            }
        }

        OrderDto order = orderService.cancelOrder(orderId, request, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", order));
    }

    // ===== ADMIN/STAFF ENDPOINTS =====

    @Operation(
            summary = "Get all orders",
            description = "Get all orders (Admin/Staff only)"
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderDto>>> getAllOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<OrderDto> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orders));
    }

    @Operation(
            summary = "Get orders by status",
            description = "Filter orders by order status"
    )
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderDto>>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<OrderDto> orders = orderService.getOrdersByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orders));
    }

    @Operation(
            summary = "Get orders by payment status",
            description = "Filter orders by payment status"
    )
    @GetMapping("/payment-status/{status}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderDto>>> getOrdersByPaymentStatus(
            @PathVariable PaymentStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<OrderDto> orders = orderService.getOrdersByPaymentStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orders));
    }

    @Operation(
            summary = "Get pending orders",
            description = "Get all pending orders that need confirmation"
    )
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderDto>>> getPendingOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<OrderDto> orders = orderService.getPendingOrders(pageable);
        return ResponseEntity.ok(ApiResponse.success("Pending orders retrieved successfully", orders));
    }

    @Operation(
            summary = "Search orders",
            description = "Search orders by order number, customer email, phone, or recipient name"
    )
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderDto>>> searchOrders(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<OrderDto> orders = orderService.searchOrders(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success("Search completed successfully", orders));
    }

    @Operation(
            summary = "Get orders by date range",
            description = "Get orders created within a specific date range"
    )
    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderDto>>> getOrdersByDateRange(
            @Parameter(description = "Start date (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<OrderDto> orders = orderService.getOrdersByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orders));
    }

    @Operation(
            summary = "Confirm order",
            description = "Confirm a pending order (Admin/Staff only)"
    )
    @PostMapping("/{orderId}/confirm")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderDto>> confirmOrder(
            @PathVariable UUID orderId,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID confirmedBy = userDetails.getId();

        OrderDto order = orderService.confirmOrder(orderId, confirmedBy);
        return ResponseEntity.ok(ApiResponse.success("Order confirmed successfully", order));
    }

    @Operation(
            summary = "Mark as processing",
            description = "Mark order as processing (Admin/Staff only)"
    )
    @PostMapping("/{orderId}/processing")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderDto>> markAsProcessing(
            @PathVariable UUID orderId) {
        OrderDto order = orderService.markAsProcessing(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order marked as processing", order));
    }

    @Operation(
            summary = "Ship order",
            description = "Mark order as shipped (Admin/Staff only)"
    )
    @PostMapping("/{orderId}/ship")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderDto>> shipOrder(
            @PathVariable UUID orderId) {
        OrderDto order = orderService.shipOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order shipped successfully", order));
    }

    @Operation(
            summary = "Deliver order",
            description = "Mark order as delivered (Admin/Staff only)"
    )
    @PostMapping("/{orderId}/deliver")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderDto>> deliverOrder(
            @PathVariable UUID orderId) {
        OrderDto order = orderService.deliverOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order delivered successfully", order));
    }

    @Operation(
            summary = "Update order status",
            description = "Update order status (Admin/Staff only)"
    )
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderDto>> updateOrderStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID updatedBy = userDetails.getId();

        OrderDto order = orderService.updateOrderStatus(orderId, request, updatedBy);
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", order));
    }

    @Operation(
            summary = "Mark payment as paid",
            description = "Mark order payment as paid (Admin/Staff only)"
    )
    @PostMapping("/{orderId}/payment/paid")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderDto>> markPaymentAsPaid(
            @PathVariable UUID orderId) {
        OrderDto order = orderService.markPaymentAsPaid(orderId);
        return ResponseEntity.ok(ApiResponse.success("Payment marked as paid", order));
    }

    @Operation(
            summary = "Mark payment as failed",
            description = "Mark order payment as failed (Admin/Staff only)"
    )
    @PostMapping("/{orderId}/payment/failed")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderDto>> markPaymentAsFailed(
            @PathVariable UUID orderId) {
        OrderDto order = orderService.markPaymentAsFailed(orderId);
        return ResponseEntity.ok(ApiResponse.success("Payment marked as failed", order));
    }

    // ===== STATISTICS ENDPOINTS =====

    @Operation(
            summary = "Get order statistics",
            description = "Get order statistics including counts by status and total revenue"
    )
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<OrderStatistics>> getOrderStatistics() {
        OrderStatistics statistics = orderService.getOrderStatistics();
        return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", statistics));
    }

    @Operation(
            summary = "Get total revenue",
            description = "Get total revenue from all delivered and paid orders"
    )
    @GetMapping("/revenue/total")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Double>> getTotalRevenue() {
        Double revenue = orderService.getTotalRevenue();
        return ResponseEntity.ok(ApiResponse.success("Total revenue retrieved successfully", revenue));
    }

    @Operation(
            summary = "Get revenue by date range",
            description = "Get total revenue within a specific date range"
    )
    @GetMapping("/revenue/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Double>> getRevenueByDateRange(
            @Parameter(description = "Start date (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        Double revenue = orderService.getRevenueByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Revenue retrieved successfully", revenue));
    }
}