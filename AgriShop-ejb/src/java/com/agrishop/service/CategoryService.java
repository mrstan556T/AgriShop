package com.agrishop.service;

import com.agrishop.dto.CategoryDTO;
import com.agrishop.entity.Category;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class CategoryService implements CategoryServiceLocal {

    @PersistenceContext(unitName = "AgriShopPU")
    private EntityManager em;

    @Override
    public List<CategoryDTO> getAllActiveCategories() {
        List<Category> categories = em.createQuery("SELECT c FROM Category c WHERE c.isDeleted = false ORDER BY c.id DESC", Category.class)
                                      .getResultList();
        
        return categories.stream().map(c -> {
            CategoryDTO dto = new CategoryDTO();
            dto.setId(c.getId());
            dto.setCode(c.getCode());
            dto.setName(c.getName());
            dto.setDescription(c.getDescription());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public void createCategory(CategoryDTO dto) {
        // Tự động sinh Business Code: CAT- + Timestamp (hoặc Sequence)
        String generatedCode = "CAT-" + System.currentTimeMillis();
        
        Category category = new Category();
        category.setCode(generatedCode);
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setIsDeleted(false);
        em.persist(category);
    }

    @Override
    public void updateCategory(CategoryDTO dto) {
        Category category = em.find(Category.class, dto.getId());
        if (category != null && !category.getIsDeleted()) {
            category.setName(dto.getName());
            category.setDescription(dto.getDescription());
            em.merge(category);
        }
    }

    @Override
    public void deleteCategory(Integer id) {
        // Ràng buộc toàn vẹn: Không cho phép xóa nếu có sản phẩm đang hoạt động
        Long activeProductsCount = em.createQuery(
            "SELECT COUNT(p) FROM Product p WHERE p.category.id = :catId AND p.isDeleted = false", Long.class)
            .setParameter("catId", id)
            .getSingleResult();
            
        if (activeProductsCount > 0) {
            throw new RuntimeException("Lỗi: Không thể xóa danh mục đang chứa sản phẩm hoạt động.");
        }

        Category category = em.find(Category.class, id);
        if (category != null) {
            category.setIsDeleted(true);
            em.merge(category);
        }
    }
}