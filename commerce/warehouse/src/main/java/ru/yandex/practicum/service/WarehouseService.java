package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.cart.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.warehouse.dto.*;
import ru.yandex.practicum.mapper.WarehouseMapper;
import ru.yandex.practicum.model.OrderBooking;
import ru.yandex.practicum.model.OrderBookingItemEntity;
import ru.yandex.practicum.model.WarehouseEntity;
import ru.yandex.practicum.repository.OrderBookingRepository;
import ru.yandex.practicum.repository.WarehouseRepository;
import ru.yandex.practicum.utils.Util;

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
    private final OrderBookingRepository bookingRepository;
    private final Util util;

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

    @Transactional
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {

        UUID orderId = request.getOrderId();
        Map<UUID, Long> productsRequest = request.getProducts();

        log.info("ENTER assemblyProductsForOrder: orderId={}, items={}",
                orderId, productsRequest.size());

        Map<UUID, WarehouseEntity> byId = util.loadWarehouseProducts(productsRequest.keySet());

        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean fragile = false;

        for (var entry : productsRequest.entrySet()) {
            UUID productId = entry.getKey();
            long needed = entry.getValue();

            WarehouseEntity p = byId.get(productId);
            long available = p.getQuantity();

            if (available < needed) {
                log.warn("assemblyProductsForOrder: low quantity: orderId={}, productId={}, needed={}, available={}",
                        orderId, productId, needed, available);
                throw new IllegalArgumentException("ProductInShoppingCartLowQuantityInWarehouse: productId=" + productId);
            }

            totalWeight += p.getWeight() * needed;
            totalVolume += (p.getWidth() * p.getHeight() * p.getDepth()) * needed;
            fragile = fragile || Boolean.TRUE.equals(p.getFragile());
        }

        // 3) списание остатков (dirty checking)
        for (var entry : productsRequest.entrySet()) {
            UUID productId = entry.getKey();
            long needed = entry.getValue();

            WarehouseEntity p = byId.get(productId);
            long before = p.getQuantity();
            p.setQuantity(before - needed);

            log.info("assembly reserved: orderId={}, productId={}, before={}, reserved={}, after={}",
                    orderId, productId, before, needed, p.getQuantity());
        }

        OrderBooking booking = new OrderBooking();
        booking.setOrderId(orderId);
        booking.setDeliveryId(null);

        for (var entry : productsRequest.entrySet()) {
            UUID productId = entry.getKey();
            long qty = entry.getValue();

            OrderBookingItemEntity item = new OrderBookingItemEntity();
            item.setBooking(booking);
            item.setId(new OrderBookingItemEntity.OrderBookingItemId(orderId, productId));
            item.setQuantity(qty);

            booking.getItems().add(item);
        }

        bookingRepository.save(booking);

        BookedProductsDto result = new BookedProductsDto(totalWeight, totalVolume, fragile);

        log.info("EXIT assemblyProductsForOrder: orderId={}, weight={}, volume={}, fragile={}",
                orderId, result.getDeliveryWeight(), result.getDeliveryVolume(), result.getFragile());

        return result;
    }

    @Transactional
    public void shippedToDelivery(ShippedToDeliveryRequest request) {

        UUID orderId = request.getOrderId();
        UUID deliveryId = request.getDeliveryId();

        log.info("ENTER shippedToDelivery: orderId={}, deliveryId={}", orderId, deliveryId);

        // бизнес-валидация: бронь должна существовать
        OrderBooking booking = bookingRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("No booking found for orderId=" + orderId));

        booking.setDeliveryId(deliveryId); // dirty checking

        log.info("EXIT shippedToDelivery: orderId={}, deliveryId={}", orderId, deliveryId);
    }

    @Transactional
    public void acceptReturn(AcceptReturnRequest request) {

        Map<UUID, Long> products = request.getProducts();

        log.info("ENTER acceptReturn: items={}", products.size());

        Map<UUID, WarehouseEntity> byId = util.loadWarehouseProducts(products.keySet());

        for (var entry : products.entrySet()) {
            UUID productId = entry.getKey();
            long qty = entry.getValue(); // гарантированно >= 1 из @Min(1)

            WarehouseEntity p = byId.get(productId);

            long before = p.getQuantity();
            p.setQuantity(before + qty);

            log.info("return accepted: productId={}, before={}, returned={}, after={}",
                    productId, before, qty, p.getQuantity());
        }

        log.info("EXIT acceptReturn");
    }
}
