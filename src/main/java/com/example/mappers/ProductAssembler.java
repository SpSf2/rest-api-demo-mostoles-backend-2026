package com.example.mappers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.example.controllers.ProductController;
import com.example.dto.ProductDto;

/**
 * Componente Spring que centraliza TODOS los enlaces HATEOAS de los productos.
 *
 * Hereda de {@link RepresentationModelAssemblerSupport}, que es justamente la
 * clase pensada para eliminar el codigo repetitivo del montaje manual de
 * enlaces. A partir de aqui los controladores solo tienen que llamar a
 * toModel(...), toCollectionModel(...) o toPagedModel(...) y ya reciben el/los
 * recurso(s) con sus enlaces.
 *
 * Como la capa de servicio ya devuelve ProductDto, el assembler no vuelve a
 * mapear: solo decora el DTO con los enlaces.
 *
 * Enlaces generados (los de mutacion solo se anaden si el usuario es ADMIN):
 *
 *  - Item:       self -> GET /products/{id}
 *                update -> PUT /products/{id}        (solo ADMIN)
 *                delete -> DELETE /products/{id}     (solo ADMIN)
 *                productos -> GET /products
 *  - Coleccion:  self -> GET /products{?page,size}
 *                create -> POST /products            (solo ADMIN)
 *  - Paginado:   self, first, prev, next, last -> GET /products?page=..&size=..
 *                create -> POST /products            (solo ADMIN)
 */
@Component
public class ProductAssembler extends RepresentationModelAssemblerSupport<ProductDto, ProductDto> {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String REL_PRODUCTOS = "productos";
    private static final String REL_CREATE = "create";
    private static final String REL_UPDATE = "update";
    private static final String REL_DELETE = "delete";
    private static final String REL_FIRST = "first";
    private static final String REL_PREV = "prev";
    private static final String REL_NEXT = "next";
    private static final String REL_LAST = "last";

    public ProductAssembler() {
        super(ProductController.class, ProductDto.class);
    }

    /**
     * Enlaces de un unico producto, incluidas las operaciones de mutacion que
     * puede realizar el cliente (actualizar y eliminar).
     */
    @Override
    public ProductDto toModel(ProductDto productDto) {

        productDto.add(linkTo(methodOn(ProductController.class).findProductById(productDto.getId())).withSelfRel());

        // Los enlaces de mutacion solo se exponen al rol ADMIN
        if (isAdmin()) {
            productDto.add(updateLink(productDto.getId()));
            productDto.add(linkTo(methodOn(ProductController.class).deleteProducto(productDto.getId())).withRel(REL_DELETE));
        }

        productDto.add(linkTo(methodOn(ProductController.class).dameProductos(null, null)).withRel(REL_PRODUCTOS));

        return productDto;
    }

    /**
     * Enlaces de la coleccion completa (sin paginar): self + operacion de alta
     * (POST, solo ADMIN) para que el cliente sepa como crear un producto.
     */
    @Override
    public CollectionModel<ProductDto> toCollectionModel(Iterable<? extends ProductDto> entities) {

        CollectionModel<ProductDto> collectionModel = super.toCollectionModel(entities);

        collectionModel.add(linkTo(methodOn(ProductController.class).dameProductos(null, null)).withSelfRel());

        if (isAdmin()) {
            collectionModel.add(createLink());
        }

        return collectionModel;
    }

    /**
     * Enlaces de la coleccion paginada. Ademas del self, recibe la operacion de
     * alta (create) y los enlaces de navegacion dinamicos (first, prev, next,
     * last) calculados a partir de los metadatos de la pagina.
     */
    public PagedModel<ProductDto> toPagedModel(Page<ProductDto> page) {

        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(
                page.getSize(), page.getNumber(), page.getTotalElements(), page.getTotalPages());

        List<ProductDto> content = page.getContent().stream().map(this::toModel).toList();

        PagedModel<ProductDto> pagedModel = PagedModel.of(content, metadata);

        int number = page.getNumber();
        int size = page.getSize();
        int lastPage = Math.max(page.getTotalPages() - 1, 0);

        // self apuntando a la pagina actual
        pagedModel.add(linkTo(methodOn(ProductController.class).dameProductos(number, size)).withSelfRel());
        // operacion de alta (POST, solo ADMIN)
        if (isAdmin()) {
            pagedModel.add(createLink());
        }
        // primera y ultima pagina
        pagedModel.add(linkTo(methodOn(ProductController.class).dameProductos(0, size)).withRel(REL_FIRST));
        pagedModel.add(linkTo(methodOn(ProductController.class).dameProductos(lastPage, size)).withRel(REL_LAST));

        // navegacion relativa (solo si existen)
        if (number > 0) {
            pagedModel.add(linkTo(methodOn(ProductController.class).dameProductos(number - 1, size)).withRel(REL_PREV));
        }
        if (number < lastPage) {
            pagedModel.add(linkTo(methodOn(ProductController.class).dameProductos(number + 1, size)).withRel(REL_NEXT));
        }

        return pagedModel;
    }

    /**
     * Indica si el usuario autenticado en la peticion actual tiene el rol ADMIN.
     * Se consulta el SecurityContext, que Spring Security rellena a partir del
     * token JWT en cada peticion.
     */
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(ROLE_ADMIN::equals);
    }

    /**
     * Enlace de alta de producto (POST /products). El metodo del controlador
     * declara IOException, asi que la construccion se aisla aqui.
     */
    private Link createLink() {
        try {
            return linkTo(methodOn(ProductController.class).saveProduct(null, null, null)).withRel(REL_CREATE);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo construir el enlace de creacion de producto", ex);
        }
    }

    /**
     * Enlace de actualizacion de producto (PUT /products/{id}). El metodo del
     * controlador declara IOException, asi que la construccion se aisla aqui.
     */
    private Link updateLink(int id) {
        try {
            return linkTo(methodOn(ProductController.class).updateProduct(null, null, null, id)).withRel(REL_UPDATE);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo construir el enlace de actualizacion de producto", ex);
        }
    }

}
