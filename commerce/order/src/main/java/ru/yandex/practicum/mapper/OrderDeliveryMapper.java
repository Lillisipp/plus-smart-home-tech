package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.commerce.delivery.dto.DeliveryDto;
import ru.yandex.practicum.commerce.delivery.enums.DeliveryState;
import ru.yandex.practicum.commerce.order.dto.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.warehouse.dto.AddressDto;
import ru.yandex.practicum.model.Order;

@Mapper(config = MapperConfigSpring.class, imports = DeliveryState.class)
public interface OrderDeliveryMapper {

    @Mapping(target = "deliveryId", ignore = true)
    @Mapping(target = "fromAddress", expression = "java(fromAddress)")
    @Mapping(target = "toAddress", expression = "java(req.getDeliveryAddress())")
    @Mapping(target = "orderId", expression = "java(order.getOrderId())")
    @Mapping(target = "deliveryState", expression = "java(DeliveryState.CREATED)")
    @Mapping(target = "deliveryWeight", expression = "java(order.getDeliveryWeight())")
    @Mapping(target = "deliveryVolume", expression = "java(order.getDeliveryVolume())")
    @Mapping(target = "fragile", expression = "java(order.isFragile())")
    DeliveryDto toPlanDeliveryRequest(Order order, CreateNewOrderRequest req, AddressDto fromAddress);
}