package com.agrishop.web.bean;

import com.agrishop.dto.CategoryDTO;
import com.agrishop.dto.ProductDTO;
import com.agrishop.dto.PageRequestDTO;
import com.agrishop.dto.PageResponseDTO;
import com.agrishop.service.CategoryServiceLocal;
import com.agrishop.service.ProductServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.util.List;
import java.util.Map;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;

@Named("productBean")
@ViewScoped
public class ProductBean extends AbstractCrudBean<ProductDTO> {

    @EJB
    private ProductServiceLocal productService;

    @EJB
    private CategoryServiceLocal categoryService;

    @EJB
    private com.agrishop.service.SupplierServiceLocal supplierService;

    private List<CategoryDTO> categories;
    private List<com.agrishop.dto.SupplierDTO> suppliers;
    private Long categoryIdFilter;

    @PostConstruct
    public void init() {
        categories = categoryService.getAllActiveCategories();
        suppliers = supplierService.getAllActiveSuppliers();
        openNew();
        lazyModel = new LazyDataModel<ProductDTO>() {
            @Override
            public int count(Map<String, FilterMeta> filterBy) {
                return 0;
            }

            @Override
            public List<ProductDTO> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
                PageRequestDTO request = new PageRequestDTO();
                int size = pageSize > 0 ? pageSize : 10;
                request.setPageIndex(first / size);
                request.setPageSize(size);
                request.setSearchKeyword(globalFilter);
                
                if (sortBy != null && !sortBy.isEmpty()) {
                    SortMeta sm = sortBy.values().iterator().next();
                    request.setSortField(sm.getField());
                    request.setSortOrder(sm.getOrder().isAscending() ? "ASC" : "DESC");
                }
                
                PageResponseDTO<ProductDTO> response = productService.getProductsWithPagination(request, categoryIdFilter);
                this.setRowCount((int) response.getTotalRecords());
                return response.getData();
            }
        };
    }

    @Override
    protected void initItem() {
        this.currentItem = new ProductDTO();
    }

    @Override
    protected void performSave() throws Exception {
        if (editMode) {
            productService.updateProduct(currentItem);
        } else {
            productService.createProduct(currentItem);
        }
    }

    @Override
    protected void performDelete(ProductDTO item) throws Exception {
        productService.deleteProduct(item.getId());
    }

    @Override
    protected String getItemName() {
        return "Sản phẩm";
    }

    public List<CategoryDTO> getCategories() { return categories; }
    public void setCategories(List<CategoryDTO> categories) { this.categories = categories; }
    public List<com.agrishop.dto.SupplierDTO> getSuppliers() { return suppliers; }
    public void setSuppliers(List<com.agrishop.dto.SupplierDTO> suppliers) { this.suppliers = suppliers; }
    public Long getCategoryIdFilter() { return categoryIdFilter; }
    public void setCategoryIdFilter(Long categoryIdFilter) { this.categoryIdFilter = categoryIdFilter; }
}