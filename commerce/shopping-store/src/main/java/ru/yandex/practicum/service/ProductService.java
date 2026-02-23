package ru.yandex.practicum.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.store.dto.ProductDto;
import ru.yandex.practicum.commerce.store.enums.ProductCategory;
import ru.yandex.practicum.commerce.store.enums.ProductState;
import ru.yandex.practicum.commerce.store.enums.QuantityState;
import ru.yandex.practicum.error.ProductNotFoundException;
import ru.yandex.practicum.mapper.ProductMapper;
import ru.yandex.practicum.model.ProductEntity;
import ru.yandex.practicum.repository.ProductRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public Page<ProductDto> getProductsByCategory(ProductCategory productCategory, Pageable pageable) {
        log.info("ENTER getProductsByCategory: category={}, page={}, size={}, sort={}",
                productCategory,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort());

        Page<ProductEntity> products = productRepository.findByProductCategoryAndProductState(
                productCategory, ProductState.ACTIVE, pageable
        );

        Page<ProductDto> result = products.map(productMapper::toDto);

        log.info("EXIT getProductsByCategory: returned={}, total={}",
                result.getNumberOfElements(), result.getTotalElements());
        return result;
    }

    @Transactional
    public ProductDto addProduct(ProductDto productDto) {
        log.info("ENTER addProduct: name={}, category={}, state={}, quantityState={}",
                productDto.getProductName(),
                productDto.getProductCategory(),
                productDto.getProductState(),
                productDto.getQuantityState());

        ProductEntity product = productMapper.toEntity(productDto);

        if (product.getProductState() == null) {
            product.setProductState(ProductState.ACTIVE);
        }
        if (product.getQuantityState() == null) {
            product.setQuantityState(QuantityState.ENOUGH);
        }

        ProductEntity savedProduct = productRepository.save(product);

        log.info("EXIT addProduct: productId={}", savedProduct.getProductId());
        return productMapper.toDto(savedProduct);
    }

    @Transactional
    public ProductDto updateProduct(ProductDto productDto) {
        UUID productId = productDto.getProductId();
        log.info("ENTER updateProduct: productId={}", productId);

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        productMapper.updateEntity(productDto, product);

        ProductEntity saved = productRepository.save(product);

        log.info("EXIT updateProduct: productId={}", saved.getProductId());
        return productMapper.toDto(saved);
    }

    @Transactional
    public boolean removeProductFromStore(UUID productId) {
        log.info("ENTER removeProductFromStore: productId={}", productId);

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.setProductState(ProductState.DEACTIVATE);
        productRepository.save(product);

        log.info("EXIT removeProductFromStore: productId={}, deactivated=true", productId);
        return true;
    }

    @Transactional
    public Boolean setQuantityState(UUID productId, QuantityState quantityState) {
        log.info("ENTER setQuantityState: productId={}, quantityState={}", productId, quantityState);

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.setQuantityState(quantityState);
        productRepository.save(product);

        log.info("EXIT setQuantityState: productId={}, ok=true", productId);
        return true;
    }

    @Transactional(readOnly = true)
    public ProductDto getProductById(UUID productId) {
        log.info("ENTER getProductById: productId={}", productId);

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        log.info("EXIT getProductById: productId={}, name={}", productId, product.getProductName());
        return productMapper.toDto(product);
    }
}

