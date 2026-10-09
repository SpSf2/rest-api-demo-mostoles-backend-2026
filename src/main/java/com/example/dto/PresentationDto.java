package com.example.dto;

/**
 * DTO de presentacion que expone unicamente los datos que la capa de
 * presentacion necesita (id y nombre), evitando acoplar la API a la entidad
 * JPA Presentation.
 */
public record PresentationDto(

    int id,
    String name

) {}
