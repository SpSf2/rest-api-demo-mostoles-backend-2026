package com.example.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.example.dto.ProductDto;
import com.example.entities.Product;

/**
 * Mapper de MapStruct para convertir entre la entidad de persistencia Product y
 * el DTO de presentacion ProductDto.
 *
 * componentModel = SPRING hace que MapStruct genere una implementacion
 * (ProductMapperImpl) anotada con @Component, por lo que Spring la detecta y se
 * puede inyectar por constructor.
 *
 * El mapeo de la presentacion (Presentation -> PresentationDto) se genera
 * automaticamente porque ambos tipos tienen propiedades con el mismo nombre.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {

    // Entidad de persistencia -> DTO de presentacion
    ProductDto toDto(Product product);

    List<ProductDto> toDtoList(List<Product> products);

    // DTO de presentacion -> Entidad de persistencia
    Product toEntity(ProductDto productDto);
}
