package ru.yandex.practicum.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.model.WarehouseEntity;
import ru.yandex.practicum.repository.WarehouseRepository;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class Util {

    private final WarehouseRepository repository;
    public Map<UUID, WarehouseEntity> loadWarehouseProducts(Set<UUID> ids) {
        log.debug("ENTER loadByIdsOrThrow: ids={}", ids == null ? 0 : ids.size());

        if (ids == null || ids.isEmpty()) {
            log.warn("loadByIdsOrThrow: ids is null/empty");
            throw new IllegalArgumentException("Product ids must be provided");
        }
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
        log.debug("EXIT loadByIdsOrThrow: loaded={}", byId.size());
        return byId;
    }
}
