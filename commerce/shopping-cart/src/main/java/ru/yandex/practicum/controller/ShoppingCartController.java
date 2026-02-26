package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.cart.controller.ShoppingCartApi;
import ru.yandex.practicum.commerce.cart.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.cart.dto.ShoppingCartDto;
import ru.yandex.practicum.service.CartService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ShoppingCartController implements ShoppingCartApi {

    private final CartService shoppingCartService;

    @Override
    public ShoppingCartDto getShoppingCart(@RequestParam @NotEmpty String username) {
        log.info("ENTER controller getShoppingCart: username={}", username);
        return shoppingCartService.getShoppingCart(username);
    }

    @Override
    public ShoppingCartDto addProductToShoppingCart(@RequestParam @NotEmpty String username,
                                                    @RequestBody Map<UUID, Long> products) {
        log.info("ENTER controller addProductToShoppingCart: username={}", username);
        return shoppingCartService.addProductToShoppingCart(username, products);
    }

    @Override
    public void deleteShoppingCart(@RequestParam @NotEmpty String username) {
        log.info("ENTER controller deactivateCurrentShoppingCart: username={}", username);
        shoppingCartService.deactivateCurrentShoppingCart(username);
    }

    @Override
    public ShoppingCartDto removeProductFromShoppingCart(@RequestParam @NotEmpty String username,
                                                         @RequestBody List<UUID> productIds) {
        log.info("ENTER controller removeFromShoppingCart: username={}", username);
        return shoppingCartService.removeFromShoppingCart(username, productIds);
    }

    @Override
    public ShoppingCartDto changeProductQuantity(@RequestParam @NotEmpty String username, @RequestBody @Valid ChangeProductQuantityRequest request) {
        log.info("ENTER controller changeProductQuantity: username={}", username);
        return shoppingCartService.changeProductQuantity(username, request);
    }
}
