package com.agrishop.service;

import com.agrishop.dto.ProductDTO;
import com.agrishop.dto.ProductImageDTO;
import com.agrishop.dto.PageRequestDTO;
import com.agrishop.dto.PageResponseDTO;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface ProductServiceLocal {
    List<ProductDTO> getAllActiveProducts();
    PageResponseDTO<ProductDTO> getProductsWithPagination(PageRequestDTO request, Long categoryIdFilter);
    void createProduct(ProductDTO dto);
    void updateProduct(ProductDTO dto);
    void deleteProduct(Long id);
    boolean hasLinkedData(Long productId);
    void hardDeleteProduct(Long productId) throws Exception;
    ProductDTO getProductById(Long id);
    
    // Gallery Image management
    void addProductImage(Long productId, String imageUrl, Integer displayOrder, boolean isPrimary);
    void deleteProductImage(Long imageId);
    void setPrimaryProductImage(Long productId, Long imageId);
    List<ProductImageDTO> getProductImages(Long productId);
}