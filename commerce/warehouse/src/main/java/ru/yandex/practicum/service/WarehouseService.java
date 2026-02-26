package ru.yandex.practicum.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.cart.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.warehouse.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.warehouse.dto.AddressDto;
import ru.yandex.practicum.commerce.warehouse.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.warehouse.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.mapper.WarehouseMapper;
import ru.yandex.practicum.model.WarehouseEntity;
import ru.yandex.practicum.repository.WarehouseRepository;

import java.security.SecureRandom;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseService {
    private static final String[] ADDRESSES = new String[]{"ADDRESS_1", "ADDRESS_2"};

    private final String currentAddress = ADDRESSES[new SecureRandom().nextInt(ADDRESSES.length)];

    private final WarehouseRepository repository;

    private final WarehouseMapper mapper;

    @Transactional
    public void newProductInWarehouse(NewProductInWarehouseRequest request) {
        log.info("ENTER newProductInWarehouse: productId={}, fragile={}, dim={}, weight={}",
                request.getProductId(), request.getFragile(), request.getDimension(), request.getWeight());

        UUID productId = request.getProductId();

        if (repository.existsById(productId)) {
            log.warn("Product already exists in warehouse: productId={}", productId);
            throw new IllegalArgumentException("SpecifiedProductAlreadyInWarehouse: " + productId);
        }

        WarehouseEntity entity = mapper.toEntity(request);
        repository.save(entity);
        log.info("EXIT newProductInWarehouse: productId={}", request.getProductId());
    }


    @Transactional
    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        log.info("ENTER addProductToWarehouse: productId={}, quantity={}", request.getProductId(), request.getQuantity());

        WarehouseEntity entity = repository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("NoSpecifiedProductInWarehouse: " + request.getProductId()));

        long before = entity.getQuantity();
        long after = before + request.getQuantity();
        entity.setQuantity(before + request.getQuantity());

        entity.setQuantity(after);
        repository.save(entity);
        log.info("EXIT addProductToWarehouse: productId={}, before={}, after={}",
                request.getProductId(), before, entity.getQuantity());
    }

    @Transactional(readOnly = true)
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto cart) {
        log.info("ENTER check: cartId={}, items={}", cart.getShoppingCartId(), cart.getProducts().size());

        if (cart.getProducts() == null || cart.getProducts().isEmpty()) {
            BookedProductsDto empty = new BookedProductsDto(0.0, 0.0, false);
            log.info("EXIT checkProductQuantityEnoughForShoppingCart: empty cart -> {}", empty);
            return empty;
        }

        Set<UUID> ids = cart.getProducts().keySet();
        List<WarehouseEntity> products = repository.findAllById(ids);
        if (products.size() != ids.size()) {
            Set<UUID> found = new HashSet<>();
            for (WarehouseEntity p : products) found.add(p.getProductId());

            Set<UUID> missing = new HashSet<>(ids);
            missing.removeAll(found);
            log.warn("Some products not registered in warehouse: missing={}", missing);
            throw new IllegalArgumentException("Product not found in warehouse: " + missing);

        }
        Map<UUID, WarehouseEntity> byId = new HashMap<>();
        for (WarehouseEntity p : products) {
            byId.put(p.getProductId(), p);
        }

        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean fragile = false;

        for (Map.Entry<UUID, Long> entry : cart.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            long needed = entry.getValue();

            WarehouseEntity p = byId.get(productId);
            long available = p.getQuantity();

            if (available < needed) {
                log.warn("Low quantity: productId={}, needed={}, available={}", productId, needed, available);
                throw new IllegalArgumentException("LowQuantity: productId=" + productId +
                        ", needed=" + needed + ", available=" + available);
            }

            totalWeight += p.getWeight() * needed;
            totalVolume += (p.getWidth() * p.getHeight() * p.getDepth()) * needed;
            fragile = fragile || p.getFragile();
        }

        BookedProductsDto result = new BookedProductsDto(1.0, 1.0, false);
        log.info("EXIT checkProductQuantityEnoughForShoppingCart: cartId={}, weight={}, volume={}, fragile={}",
                cart.getShoppingCartId(), result.getDeliveryWeight(), result.getDeliveryVolume(), result.getFragile());
        return result;
    }

    public AddressDto getWarehouseAddress() {
        log.info("getWarehouseAddress: {}", currentAddress);
        return new AddressDto(currentAddress, currentAddress, currentAddress, currentAddress, currentAddress);
    }

}
