package dev.CaoNguyen_1883.ecommerce.order.mapper;

import dev.CaoNguyen_1883.ecommerce.order.dto.OrderDto;
import dev.CaoNguyen_1883.ecommerce.order.dto.OrderItemDto;
import dev.CaoNguyen_1883.ecommerce.order.entity.Order;
import dev.CaoNguyen_1883.ecommerce.order.entity.OrderItem;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class OrderMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "userFullName", source = "user.fullName")
    @Mapping(target = "items", source = "items")
    public abstract OrderDto toDto(Order order);

    public abstract List<OrderDto> toDtoList(List<Order> orders);

    @Mapping(target = "variantId", source = "variant.id")
    public abstract OrderItemDto toItemDto(OrderItem orderItem);

    public abstract List<OrderItemDto> toItemDtoList(List<OrderItem> items);
}