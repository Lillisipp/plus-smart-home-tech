package ru.yandex.practicum.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.model.OrderItemEntity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class OrderItemFactory {
    public Set<OrderItemEntity> buildItems(Map<UUID, Long> products, Order savedOrder) {
        log.debug("ENTER buildItems: orderId={}, products={}",
                savedOrder == null ? null : savedOrder.getOrderId(),
                products == null ? 0 : products.size());

        if (savedOrder == null || savedOrder.getOrderId() == null) {
            throw new IllegalArgumentException("savedOrder with generated orderId must be provided");
        }
        if (products == null || products.isEmpty()) {
            return new HashSet<>();
        }

        Set<OrderItemEntity> items = new HashSet<>();

        for (var entry : products.entrySet()) {
            UUID productId = entry.getKey();
            Long qty = entry.getValue();

            if (productId == null) {
                log.warn("buildItems: productId is null, skip");
                continue;
            }
            long quantity = qty == null ? 0L : qty;
            if (quantity <= 0) {
                log.warn("buildItems: non-positive quantity={}, productId={}, skip", quantity, productId);
                continue;
            }

            OrderItemEntity item = new OrderItemEntity();
            item.setOrder(savedOrder);
            item.setId(new OrderItemEntity.OrderItemId(savedOrder.getOrderId(), productId));
            item.setQuantity(quantity);

            items.add(item);
        }

        log.debug("EXIT buildItems: orderId={}, items={}", savedOrder.getOrderId(), items.size());
        return items;
    }
}
