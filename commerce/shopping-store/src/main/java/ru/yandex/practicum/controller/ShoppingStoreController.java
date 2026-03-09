package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.order.dto.ProductPriceDto;
import ru.yandex.practicum.commerce.store.controller.ShoppingStoreApi;
import ru.yandex.practicum.commerce.store.dto.ProductDto;
import ru.yandex.practicum.commerce.store.enums.ProductCategory;
import ru.yandex.practicum.commerce.store.enums.QuantityState;
import ru.yandex.practicum.commerce.util.PageableFactory;
import ru.yandex.practicum.service.ProductService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ShoppingStoreController implements ShoppingStoreApi {

    private final ProductService productService;
    private final PageableFactory pageableFactory;

    @Override
    public Page<ProductDto> getProducts(
            ProductCategory category,
            int page,
            int size,
            List<String> sort
    ) {
        log.info("ENTER getProducts: category={}, page={}, size={}, sort={}", category, page, size, sort);
        Pageable pageable = pageableFactory.from(page, size, sort);
        return productService.getProductsByCategory(category, pageable);
    }

    @Override
    public ProductDto createNewProduct(@Valid ProductDto productDto) {
        log.info("ENTER createNewProduct: name={}, category={}, price={}",
                productDto.getProductName(), productDto.getProductCategory(), productDto.getPrice());
        ProductDto created = productService.addProduct(productDto);
        log.info("EXIT createNewProduct: productId={}", created.getProductId());
        return created;
    }

    @Override
    public ProductDto updateProduct(@Valid ProductDto productDto) {
        log.info("ENTER updateProduct: productId={}", productDto.getProductId());
        ProductDto updated = productService.updateProduct(productDto);
        log.info("EXIT updateProduct: productId={}", updated.getProductId());
        return updated;
    }

    @Override
    public boolean removeProductFromStore(UUID productId) {
        log.info("ENTER removeProductFromStore: productId={}", productId);
        boolean ok = productService.removeProductFromStore(productId);
        log.info("EXIT removeProductFromStore: productId={}, ok={}", productId, ok);
        return ok;
    }

    @Override
    public boolean setProductQuantityState(@RequestParam UUID productId, QuantityState quantityState) {
        log.info("ENTER setProductQuantityState: productId={}, quantityState={}", productId, quantityState); // [ИЗМЕНЕНИЕ]
        boolean ok = productService.setQuantityState(productId, quantityState);
        log.info("EXIT setProductQuantityState: productId={}, ok={}", productId, ok); // [ИЗМЕНЕНИЕ]
        return ok;
    }

    @Override
    public ProductDto getProduct(UUID productId) {
        log.info("ENTER getProduct: productId={}", productId);
        ProductDto dto = productService.getProductById(productId);
        log.info("EXIT getProduct: productId={}, name={}", productId, dto.getProductName());
        return dto;
    }

    @PostMapping("/prices")
    public List<ProductPriceDto> getPrices(@RequestBody List<UUID> productIds) {
        return productService.getPrices(productIds);
    }
}
