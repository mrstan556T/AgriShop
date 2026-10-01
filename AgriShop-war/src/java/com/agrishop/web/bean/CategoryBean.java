package com.agrishop.web.bean;

import com.agrishop.dto.CategoryDTO;
import com.agrishop.dto.PageRequestDTO;
import com.agrishop.dto.PageResponseDTO;
import com.agrishop.service.CategoryServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.util.List;
import java.util.Map;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;

@Named("categoryBean")
@ViewScoped
public class CategoryBean extends AbstractCrudBean<CategoryDTO> {

    @EJB
    private CategoryServiceLocal categoryService;

    @PostConstruct
    public void init() {
        openNew();
        lazyModel = new LazyDataModel<CategoryDTO>() {
            @Override
            public int count(Map<String, FilterMeta> filterBy) {
                return 0;
            }

            @Override
            public List<CategoryDTO> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
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
                
                PageResponseDTO<CategoryDTO> response = categoryService.getCategoriesWithPagination(request);
                this.setRowCount((int) response.getTotalRecords());
                return response.getData();
            }
        };
    }

    @Override
    protected void initItem() {
        this.currentItem = new CategoryDTO();
    }

    @Override
    protected void performSave() throws Exception {
        if (currentItem.getCode() == null || currentItem.getCode().trim().isEmpty()) {
            throw new com.agrishop.exception.BusinessException("Vui lòng nhập mã danh mục");
        }
        if (currentItem.getName() == null || currentItem.getName().trim().isEmpty()) {
            throw new com.agrishop.exception.BusinessException("Vui lòng nhập tên danh mục");
        }
        if (editMode) {
            categoryService.updateCategory(currentItem);
        } else {
            categoryService.createCategory(currentItem);
        }
    }

    @Override
    protected void performDelete(CategoryDTO item) throws Exception {
        categoryService.deleteCategory(item.getId());
    }

    @Override
    protected String getItemName() {
        return "Danh mục";
    }
}