package dev.CaoNguyen_1883.ecommerce.cart.service.impl;

import dev.CaoNguyen_1883.ecommerce.cart.dto.AddToCartRequest;
import dev.CaoNguyen_1883.ecommerce.cart.dto.CartDto;
import dev.CaoNguyen_1883.ecommerce.cart.dto.UpdateCartItemRequest;
import dev.CaoNguyen_1883.ecommerce.cart.entity.Cart;
import dev.CaoNguyen_1883.ecommerce.cart.entity.CartItem;
import dev.CaoNguyen_1883.ecommerce.cart.mapper.CartMapper;
import dev.CaoNguyen_1883.ecommerce.cart.repository.CartItemRepository;
import dev.CaoNguyen_1883.ecommerce.cart.repository.CartRepository;
import dev.CaoNguyen_1883.ecommerce.cart.service.ICartService;
import dev.CaoNguyen_1883.ecommerce.common.exception.BadRequestException;
import dev.CaoNguyen_1883.ecommerce.common.exception.ForbiddenException;
import dev.CaoNguyen_1883.ecommerce.common.exception.OutOfStockException;
import dev.CaoNguyen_1883.ecommerce.common.exception.ResourceNotFoundException;
import dev.CaoNguyen_1883.ecommerce.product.entity.ProductVariant;
import dev.CaoNguyen_1883.ecommerce.product.repository.ProductVariantRepository;
import dev.CaoNguyen_1883.ecommerce.user.entity.User;
import dev.CaoNguyen_1883.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CartServiceImpl implements ICartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository variantRepository;
    private final CartMapper cartMapper;

    @Override
    public CartDto getCart(UUID userId) {
        log.debug("Fetching cart for user: {}", userId);

        // Get or create cart
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> createCartForUser(userId));

        return cartMapper.toDto(cart);
    }

    @Override
    @Transactional
    public CartDto addToCart(UUID userId, AddToCartRequest request) {
        log.debug("Adding item to cart for user: {}, variantId: {}, quantity: {}",
                userId, request.getVariantId(), request.getQuantity());

        // Get or create cart
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> createCartForUser(userId));

        // Get variant and validate
        ProductVariant variant = variantRepository.findByIdWithProduct(request.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Product variant", "id", request.getVariantId()));

        // Validate variant is active and product is approved
        validateVariantAvailable(variant);

        // Check if item already exists in cart
        CartItem existingItem = cartItemRepository.findByCartIdAndVariantId(cart.getId(), variant.getId())
                .orElse(null);

        if (existingItem != null) {
            // Update existing item quantity
            int newQuantity = existingItem.getQuantity() + request.getQuantity();

            // Check stock availability
            if (!variant.canReserve(request.getQuantity())) {
                throw new OutOfStockException("Insufficient stock for variant: " + variant.getName());
            }

            // Release old reservation and reserve new quantity
            variant.releaseReservedStock(existingItem.getQuantity());
            variant.reserveStock(newQuantity);

            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);

            log.info("Updated cart item quantity. User: {}, Item: {}, New quantity: {}",
                    userId, existingItem.getId(), newQuantity);
        } else {
            // Add new item to cart

            // Check stock availability
            if (!variant.canReserve(request.getQuantity())) {
                throw new OutOfStockException("Insufficient stock for variant: " + variant.getName());
            }

            // Reserve stock
            variant.reserveStock(request.getQuantity());

            // Create new cart item
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .variant(variant)
                    .quantity(request.getQuantity())
                    .priceAtAdd(variant.getPrice() != null ? variant.getPrice() : variant.getProduct().getBasePrice())
                    .build();

            cart.addItem(cartItem);
//            cartItemRepository.save(cartItem);

            log.info("Added new item to cart. User: {}, Variant: {}, Quantity: {}",
                    userId, variant.getId(), request.getQuantity());
        }

        // Save variant with updated reserved stock
        variantRepository.save(variant);

        // Save cart
        cart = cartRepository.save(cart);

        return cartMapper.toDto(cart);
    }

    @Override
    @Transactional
    public CartDto updateCartItem(UUID userId, UUID cartItemId, UpdateCartItemRequest request) {
        log.debug("Updating cart item: {} for user: {}, new quantity: {}",
                cartItemId, userId, request.getQuantity());

        // Get cart
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user", "userId", userId));

        // Get cart item
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", "id", cartItemId));

        // Verify cart item belongs to user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new ForbiddenException("Cart item does not belong to user's cart");
        }

        // Get variant and check availability
        ProductVariant variant = variantRepository.findById(cartItem.getVariant().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product variant", "id", cartItem.getVariant().getId()));

        int oldQuantity = cartItem.getQuantity();
        int quantityDiff = request.getQuantity() - oldQuantity;

        if (quantityDiff > 0) {
            // Increasing quantity - check if we can reserve more
            if (!variant.canReserve(quantityDiff)) {
                throw new OutOfStockException("Insufficient stock for variant: " + variant.getName());
            }
            variant.reserveStock(quantityDiff);
        } else if (quantityDiff < 0) {
            // Decreasing quantity - release reserved stock
            variant.releaseReservedStock(Math.abs(quantityDiff));
        }

        // Update cart item
        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);
        variantRepository.save(variant);

        log.info("Updated cart item. User: {}, Item: {}, Old quantity: {}, New quantity: {}",
                userId, cartItemId, oldQuantity, request.getQuantity());

        // Reload cart to get updated data
        cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));

        return cartMapper.toDto(cart);
    }

    @Override
    @Transactional
    public CartDto removeCartItem(UUID userId, UUID cartItemId) {
        log.debug("Removing cart item: {} for user: {}", cartItemId, userId);

        // Get cart
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user", "userId", userId));

        // Get cart item
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", "id", cartItemId));

        // Verify cart item belongs to user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new ForbiddenException("Cart item does not belong to user's cart");
        }

        // Release reserved stock
        ProductVariant variant = variantRepository.findById(cartItem.getVariant().getId())
                .orElse(null);

        if (variant != null) {
            variant.releaseReservedStock(cartItem.getQuantity());
            variantRepository.save(variant);
        }

        // Remove item from cart
        cart.removeItem(cartItem);
        cartItem.softDelete();
        cartItemRepository.save(cartItem);

        log.info("Removed cart item. User: {}, Item: {}", userId, cartItemId);

        // Reload cart to get updated data
        cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));

        return cartMapper.toDto(cart);
    }

    @Override
    @Transactional
    public void clearCart(UUID userId) {
        log.debug("Clearing cart for user: {}", userId);

        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user", "userId", userId));

        // Release all reserved stock
        for (CartItem item : cart.getItems()) {
            ProductVariant variant = variantRepository.findById(item.getVariant().getId())
                    .orElse(null);

            if (variant != null) {
                variant.releaseReservedStock(item.getQuantity());
                variantRepository.save(variant);
            }
        }

        // Clear all items
        cart.clearItems();
        cartItemRepository.softDeleteAllByCartId(cart.getId());
        cartRepository.save(cart);

        log.info("Cleared cart for user: {}", userId);
    }

    @Override
    @Transactional
    public CartDto syncCart(UUID userId) {
        log.debug("Syncing cart for user: {}", userId);

        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user", "userId", userId));

        List<CartItem> itemsToRemove = new ArrayList<>();

        for (CartItem item : cart.getItems()) {
            ProductVariant variant = variantRepository.findById(item.getVariant().getId())
                    .orElse(null);

            if (variant == null || !variant.getIsActive() || !variant.getProduct().getIsActive()) {
                // Product or variant no longer available
                itemsToRemove.add(item);
                log.warn("Removing unavailable item from cart. User: {}, Item: {}", userId, item.getId());
                continue;
            }

            // Update price if changed
            BigDecimal currentPrice = variant.getPrice() != null ? variant.getPrice() : variant.getProduct().getBasePrice();
            if (!item.getPriceAtAdd().equals(currentPrice)) {
                item.setPriceAtAdd(currentPrice);
                log.info("Updated cart item price. User: {}, Item: {}, New price: {}",
                        userId, item.getId(), currentPrice);
            }

            // Check if quantity exceeds available stock
            if (!variant.canReserve(item.getQuantity())) {
                int availableStock = variant.getAvailableStock();
                if (availableStock > 0) {
                    // Adjust quantity to available stock
                    variant.releaseReservedStock(item.getQuantity());
                    variant.reserveStock(availableStock);
                    item.setQuantity(availableStock);
                    log.warn("Adjusted cart item quantity to available stock. User: {}, Item: {}, New quantity: {}",
                            userId, item.getId(), availableStock);
                } else {
                    // No stock available - remove item
                    itemsToRemove.add(item);
                    variant.releaseReservedStock(item.getQuantity());
                    log.warn("Removing out of stock item from cart. User: {}, Item: {}", userId, item.getId());
                }
                variantRepository.save(variant);
            }
        }

        // Remove unavailable items
        for (CartItem item : itemsToRemove) {
            cart.removeItem(item);
            item.softDelete();
            cartItemRepository.save(item);
        }

        cart = cartRepository.save(cart);

        log.info("Synced cart for user: {}. Removed {} items", userId, itemsToRemove.size());

        return cartMapper.toDto(cart);
    }

    // ===== HELPER METHODS =====

    private Cart createCartForUser(UUID userId) {
        log.debug("Creating new cart for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Cart cart = Cart.builder()
                .user(user)
                .build();

        return cartRepository.save(cart);
    }

    private void validateVariantAvailable(ProductVariant variant) {
        if (!variant.getIsActive()) {
            throw new BadRequestException("Product variant is not available");
        }

        if (!variant.getProduct().getIsActive()) {
            throw new BadRequestException("Product is not available");
        }

        if (!variant.getProduct().getStatus().equals(dev.CaoNguyen_1883.ecommerce.product.entity.ProductStatus.APPROVED)) {
            throw new BadRequestException("Product is not approved for sale");
        }
    }

    @Override
    public int getCartItemCount(UUID userId) {
        log.debug("Fetching cart item count for user: {}", userId);

        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElse(null);

        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            return 0;
        }

        // Sum of all quantities
        return cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
}