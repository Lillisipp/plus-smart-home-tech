package ru.yandex.practicum.commerce.warehouse.dto;

import jakarta.validation.constraints.Min;
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
public class AddProductToWarehouseRequest {
    @NotNull
    UUID productId;

    @NotNull
    @Min(1)
    Long quantity;
}
