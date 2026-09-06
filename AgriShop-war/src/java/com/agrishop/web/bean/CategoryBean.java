package com.agrishop.web.bean;

import com.agrishop.dto.CategoryDTO;
import com.agrishop.service.CategoryServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Named("categoryBean")
@ViewScoped
public class CategoryBean implements Serializable {

    @EJB
    private CategoryServiceLocal categoryService;

    private List<CategoryDTO> allCategories;
    private List<CategoryDTO> filteredCategories;
    private CategoryDTO currentCategory;
    private boolean isEditMode;
    private String searchQuery;

    @PostConstruct
    public void init() {
        loadCategories();
        resetForm();
    }

    private void loadCategories() {
        allCategories = categoryService.getAllActiveCategories();
        filterData();
    }

    public void filterData() {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            filteredCategories = allCategories;
        } else {
            String lowerQuery = searchQuery.toLowerCase();
            filteredCategories = allCategories.stream()
                .filter(c -> c.getName().toLowerCase().contains(lowerQuery) || 
                             c.getCode().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
        }
    }

    public void resetForm() {
        currentCategory = new CategoryDTO();
        isEditMode = false;
    }

    public void prepareEdit(CategoryDTO category) {
        this.currentCategory = category;
        this.isEditMode = true;
    }

    public void save() {
        try {
            if (isEditMode) {
                categoryService.updateCategory(currentCategory);
                addMessage(FacesMessage.SEVERITY_INFO, "Cập nhật thành công!");
            } else {
                categoryService.createCategory(currentCategory);
                addMessage(FacesMessage.SEVERITY_INFO, "Thêm mới thành công!");
            }
            loadCategories();
            resetForm();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
        }
    }

    public void delete(Integer id) {
        try {
            categoryService.deleteCategory(id);
            loadCategories();
            addMessage(FacesMessage.SEVERITY_INFO, "Xóa thành công!");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
        }
    }

    private void addMessage(FacesMessage.Severity severity, String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, message, null));
    }

    // Getters / Setters
    public List<CategoryDTO> getFilteredCategories() { return filteredCategories; }
    public CategoryDTO getCurrentCategory() { return currentCategory; }
    public void setCurrentCategory(CategoryDTO currentCategory) { this.currentCategory = currentCategory; }
    public boolean getIsEditMode() { return isEditMode; }
    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }
}