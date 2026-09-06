package com.agrishop.web.bean;

import com.agrishop.dto.CategoryDTO;
import com.agrishop.dto.ProductDTO;
import com.agrishop.service.CategoryServiceLocal;
import com.agrishop.service.ProductServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("productBean")
@ViewScoped
public class ProductBean implements Serializable {

    @EJB
    private ProductServiceLocal productService;

    @EJB
    private CategoryServiceLocal categoryService;

    private List<ProductDTO> products;
    private List<CategoryDTO> categories;
    private ProductDTO currentProduct;
    private boolean isEditMode;

    @PostConstruct
    public void init() {
        loadData();
        resetForm();
    }

    private void loadData() {
        products = productService.getAllActiveProducts();
        categories = categoryService.getAllActiveCategories();
    }

    public void resetForm() {
        currentProduct = new ProductDTO();
        isEditMode = false;
    }

    public void prepareEdit(ProductDTO product) {
        this.currentProduct = product;
        this.isEditMode = true;
    }

    public void save() {
        try {
            if (isEditMode) {
                productService.updateProduct(currentProduct);
                addMessage(FacesMessage.SEVERITY_INFO, "Cập nhật sản phẩm thành công!");
            } else {
                productService.createProduct(currentProduct);
                addMessage(FacesMessage.SEVERITY_INFO, "Thêm sản phẩm thành công!");
            }
            loadData();
            resetForm();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Lỗi: " + e.getMessage());
        }
    }

    public void delete(Integer id) {
        try {
            productService.deleteProduct(id);
            loadData();
            addMessage(FacesMessage.SEVERITY_INFO, "Xóa sản phẩm thành công!");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Lỗi khi xóa: " + e.getMessage());
        }
    }

    private void addMessage(FacesMessage.Severity severity, String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, message, null));
    }

    public List<ProductDTO> getProducts() { return products; }
    public List<CategoryDTO> getCategories() { return categories; }
    public ProductDTO getCurrentProduct() { return currentProduct; }
    public void setCurrentProduct(ProductDTO currentProduct) { this.currentProduct = currentProduct; }
    public boolean getIsEditMode() { return isEditMode; }
}