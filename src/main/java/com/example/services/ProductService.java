package com.example.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.dto.ProductDto;
import com.example.entities.Product;

public interface ProductService {
    Page<ProductDto> findAll(Pageable pageable);
    List<ProductDto> findAll(Sort sort);
    ProductDto findById(int id);
    ProductDto save(Product product);
    void delete(int id);
    List<ProductDto> findAll();
}
