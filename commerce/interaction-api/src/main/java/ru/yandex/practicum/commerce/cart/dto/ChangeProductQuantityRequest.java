package ru.yandex.practicum.commerce.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Запрос изменения количества конкретного товара в корзине.
 */

@NoArgsConstructor
@AllArgsConstructor
public class ChangeProductQuantityRequest {
    @NotNull
    UUID productId;

    @NotNull
    @Min(0)
    Long newQuantity;
}
