package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public ShoppingCartDto getShoppingCart(String username) {
        log.info("ENTER controller getShoppingCart: username={}", username);
        return shoppingCartService.getShoppingCart(username);
    }

    @Override
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products) {
        log.info("ENTER controller addProductToShoppingCart: username={}", username);
        return shoppingCartService.addProductToShoppingCart(username, products);
    }

    @Override
    public void deleteShoppingCart(String username) {
        log.info("ENTER controller deactivateCurrentShoppingCart: username={}", username);
        shoppingCartService.deactivateCurrentShoppingCart(username);
    }

    @Override
    public ShoppingCartDto removeProductFromShoppingCart(String username, List<UUID> productIds) {
        log.info("ENTER controller removeFromShoppingCart: username={}", username);
        return shoppingCartService.removeFromShoppingCart(username, productIds);
    }

    @Override
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        log.info("ENTER controller changeProductQuantity: username={}", username);
        return shoppingCartService.changeProductQuantity(username, request);
    }
}
