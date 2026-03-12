package ru.yandex.practicum.commerce.warehouse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AcceptReturnRequest {

    @NotNull
    @NotEmpty
    private Map<@NotNull UUID, @NotNull @Min(1) Long> products;

    public Map<UUID, Long> getProducts() { return products; }
    public void setProducts(Map<UUID, Long> products) { this.products = products; }
}