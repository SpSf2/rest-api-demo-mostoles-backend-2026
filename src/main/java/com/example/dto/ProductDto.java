package com.example.dto;

import java.math.BigDecimal;

import org.springframework.hateoas.RepresentationModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de producto que desacopla la capa de presentacion (controladores/API) de
 * la capa de persistencia (entidad JPA Product).
 *
 * Hereda de {@link RepresentationModel} para que el propio modelo de
 * presentacion transporte los enlaces HATEOAS. Asi los end points ya no tienen
 * que envolver el DTO en un EntityModel ni construir los enlaces a mano.
 *
 * Nota: al necesitar heredar de una clase no puede seguir siendo un record (los
 * record no pueden extender clases), por eso se implementa como una clase
 * anotada con Lombok.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto extends RepresentationModel<ProductDto> {

    private int id;
    private String name;
    private String description;
    private int stock;
    private BigDecimal price;
    private String productImage;
    private PresentationDto presentation;

}
