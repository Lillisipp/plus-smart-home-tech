package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.model.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    @EntityGraph(attributePaths = "items")
    List<Order> findAllByUsernameOrderByCreatedAtDesc(String username);

    @EntityGraph(attributePaths = "items")
    Optional<Order> findWithItemsByOrderId(UUID orderId);
}