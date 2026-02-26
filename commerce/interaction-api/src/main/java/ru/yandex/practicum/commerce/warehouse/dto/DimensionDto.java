package ru.yandex.practicum.commerce.warehouse.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Размеры товара: ширина/высота/глубина.
 */

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DimensionDto {
    @NotNull
    @DecimalMin("1.0")
    Double width;

    @NotNull
    @DecimalMin("1.0")
    Double height;

    @NotNull
    @DecimalMin("1.0")
    Double depth;
}
