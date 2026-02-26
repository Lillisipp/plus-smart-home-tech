package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.model.ShoppingCart;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<ShoppingCart, UUID> {
    @EntityGraph(attributePaths = "products")
    Optional<ShoppingCart> findByUsernameAndActiveTrue(String username);
    Optional<ShoppingCart> findTopByUsernameOrderByCreatedAtDesc(String username);
}
