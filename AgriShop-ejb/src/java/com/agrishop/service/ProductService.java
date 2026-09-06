package com.agrishop.service;

import com.agrishop.dto.ProductDTO;
import com.agrishop.dto.PageRequestDTO;
import com.agrishop.dto.PageResponseDTO;
import com.agrishop.entity.Category;
import com.agrishop.entity.Product;
import com.agrishop.repository.CategoryRepositoryLocal;
import com.agrishop.repository.ProductRepositoryLocal;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class ProductService implements ProductServiceLocal {

    @EJB
    private ProductRepositoryLocal productRepository;

    @EJB
    private CategoryRepositoryLocal categoryRepository;

    @EJB
    private com.agrishop.repository.SupplierRepositoryLocal supplierRepository;

    @Override
    public List<ProductDTO> getAllActiveProducts() {
        List<Product> products = productRepository.findActiveProducts();
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public PageResponseDTO<ProductDTO> getProductsWithPagination(PageRequestDTO request, Long categoryIdFilter) {
        List<Product> products = productRepository.findWithPagination(request, categoryIdFilter);
        long totalRecords = productRepository.countWithPagination(request, categoryIdFilter);
        
        List<ProductDTO> dtoList = products.stream().map(this::convertToDTO).collect(Collectors.toList());
        return new PageResponseDTO<>(dtoList, totalRecords);
    }

    @Override
    public void createProduct(ProductDTO dto) {
        Product product = new Product();
        Category category = categoryRepository.findById(dto.getCategoryId());
        com.agrishop.entity.Supplier supplier = null;
        if (dto.getSupplierId() != null) {
            supplier = supplierRepository.findById(dto.getSupplierId());
        }
        
        product.setProductCode(dto.getProductCode());
        product.setCategory(category);
        product.setSupplier(supplier);
        product.setName(dto.getName());
        product.setUnit(dto.getUnit());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setDescription(dto.getDescription());
        product.setImageUrl(dto.getImageUrl());
        product.setIsDeleted(false);
        productRepository.create(product);
    }

    @Override
    public void updateProduct(ProductDTO dto) {
        Product product = productRepository.findById(dto.getId());
        if (product != null && !product.getIsDeleted()) {
            Category category = categoryRepository.findById(dto.getCategoryId());
            com.agrishop.entity.Supplier supplier = null;
            if (dto.getSupplierId() != null) {
                supplier = supplierRepository.findById(dto.getSupplierId());
            }
            product.setCategory(category);
            product.setSupplier(supplier);
            product.setName(dto.getName());
            product.setUnit(dto.getUnit());
            product.setPrice(dto.getPrice());
            product.setStockQuantity(dto.getStockQuantity());
            product.setDescription(dto.getDescription());
            product.setImageUrl(dto.getImageUrl());
            productRepository.update(product);
        }
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id);
        if (product != null) {
            product.setIsDeleted(true);
            productRepository.update(product);
        }
    }
    
    private ProductDTO convertToDTO(Product p) {
        ProductDTO dto = new ProductDTO();
        dto.setId(p.getId());
        dto.setProductCode(p.getProductCode());
        if (p.getCategory() != null) {
            dto.setCategoryId(p.getCategory().getId());
            dto.setCategoryName(p.getCategory().getName());
        }
        if (p.getSupplier() != null) {
            dto.setSupplierId(p.getSupplier().getId());
            dto.setSupplierName(p.getSupplier().getName());
        }
        dto.setName(p.getName());
        dto.setUnit(p.getUnit());
        dto.setPrice(p.getPrice());
        dto.setStockQuantity(p.getStockQuantity());
        dto.setDescription(p.getDescription());
        dto.setImageUrl(p.getImageUrl());
        return dto;
    }
}