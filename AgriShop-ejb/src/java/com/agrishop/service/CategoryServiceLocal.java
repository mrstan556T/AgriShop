package com.agrishop.service;

import com.agrishop.dto.CategoryDTO;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface CategoryServiceLocal {
    List<CategoryDTO> getAllActiveCategories();
    void createCategory(CategoryDTO dto);
    void updateCategory(CategoryDTO dto);
    void deleteCategory(Integer id); // Thực hiện Soft Delete
}