package dev.CaoNguyen_1883.ecommerce.cart.service;

import dev.CaoNguyen_1883.ecommerce.cart.dto.AddToCartRequest;
import dev.CaoNguyen_1883.ecommerce.cart.dto.CartDto;
import dev.CaoNguyen_1883.ecommerce.cart.dto.UpdateCartItemRequest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface ICartService {

    /**
     * Get user's cart. Creates a new cart if doesn't exist.
     */
    @Cacheable(value = "cart", key = "#userId")
    CartDto getCart(UUID userId);

    /**
     * Add item to cart. If item already exists, increase quantity.
     */
    @Transactional
    @CacheEvict(value = "cart", key = "#userId")
    CartDto addToCart(UUID userId, AddToCartRequest request);

    /**
     * Update cart item quantity
     */
    @Transactional
    @CacheEvict(value = "cart", key = "#userId")
    CartDto updateCartItem(UUID userId, UUID cartItemId, UpdateCartItemRequest request);

    /**
     * Remove item from cart
     */
    @Transactional
    @CacheEvict(value = "cart", key = "#userId")
    CartDto removeCartItem(UUID userId, UUID cartItemId);

    /**
     * Clear all items from cart
     */
    @Transactional
    @CacheEvict(value = "cart", key = "#userId")
    void clearCart(UUID userId);

    /**
     * Sync cart items with current product data
     * Updates prices and removes unavailable items
     */
    @Transactional
    @CacheEvict(value = "cart", key = "#userId")
    CartDto syncCart(UUID userId);

    /**
     * Get total number of items in cart
     */
    int getCartItemCount(UUID userId);
}