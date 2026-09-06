package com.agrishop.service;

import com.agrishop.dto.ProductDTO;
import com.agrishop.entity.Category;
import com.agrishop.entity.Product;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class ProductService implements ProductServiceLocal {

    @PersistenceContext(unitName = "AgriShopPU")
    private EntityManager em;

    @Override
    public List<ProductDTO> getAllActiveProducts() {
        List<Product> products = em.createQuery(
                "SELECT p FROM Product p JOIN FETCH p.category WHERE p.isDeleted = false", Product.class)
                .getResultList();

        return products.stream().map(p -> {
            ProductDTO dto = new ProductDTO();
            dto.setId(p.getId());
            dto.setProductCode(p.getProductCode());
            dto.setCategoryId(p.getCategory().getId());
            dto.setCategoryName(p.getCategory().getName());
            dto.setName(p.getName());
            dto.setUnit(p.getUnit());
            dto.setPrice(p.getPrice());
            dto.setStockQuantity(p.getStockQuantity());
            dto.setDescription(p.getDescription());
            dto.setImageUrl(p.getImageUrl());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public void createProduct(ProductDTO dto) {
        Product product = new Product();
        Category category = em.find(Category.class, dto.getCategoryId());
        
        product.setProductCode(dto.getProductCode());
        product.setCategory(category);
        product.setName(dto.getName());
        product.setUnit(dto.getUnit());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setDescription(dto.getDescription());
        product.setIsDeleted(false);
        em.persist(product);
    }

    @Override
    public void updateProduct(ProductDTO dto) {
        Product product = em.find(Product.class, dto.getId());
        if (product != null && !product.getIsDeleted()) {
            Category category = em.find(Category.class, dto.getCategoryId());
            product.setCategory(category);
            product.setName(dto.getName());
            product.setUnit(dto.getUnit());
            product.setPrice(dto.getPrice());
            product.setStockQuantity(dto.getStockQuantity());
            product.setDescription(dto.getDescription());
            em.merge(product);
        }
    }

    @Override
    public void deleteProduct(Integer id) {
        Product product = em.find(Product.class, id);
        if (product != null) {
            product.setIsDeleted(true);
            em.merge(product);
        }
    }
}