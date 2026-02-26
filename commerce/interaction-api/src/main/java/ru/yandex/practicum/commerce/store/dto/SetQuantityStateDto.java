package ru.yandex.practicum.commerce.store.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.commerce.store.enums.QuantityState;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SetQuantityStateDto {

    @NotNull
    UUID productId;

    @NotNull
    QuantityState state;
}
