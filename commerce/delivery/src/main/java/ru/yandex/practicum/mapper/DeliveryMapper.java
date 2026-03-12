package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.commerce.delivery.dto.DeliveryDto;
import ru.yandex.practicum.commerce.warehouse.dto.AddressDto;
import ru.yandex.practicum.model.Delivery;
import ru.yandex.practicum.model.DeliveryAddress;


@Mapper(componentModel = "spring")
public interface DeliveryMapper {

    DeliveryAddress toEntity(AddressDto dto);

    AddressDto toDto(DeliveryAddress entity);

    @Mapping(target = "fromAddress", expression = "java(toEntity(dto.getFromAddress()))")
    @Mapping(target = "toAddress", expression = "java(toEntity(dto.getToAddress()))")
    Delivery toEntity(DeliveryDto dto);

    @Mapping(target = "fromAddress", expression = "java(toDto(entity.getFromAddress()))")
    @Mapping(target = "toAddress", expression = "java(toDto(entity.getToAddress()))")
    DeliveryDto toDto(Delivery entity);
}

