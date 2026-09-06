package com.agrishop.service;

import com.agrishop.dto.ProductDTO;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface ProductServiceLocal {
    List<ProductDTO> getAllActiveProducts();
    void createProduct(ProductDTO dto);
    void updateProduct(ProductDTO dto);
    void deleteProduct(Integer id);
}