package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.cart.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.cart.dto.ShoppingCartDto;
import ru.yandex.practicum.controller.WarehouseClient;
import ru.yandex.practicum.mapper.CartMapper;
import ru.yandex.practicum.model.ShoppingCart;
import ru.yandex.practicum.repository.CartRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository shoppingCartRepository;
    private final CartMapper shoppingCartMapper;
    private final WarehouseClient warehouseClient;

    @Transactional(readOnly = true)
    public ShoppingCartDto getShoppingCart(String username) {
        log.info("ENTER getShoppingCart: username={}", username);
        validateUsername(username);

        ShoppingCart cart = shoppingCartRepository.findByUsernameAndActiveTrue(username)
                .orElseGet(() -> {
                    ShoppingCart created = ShoppingCart.builder()
                            .username(username)
                            .active(true)
                            .build();
                    ShoppingCart saved = shoppingCartRepository.save(created);
                    log.info("Created new cart: username={}, cartId={}", username, saved.getShoppingCartId());
                    return saved;
                });

        ShoppingCartDto dto = shoppingCartMapper.toShoppingCartDto(cart);
        log.info("EXIT getShoppingCart: username={}, cartId={}, items={}",
                username, dto.getShoppingCartId(), dto.getProducts().size());
        return dto;
    }

    @Transactional
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> toAdd) {
        log.info("ENTER addProductToShoppingCart: username={}, toAdd={}", username, toAdd);
        validateUsername(username);

        ShoppingCart cart = shoppingCartRepository.findByUsernameAndActiveTrue(username)
                .orElseGet(() -> shoppingCartRepository.save(
                        ShoppingCart.builder().username(username).active(true).build()
                ));

        if (!cart.isActive()) {
            throw new IllegalStateException("Cart is deactivated");
        }

        for (Map.Entry<UUID, Long> e : toAdd.entrySet()) {
            UUID productId = e.getKey();
            long addQty = e.getValue() == null ? 0L : e.getValue();
            if (addQty <= 0) continue;

            int current = cart.getProducts().getOrDefault(productId, 0);
            long sum = (long) current + addQty;
            if (sum > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Quantity too large for Integer: " + sum);
            }
            cart.getProducts().put(productId, (int) sum);
        }
        ShoppingCartDto dtoForCheck = shoppingCartMapper.toShoppingCartDto(cart);
        log.info("CALL warehouse.check: cartId={}, items={}", dtoForCheck.getShoppingCartId(), dtoForCheck.getProducts().size());
        warehouseClient.checkProductQuantityEnoughForShoppingCart(dtoForCheck);
        log.info("warehouse.check OK: cartId={}", dtoForCheck.getShoppingCartId());

        ShoppingCart saved = shoppingCartRepository.save(cart);
        ShoppingCartDto result = shoppingCartMapper.toShoppingCartDto(saved);

        log.info("EXIT addProductToShoppingCart: cartId={}, items={}", result.getShoppingCartId(), result.getProducts().size());
        return result;
    }

    @Transactional
    public void deactivateCurrentShoppingCart(String username) {
        log.info("ENTER deactivateCurrentShoppingCart: username={}", username);
        validateUsername(username);

        ShoppingCart cart = shoppingCartRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new IllegalArgumentException("Active cart not found for user: " + username));

        cart.setActive(false);
        shoppingCartRepository.save(cart);

        log.info("EXIT deactivateCurrentShoppingCart: cartId={}", cart.getShoppingCartId());
    }

    @Transactional
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> productIds) {
        log.info("ENTER removeFromShoppingCart: username={}, productIds={}", username, productIds);
        validateUsername(username);

        ShoppingCart cart = shoppingCartRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new IllegalArgumentException("Active cart not found for user: " + username));

        boolean removedAny = false;
        for (UUID id : productIds) {
            removedAny |= (cart.getProducts().remove(id) != null);
        }
        if (!removedAny) {
            throw new IllegalArgumentException("No specified products in shopping cart");
        }
        ShoppingCart saved = shoppingCartRepository.save(cart);
        ShoppingCartDto result = shoppingCartMapper.toShoppingCartDto(saved);

        log.info("EXIT removeFromShoppingCart: cartId={}, items={}", result.getShoppingCartId(), result.getProducts().size());
        return result;
    }

    @Transactional
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest req) {
        log.info("ENTER changeProductQuantity: username={}, productId={}, newQty={}",
                username, req.getProductId(), req.getNewQuantity());
        validateUsername(username);

        ShoppingCart cart = shoppingCartRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new IllegalArgumentException("Active cart not found for user: " + username));

        UUID productId = req.getProductId();
        long newQty = req.getNewQuantity() == null ? 0L : req.getNewQuantity();
        if (!cart.getProducts().containsKey(productId)) {
            throw new IllegalArgumentException("No such product in cart: " + productId);
        }

        if (newQty <= 0) {
            cart.getProducts().remove(productId); // 0 = удалить
        } else {
            if (newQty > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Quantity too large for Integer: " + newQty);
            }
            cart.getProducts().put(productId, (int) newQty);
        }
        ShoppingCartDto dtoForCheck = shoppingCartMapper.toShoppingCartDto(cart);
        warehouseClient.checkProductQuantityEnoughForShoppingCart(dtoForCheck);

        ShoppingCart saved = shoppingCartRepository.save(cart);
        ShoppingCartDto result = shoppingCartMapper.toShoppingCartDto(saved);

        log.info("EXIT changeProductQuantity: cartId={}, items={}", result.getShoppingCartId(), result.getProducts().size());
        return result;
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be blank");
        }
    }
}
