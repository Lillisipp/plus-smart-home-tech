package ru.yandex.practicum.commerce.util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
public class PageableFactory {

    public Pageable from(int page, int size, List<String> sort) {
        log.debug("ENTER from: page={}, size={}, sort={}", page, size, sort);

        int safePage = Math.max(page, 0);
        int safeSize = (size <= 0) ? 10 : Math.min(size, 1000); // можно настроить лимит

        Sort parsedSort = parseSort(sort);

        Pageable pageable = PageRequest.of(safePage, safeSize, parsedSort);
        log.debug("EXIT from: pageable={}", pageable);
        return pageable;
    }

    private Sort parseSort(List<String> sort) {
        log.debug("ENTER parseSort: sort={}", sort);

        if (sort == null || sort.isEmpty()) {
            log.debug("EXIT parseSort: unsorted (sort is null/empty)");
            return Sort.unsorted();
        }

        List<Sort.Order> orders = new ArrayList<>();

        // Идём по списку индексом, чтобы поддержать пары ["productName", "DESC"]
        for (int i = 0; i < sort.size(); i++) {
            String token = sort.get(i);
            if (token == null || token.isBlank()) {
                continue;
            }

            String trimmed = token.trim();

            // Случай 1: пришло "field,DESC"
            if (trimmed.contains(",")) {
                String[] parts = trimmed.split(",", 2);
                String property = parts[0].trim();
                Sort.Direction direction = parseDirectionOrDefault(parts.length > 1 ? parts[1] : null);

                if (property.isBlank()) {
                    log.warn("Skip invalid sort token (empty property): '{}'", token);
                    continue;
                }

                orders.add(new Sort.Order(direction, property));
                continue;
            }

            // Случай 2: Spring разрезал по запятой: ["field", "DESC"]
            String property = trimmed;

            Sort.Direction direction = Sort.Direction.ASC; // по умолчанию
            if (i + 1 < sort.size()) {
                String maybeDir = sort.get(i + 1);
                Sort.Direction parsed = tryParseDirection(maybeDir);
                if (parsed != null) {
                    direction = parsed;
                    i++; // съели следующий элемент как направление
                }
            }

            if (property.isBlank()) {
                log.warn("Skip invalid sort token (empty property): '{}'", token);
                continue;
            }

            orders.add(new Sort.Order(direction, property));
        }

        if (orders.isEmpty()) {
            log.debug("EXIT parseSort: unsorted (no valid orders)");
            return Sort.unsorted();
        }

        Sort result = Sort.by(orders);
        log.debug("EXIT parseSort: {}", result);
        return result;
    }

    private Sort.Direction parseDirectionOrDefault(String directionRaw) {
        Sort.Direction parsed = tryParseDirection(directionRaw);
        return (parsed != null) ? parsed : Sort.Direction.ASC;
    }

    private Sort.Direction tryParseDirection(String directionRaw) {
        if (directionRaw == null) return null;

        String v = directionRaw.trim().toUpperCase(Locale.ROOT);
        if (v.isEmpty()) return null;

        // поддержим только ASC/DESC (чтобы не падать от мусора)
        return switch (v) {
            case "ASC" -> Sort.Direction.ASC;
            case "DESC" -> Sort.Direction.DESC;
            default -> null;
        };
    }
}
