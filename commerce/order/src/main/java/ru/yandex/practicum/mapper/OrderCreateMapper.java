package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.commerce.order.dto.CreateNewOrderRequest;
import ru.yandex.practicum.model.Order;

@Mapper(componentModel = "spring")
public interface OrderCreateMapper {

    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "paymentId", ignore = true)
    @Mapping(target = "deliveryId", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "deliveryPrice", ignore = true)
    @Mapping(target = "productPrice", ignore = true)

    @Mapping(target = "deliveryWeight", constant = "0")
    @Mapping(target = "deliveryVolume", constant = "0")
    @Mapping(target = "fragile", constant = "false")

    @Mapping(target = "status", constant = "NEW")
    @Mapping(target = "username", expression = "java(username)")
    @Mapping(
            target = "shoppingCartId",
            expression = "java(req.getShoppingCart() == null ? null : req.getShoppingCart().getShoppingCartId())" // [ИЗМЕНЕНИЕ]
    )
    Order toEntity(CreateNewOrderRequest req, String username);
}