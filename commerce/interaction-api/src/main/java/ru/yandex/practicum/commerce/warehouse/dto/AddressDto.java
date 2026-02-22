package ru.yandex.practicum.commerce.warehouse.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {

    @NotBlank
    String country;
    @NotBlank
    String city;
    @NotBlank
    String street;
    @NotBlank
    String house;
    @NotBlank
    String flat;
}
