package com.agrishop.repository;

import com.agrishop.entity.Category;
import com.agrishop.dto.PageRequestDTO;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface CategoryRepositoryLocal {
    Category findById(Long id);
    List<Category> findAll();
    List<Category> findActiveCategories();
    void create(Category category);
    void update(Category category);
    void delete(Category category);
    List<Category> findWithPagination(PageRequestDTO request);
    long countWithPagination(PageRequestDTO request);
}
