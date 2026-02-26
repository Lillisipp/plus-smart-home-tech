package ru.yandex.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.commerce.store.enums.ProductCategory;
import ru.yandex.practicum.commerce.store.enums.ProductState;
import ru.yandex.practicum.model.ProductEntity;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {
    Page<ProductEntity> findByProductCategoryAndProductState(ProductCategory category, ProductState state, Pageable pageable);
    Page<ProductEntity> findByProductCategory(ProductCategory productCategory, Pageable pageable);
}
