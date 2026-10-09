package com.example.mappers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import com.example.controllers.ProductController;
import com.example.dto.ProductDto;

/**
 * Componente Spring que centraliza TODOS los enlaces HATEOAS de los productos.
 *
 * Hereda de {@link RepresentationModelAssemblerSupport}, que es justamente la
 * clase pensada para eliminar el codigo repetitivo del montaje manual de
 * enlaces. A partir de aqui los controladores solo tienen que llamar a
 * toModel(...) o toCollectionModel(...) y ya reciben el/los recurso(s) con sus
 * enlaces.
 *
 * Como la capa de servicio ya devuelve ProductDto, el assembler no vuelve a
 * mapear: solo decora el DTO con los enlaces:
 *
 *  - self      -> GET /products/{id}
 *  - productos -> GET /products
 */
@Component
public class ProductAssembler extends RepresentationModelAssemblerSupport<ProductDto, ProductDto> {

    public ProductAssembler() {
        super(ProductController.class, ProductDto.class);
    }

    @Override
    public ProductDto toModel(ProductDto productDto) {

        productDto.add(
                linkTo(methodOn(ProductController.class).findProductById(productDto.getId())).withSelfRel(),
                linkTo(methodOn(ProductController.class).dameProductos(null, null)).withRel("productos"));

        return productDto;
    }

    @Override
    public CollectionModel<ProductDto> toCollectionModel(Iterable<? extends ProductDto> entities) {

        CollectionModel<ProductDto> collectionModel = super.toCollectionModel(entities);

        // Enlace self de la coleccion, tambien centralizado aqui
        collectionModel.add(linkTo(methodOn(ProductController.class).dameProductos(null, null)).withSelfRel());

        return collectionModel;
    }

}
