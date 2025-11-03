package dev.CaoNguyen_1883.ecommerce.order.entity;

import dev.CaoNguyen_1883.ecommerce.common.entity.BaseEntity;
import dev.CaoNguyen_1883.ecommerce.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_user", columnList = "user_id"),
        @Index(name = "idx_order_number", columnList = "order_number"),
        @Index(name = "idx_order_status", columnList = "order_status"),
        @Index(name = "idx_order_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Order extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String orderNumber;  // ORD-20250130-00001

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;  // Sum of all items

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;  // subtotal + shipping + tax - discount

    // Shipping information
    @Column(nullable = false, length = 200)
    private String shippingAddress;

    @Column(length = 100)
    private String shippingCity;

    @Column(length = 100)
    private String shippingDistrict;

    @Column(length = 100)
    private String shippingWard;

    @Column(length = 20)
    private String shippingPhone;

    @Column(length = 100)
    private String shippingRecipient;

    // Billing information (optional, can be same as shipping)
    @Column(length = 200)
    private String billingAddress;

    @Column(length = 100)
    private String billingCity;

    @Column(length = 100)
    private String billingDistrict;

    @Column(length = 100)
    private String billingWard;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.COD;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String notes;  // Customer notes

    @Column(columnDefinition = "TEXT")
    private String cancelReason;  // If cancelled

    @Column(columnDefinition = "TEXT")
    private String adminNotes;  // Internal notes for staff

    private LocalDateTime confirmedAt;

    private LocalDateTime shippedAt;

    private LocalDateTime deliveredAt;

    private LocalDateTime cancelledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmed_by")
    private User confirmedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelled_by")
    private User cancelledBy;

    // Helper methods
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

    public void calculateTotalAmount() {
        this.totalAmount = subtotal
                .add(shippingFee)
                .add(tax)
                .subtract(discount);
    }

    public boolean canCancel() {
        return orderStatus == OrderStatus.PENDING || orderStatus == OrderStatus.CONFIRMED;
    }

    public boolean canConfirm() {
        return orderStatus == OrderStatus.PENDING;
    }

    public boolean canShip() {
        return orderStatus == OrderStatus.CONFIRMED || orderStatus == OrderStatus.PROCESSING;
    }

    public boolean canDeliver() {
        return orderStatus == OrderStatus.SHIPPED;
    }

    public void cancel(String reason, User cancelledBy) {
        if (!canCancel()) {
            throw new IllegalStateException("Cannot cancel order in status: " + orderStatus);
        }
        this.orderStatus = OrderStatus.CANCELLED;
        this.cancelReason = reason;
        this.cancelledBy = cancelledBy;
        this.cancelledAt = LocalDateTime.now();
    }

    public void confirm(User confirmedBy) {
        if (!canConfirm()) {
            throw new IllegalStateException("Cannot confirm order in status: " + orderStatus);
        }
        this.orderStatus = OrderStatus.CONFIRMED;
        this.confirmedBy = confirmedBy;
        this.confirmedAt = LocalDateTime.now();
    }

    public void markAsProcessing() {
        if (orderStatus != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot process order in status: " + orderStatus);
        }
        this.orderStatus = OrderStatus.PROCESSING;
    }

    public void ship() {
        if (!canShip()) {
            throw new IllegalStateException("Cannot ship order in status: " + orderStatus);
        }
        this.orderStatus = OrderStatus.SHIPPED;
        this.shippedAt = LocalDateTime.now();
    }

    public void deliver() {
        if (!canDeliver()) {
            throw new IllegalStateException("Cannot deliver order in status: " + orderStatus);
        }
        this.orderStatus = OrderStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
        
        // Auto mark payment as paid for COD
        if (paymentMethod == PaymentMethod.COD && paymentStatus == PaymentStatus.PENDING) {
            this.paymentStatus = PaymentStatus.PAID;
        }
    }

    public void markPaymentAsPaid() {
        this.paymentStatus = PaymentStatus.PAID;
    }

    public void markPaymentAsFailed() {
        this.paymentStatus = PaymentStatus.FAILED;
    }
}