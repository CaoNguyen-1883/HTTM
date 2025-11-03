package dev.CaoNguyen_1883.ecommerce.cart.mapper;

import dev.CaoNguyen_1883.ecommerce.cart.dto.CartDto;
import dev.CaoNguyen_1883.ecommerce.cart.dto.CartItemDto;
import dev.CaoNguyen_1883.ecommerce.cart.entity.Cart;
import dev.CaoNguyen_1883.ecommerce.cart.entity.CartItem;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class CartMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "totalItems", expression = "java(cart.getTotalItems())")
    @Mapping(target = "subtotal", expression = "java(cart.getTotalPrice())")
    @Mapping(target = "discount", expression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "totalPrice", expression = "java(cart.getTotalPrice())")
    public abstract CartDto toDto(Cart cart);

    @Mapping(target = "variantId", source = "variant.id")
    @Mapping(target = "variantSku", source = "variant.sku")
    @Mapping(target = "variantName", source = "variant.name")
    @Mapping(target = "productName", source = "variant.product.name")
    @Mapping(target = "productId", source = "variant.product.id")
    @Mapping(target = "productSlug", source = "variant.product.slug")
    @Mapping(target = "primaryImage", expression = "java(getPrimaryImageUrl(cartItem))")
    @Mapping(target = "subtotal", expression = "java(cartItem.getSubtotal())")
    @Mapping(target = "availableStock", expression = "java(getAvailableStock(cartItem))")
    @Mapping(target = "isAvailable", expression = "java(isItemAvailable(cartItem))")
    public abstract CartItemDto toItemDto(CartItem cartItem);

    public abstract List<CartItemDto> toItemDtoList(List<CartItem> items);

    // Helper methods
    protected String getPrimaryImageUrl(CartItem cartItem) {
        if (cartItem.getVariant() == null || cartItem.getVariant().getImages() == null) {
            return null;
        }
        return cartItem.getVariant().getImages().stream()
                .filter(img -> img.getIsPrimary() != null && img.getIsPrimary())
                .findFirst()
                .map(img -> img.getImageUrl())
                .orElseGet(() -> cartItem.getVariant().getImages().stream()
                        .findFirst()
                        .map(img -> img.getImageUrl())
                        .orElse(null));
    }

    protected Integer getAvailableStock(CartItem cartItem) {
        if (cartItem.getVariant() == null) {
            return 0;
        }
        int stock = cartItem.getVariant().getStock() != null ? cartItem.getVariant().getStock() : 0;
        int reserved = cartItem.getVariant().getReservedStock() != null ? cartItem.getVariant().getReservedStock() : 0;
        return Math.max(0, stock - reserved);
    }

    protected Boolean isItemAvailable(CartItem cartItem) {
        if (cartItem.getVariant() == null || cartItem.getVariant().getProduct() == null) {
            return false;
        }
        boolean variantActive = cartItem.getVariant().getIsActive() != null && cartItem.getVariant().getIsActive();
        boolean productActive = cartItem.getVariant().getProduct().getIsActive() != null && cartItem.getVariant().getProduct().getIsActive();
        boolean hasStock = getAvailableStock(cartItem) >= cartItem.getQuantity();

        return variantActive && productActive && hasStock;
    }
}