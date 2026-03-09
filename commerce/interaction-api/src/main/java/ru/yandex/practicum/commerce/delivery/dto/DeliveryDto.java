package ru.yandex.practicum.commerce.delivery.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.commerce.delivery.enums.DeliveryState;
import ru.yandex.practicum.commerce.warehouse.dto.AddressDto;

import java.util.UUID;

@Getter
@Setter
public class DeliveryDto {
    private UUID deliveryId;
    @NotNull
    @Valid
    private AddressDto fromAddress;
    @NotNull
    @Valid
    private AddressDto toAddress;
    @NotNull
    private UUID orderId;
    @NotNull
    private DeliveryState deliveryState;
    @NotNull
    private Double deliveryWeight;
    @NotNull
    private Double deliveryVolume;
    @NotNull
    private Boolean fragile;
}
