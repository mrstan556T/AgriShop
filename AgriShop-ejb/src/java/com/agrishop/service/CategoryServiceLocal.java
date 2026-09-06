package com.agrishop.service;

import com.agrishop.dto.CategoryDTO;
import com.agrishop.dto.PageRequestDTO;
import com.agrishop.dto.PageResponseDTO;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface CategoryServiceLocal {
    List<CategoryDTO> getAllActiveCategories();
    PageResponseDTO<CategoryDTO> getCategoriesWithPagination(PageRequestDTO request);
    void createCategory(CategoryDTO dto);
    void updateCategory(CategoryDTO dto);
    void deleteCategory(Long id); // Thực hiện Soft Delete
}