package ru.yandex.practicum.commerce.cart.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.cart.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.cart.dto.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequestMapping("/api/v1/shopping-cart")
public interface ShoppingCartApi {

    @GetMapping
    ShoppingCartDto getShoppingCart(@RequestParam @NotEmpty String username);

    @PutMapping
    ShoppingCartDto addProductToShoppingCart(@RequestParam @NotEmpty String username,
                                             @RequestBody Map<UUID, Long> products);

    @DeleteMapping
    void deleteShoppingCart(@RequestParam @NotEmpty String username);

    @PostMapping("/remove")
    ShoppingCartDto removeProductFromShoppingCart(@RequestParam @NotEmpty String username,
                                                  @RequestBody List<UUID> productIds);

    @PostMapping("/change-quantity")
    ShoppingCartDto changeProductQuantity(@RequestParam @NotEmpty String username,
                                          @RequestBody @Valid ChangeProductQuantityRequest request);
}
