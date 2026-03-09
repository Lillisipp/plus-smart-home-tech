package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.commerce.order.dto.OrderDto;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.model.OrderItemEntity;

import java.util.*;
import java.util.stream.Collectors;

@Mapper(config = MapperConfigSpring.class)
public interface OrderMapper {

    @Mapping(target = "state", source = "status")
    @Mapping(target = "products", expression = "java(toProductsMap(entity.getItems()))")
    OrderDto toDto(Order entity);

    @Mapping(target = "status", source = "state")
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Order toEntity(OrderDto dto);

    default Set<OrderItemEntity> toItems(Map<UUID, Long> products, Order order) {
        if (products == null || products.isEmpty()) return Collections.emptySet();

        Set<OrderItemEntity> result = new HashSet<>();
        for (var entry : products.entrySet()) {
            UUID productId = entry.getKey();
            Long qty = entry.getValue();

            OrderItemEntity item = new OrderItemEntity();
            item.setOrder(order);
            item.setId(new OrderItemEntity.OrderItemId(order.getOrderId(), productId));
            item.setQuantity(qty == null ? 0L : qty);

            result.add(item);
        }
        return result;
    }

    default Map<UUID, Long> toProductsMap(Set<OrderItemEntity> items) {
        if (items == null || items.isEmpty()) return Collections.emptyMap();

        return items.stream().collect(Collectors.toMap(
                it -> it.getId().getProductId(),
                OrderItemEntity::getQuantity
        ));
    }
}
