package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.commerce.warehouse.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.model.WarehouseEntity;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {

    @Mapping(source = "dimension.width", target = "width")
    @Mapping(source = "dimension.height", target = "height")
    @Mapping(source = "dimension.depth", target = "depth")
    @Mapping(target = "quantity", constant = "0L")
    WarehouseEntity toEntity(NewProductInWarehouseRequest request);

}