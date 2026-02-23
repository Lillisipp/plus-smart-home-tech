package ru.yandex.practicum.mapper;

import org.mapstruct.*;
import ru.yandex.practicum.commerce.store.dto.ProductDto;
import ru.yandex.practicum.model.ProductEntity;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductDto toDto(ProductEntity entity);
    ProductEntity toEntity(ProductDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(ProductDto dto, @MappingTarget ProductEntity entity);

}
