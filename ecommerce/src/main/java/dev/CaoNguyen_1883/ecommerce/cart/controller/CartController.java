package dev.CaoNguyen_1883.ecommerce.cart.controller;

import dev.CaoNguyen_1883.ecommerce.auth.security.CustomUserDetails;
import dev.CaoNguyen_1883.ecommerce.cart.dto.AddToCartRequest;
import dev.CaoNguyen_1883.ecommerce.cart.dto.CartDto;
import dev.CaoNguyen_1883.ecommerce.cart.dto.UpdateCartItemRequest;
import dev.CaoNguyen_1883.ecommerce.cart.service.ICartService;
import dev.CaoNguyen_1883.ecommerce.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Shopping Cart", description = "APIs for managing shopping cart")
@SecurityRequirement(name = "bearer-jwt")
public class CartController {

    private final ICartService cartService;

    @Operation(
            summary = "Get cart",
            description = "Get current user's shopping cart with all items"
    )
    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartDto>> getCart(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getId();

        CartDto cart = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", cart));
    }

    @Operation(
            summary = "Add item to cart",
            description = "Add a product variant to the cart. If item already exists, quantity will be increased."
    )
    @PostMapping("/items")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartDto>> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getId();

        CartDto cart = cartService.addToCart(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart successfully", cart));
    }

    @Operation(
            summary = "Update cart item",
            description = "Update the quantity of a cart item"
    )
    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartDto>> updateCartItem(
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getId();

        CartDto cart = cartService.updateCartItem(userId, itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated successfully", cart));
    }

    @Operation(
            summary = "Remove cart item",
            description = "Remove an item from the cart"
    )
    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartDto>> removeCartItem(
            @PathVariable UUID itemId,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getId();

        CartDto cart = cartService.removeCartItem(userId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart successfully", cart));
    }

    @Operation(
            summary = "Clear cart",
            description = "Remove all items from the cart"
    )
    @DeleteMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> clearCart(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getId();

        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully", null));
    }

    @Operation(
            summary = "Sync cart",
            description = "Synchronize cart with current product data. Updates prices and removes unavailable items."
    )
    @PostMapping("/sync")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartDto>> syncCart(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getId();

        CartDto cart = cartService.syncCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart synced successfully", cart));
    }

    @Operation(
            summary = "Get cart item count",
            description = "Get total number of items in the cart"
    )
    @GetMapping("/count")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Integer>> getCartItemCount(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getId();

        int count = cartService.getCartItemCount(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart item count retrieved successfully", count));
    }
}