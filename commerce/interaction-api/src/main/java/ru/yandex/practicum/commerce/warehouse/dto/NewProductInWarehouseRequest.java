package ru.yandex.practicum.commerce.warehouse.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NewProductInWarehouseRequest {

    @NotNull
    UUID productId;

    @NotNull
    Boolean fragile;

    @NotNull
    @Valid
    DimensionDto dimension;

    @NotNull
    @DecimalMin("1.0")
    Double weight;
}
