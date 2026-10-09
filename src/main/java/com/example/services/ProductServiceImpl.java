package com.example.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.dao.ProductDao;
import com.example.dto.ProductDto;
import com.example.entities.Product;
import com.example.mappers.ProductMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    // Hay que inyectar, por constructor, la dependencia de ProductDao y el mapper
    // de MapStruct que convierte entidad <-> DTO
    private final ProductDao productDao;
    private final ProductMapper productMapper;

    @Override
    public Page<ProductDto> findAll(Pageable pageable) {
        // Page.map conserva la informacion de paginacion y transforma el contenido
        return productDao.findAll(pageable).map(productMapper::toDto);
    }

    @Override
    public List<ProductDto> findAll(Sort sort) {
        return productMapper.toDtoList(productDao.findAll(sort));
    }

    @Override
    public ProductDto findById(int id) {
        return productMapper.toDto(productDao.findById(id));
    }

    @Override
    public ProductDto save(Product product) {
        return productMapper.toDto(productDao.save(product));
    }

    @Override
    public void delete(int id) {
        productDao.deleteById(id);
    }

    @Override
    public List<ProductDto> findAll() {
        return productMapper.toDtoList(productDao.findAll());
    }

}
