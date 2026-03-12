package ru.yandex.practicum.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.controller.ShoppingStoreFeignClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductPriceCache {
    private final ShoppingStoreFeignClient storeClient;

    private final Cache<UUID, BigDecimal> cache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofMinutes(10))
            .build();

    public BigDecimal getPrice(UUID productId) {
        return cache.get(productId, this::loadPrice);
    }
    private BigDecimal loadPrice(UUID productId) {
        log.info("CACHE MISS: loading price from shopping-store: productId={}", productId);

        var product = storeClient.getProduct(productId);
        BigDecimal price = product.getPrice();

        if (price == null) {
            throw new IllegalArgumentException("Product price is null: productId=" + productId);
        }
        return price;
    }
}
