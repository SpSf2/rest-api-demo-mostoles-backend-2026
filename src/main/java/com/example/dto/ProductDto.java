package com.example.dto;

import java.math.BigDecimal;

/**
 * DTO de producto que desacopla la capa de presentacion (controladores/API) de
 * la capa de persistencia (entidad JPA Product).
 */
public record ProductDto(

    int id,
    String name,
    String description,
    int stock,
    BigDecimal price,
    String productImage,
    PresentationDto presentation

) {}
