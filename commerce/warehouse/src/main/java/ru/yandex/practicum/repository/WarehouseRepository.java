package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.Warehouse;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
}
