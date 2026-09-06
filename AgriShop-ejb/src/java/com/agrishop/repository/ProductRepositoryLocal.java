package com.agrishop.repository;

import com.agrishop.entity.Product;
import com.agrishop.dto.PageRequestDTO;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface ProductRepositoryLocal {
    Product findById(Long id);
    List<Product> findAll();
    List<Product> findActiveProducts();
    void create(Product product);
    void update(Product product);
    void delete(Product product);
    List<Product> findWithPagination(PageRequestDTO request, Long categoryIdFilter);
    long countWithPagination(PageRequestDTO request, Long categoryIdFilter);
    long countByCategoryId(Long categoryId);
    long countBySupplierId(Long supplierId);
}
