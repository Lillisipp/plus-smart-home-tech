package ru.yandex.practicum.mapper;

import org.mapstruct.MappingTarget;
import ru.yandex.practicum.commerce.store.dto.ProductDto;
import ru.yandex.practicum.model.ProductEntity;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductDto toDto(ProductEntity entity);
    ProductEntity toEntity(ProductDto dto);
    void updateEntity(ProductDto dto, @MappingTarget ProductEntity entity);

}
