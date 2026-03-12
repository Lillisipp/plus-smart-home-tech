package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.commerce.payment.dto.PaymentDto;
import ru.yandex.practicum.model.PaymentEntity;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "status", source = "paymentState")
    @Mapping(target = "productTotal", source = "productTotal")
    PaymentDto toDto(PaymentEntity entity);

    @Mapping(target = "paymentState", source = "status")
    PaymentEntity toEntity(PaymentDto dto);
}