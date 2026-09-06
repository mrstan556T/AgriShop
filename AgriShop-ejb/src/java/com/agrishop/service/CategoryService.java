package com.agrishop.service;

import com.agrishop.dto.CategoryDTO;
import com.agrishop.dto.PageRequestDTO;
import com.agrishop.dto.PageResponseDTO;
import com.agrishop.entity.Category;
import com.agrishop.repository.CategoryRepositoryLocal;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class CategoryService implements CategoryServiceLocal {

    @EJB
    private CategoryRepositoryLocal categoryRepository;

    @EJB
    private com.agrishop.repository.ProductRepositoryLocal productRepository;

    @Override
    public List<CategoryDTO> getAllActiveCategories() {
        List<Category> categories = categoryRepository.findActiveCategories();
        return categories.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public PageResponseDTO<CategoryDTO> getCategoriesWithPagination(PageRequestDTO request) {
        List<Category> categories = categoryRepository.findWithPagination(request);
        long totalRecords = categoryRepository.countWithPagination(request);
        
        List<CategoryDTO> dtoList = categories.stream().map(this::convertToDTO).collect(Collectors.toList());
        return new PageResponseDTO<>(dtoList, totalRecords);
    }

    @Override
    public void createCategory(CategoryDTO dto) {
        Category category = new Category();
        category.setCode(dto.getCode());
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setIsDeleted(false);
        categoryRepository.create(category);
    }

    @Override
    public void updateCategory(CategoryDTO dto) {
        Category category = categoryRepository.findById(dto.getId());
        if (category != null && !category.getIsDeleted()) {
            category.setCode(dto.getCode());
            category.setName(dto.getName());
            category.setDescription(dto.getDescription());
            categoryRepository.update(category);
        }
    }

    @Override
    public void deleteCategory(Long id) {
        if (productRepository.countByCategoryId(id) > 0) {
            throw new com.agrishop.exception.BusinessException("Không thể xóa danh mục này vì vẫn còn sản phẩm đang hoạt động.");
        }
        Category category = categoryRepository.findById(id);
        if (category != null) {
            category.setIsDeleted(true);
            categoryRepository.update(category);
        }
    }
    
    private CategoryDTO convertToDTO(Category c) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(c.getId());
        dto.setCode(c.getCode());
        dto.setName(c.getName());
        dto.setDescription(c.getDescription());
        return dto;
    }
}