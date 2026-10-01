package com.agrishop.web.bean;

import com.agrishop.dto.CategoryDTO;
import com.agrishop.dto.ProductDTO;
import com.agrishop.dto.PageRequestDTO;
import com.agrishop.dto.PageResponseDTO;
import com.agrishop.service.CategoryServiceLocal;
import com.agrishop.service.ProductServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.file.UploadedFile;
import com.agrishop.web.util.FileUploadUtil;

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
    private CategoryDTO selectedCategory;
    private com.agrishop.dto.SupplierDTO selectedSupplier;
    private Long categoryIdFilter;
    private UploadedFile imageFile;
    private UploadedFile galleryFile;
    private boolean removeCurrentImage = false;

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
        this.currentItem.setUnitType("COUNT");
        this.currentItem.setStockQuantity(BigDecimal.ZERO);
        this.currentItem.setMinStock(new BigDecimal("10"));
        this.selectedCategory = null;
        this.selectedSupplier = null;
        this.imageFile = null;
        this.galleryFile = null;
        this.removeCurrentImage = false;
        generateSku();
    }

    public void generateSku() {
        if (currentItem == null) return;
        String prefix = "AGRI";
        if (selectedCategory != null && selectedCategory.getCode() != null && !selectedCategory.getCode().trim().isEmpty()) {
            prefix = selectedCategory.getCode().trim().toUpperCase();
        } else if (currentItem.getName() != null && !currentItem.getName().trim().isEmpty()) {
            String clean = currentItem.getName().trim().replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
            if (clean.length() >= 3) {
                prefix = clean.substring(0, 3);
            }
        }
        int randomNum = (int)(Math.random() * 900000) + 100000;
        currentItem.setProductCode(prefix + "-" + randomNum);
    }

    @Override
    public void prepareEdit(ProductDTO item) {
        super.prepareEdit(item);
        this.imageFile = null;
        this.galleryFile = null;
        this.removeCurrentImage = false;
        if (item.getCategoryId() != null && categories != null) {
            this.selectedCategory = categories.stream().filter(c -> c.getId().equals(item.getCategoryId())).findFirst().orElse(null);
        } else {
            this.selectedCategory = null;
        }
        if (item.getSupplierId() != null && suppliers != null) {
            this.selectedSupplier = suppliers.stream().filter(s -> s.getId().equals(item.getSupplierId())).findFirst().orElse(null);
        } else {
            this.selectedSupplier = null;
        }
        refreshCurrentProductImages();
    }

    @Override
    public void prepareView(ProductDTO item) {
        super.prepareView(item);
        this.imageFile = null;
        this.galleryFile = null;
        this.removeCurrentImage = false;
        if (item.getCategoryId() != null && categories != null) {
            this.selectedCategory = categories.stream().filter(c -> c.getId().equals(item.getCategoryId())).findFirst().orElse(null);
        } else {
            this.selectedCategory = null;
        }
        if (item.getSupplierId() != null && suppliers != null) {
            this.selectedSupplier = suppliers.stream().filter(s -> s.getId().equals(item.getSupplierId())).findFirst().orElse(null);
        } else {
            this.selectedSupplier = null;
        }
        refreshCurrentProductImages();
    }

    @Override
    protected void performSave() throws Exception {
        if (currentItem.getName() == null || currentItem.getName().trim().isEmpty()) {
            throw new com.agrishop.exception.BusinessException("Vui lòng nhập tên sản phẩm");
        }
        if (selectedCategory == null) {
            throw new com.agrishop.exception.BusinessException("Vui lòng chọn danh mục cho sản phẩm");
        }
        if (currentItem.getPrice() == null || currentItem.getPrice().doubleValue() <= 0) {
            throw new com.agrishop.exception.BusinessException("Giá bán phải lớn hơn 0");
        }
        if (!editMode) {
            if (currentItem.getStockQuantity() == null || currentItem.getStockQuantity().compareTo(BigDecimal.ZERO) < 0) {
                throw new com.agrishop.exception.BusinessException("Số lượng tồn kho không được âm");
            }
        }
        if (currentItem.getMinStock() == null || currentItem.getMinStock().compareTo(BigDecimal.ZERO) < 0) {
            currentItem.setMinStock(new BigDecimal("10"));
        }
        if (currentItem.getUnit() == null || currentItem.getUnit().trim().isEmpty()) {
            throw new com.agrishop.exception.BusinessException("Vui lòng nhập đơn vị tính");
        }
        if (currentItem.getUnitType() == null || currentItem.getUnitType().trim().isEmpty()) {
            currentItem.setUnitType("COUNT");
        }
        if (currentItem.getCostPrice() == null) {
            currentItem.setCostPrice(BigDecimal.ZERO);
        }

        if (selectedCategory != null) {
            currentItem.setCategoryId(selectedCategory.getId());
        }
        if (selectedSupplier != null) {
            currentItem.setSupplierId(selectedSupplier.getId());
        } else {
            currentItem.setSupplierId(null);
        }

        if (imageFile != null && imageFile.getSize() > 0) {
            String path = FileUploadUtil.saveFile(imageFile, "products");
            if (path != null) {
                currentItem.setImageUrl(path);
            }
        } else if (removeCurrentImage) {
            currentItem.setImageUrl(null);
        }

        if (editMode) {
            productService.updateProduct(currentItem);
        } else {
            productService.createProduct(currentItem);
        }
        refreshCurrentProductImages();
    }

    public void removeImage() {
        this.removeCurrentImage = true;
        this.imageFile = null;
        if (this.currentItem != null) {
            this.currentItem.setImageUrl(null);
        }
    }

    // Gallery operations
    public void uploadGalleryImage() {
        if (currentItem == null || currentItem.getId() == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Cảnh báo", "Vui lòng lưu sản phẩm trước khi tải lên thư viện ảnh."));
            return;
        }
        if (galleryFile != null && galleryFile.getSize() > 0) {
            try {
                String path = FileUploadUtil.saveFile(galleryFile, "products");
                int nextOrder = currentItem.getImages() != null ? currentItem.getImages().size() + 1 : 1;
                boolean isFirst = (currentItem.getImages() == null || currentItem.getImages().isEmpty());
                productService.addProductImage(currentItem.getId(), path, nextOrder, isFirst);
                refreshCurrentProductImages();
                this.galleryFile = null;
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đã thêm ảnh vào bộ sưu tập"));
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi tải ảnh", e.getMessage()));
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Cảnh báo", "Vui lòng chọn tệp ảnh để tải lên."));
        }
    }

    public void setPrimaryImage(Long imageId) {
        if (currentItem == null || currentItem.getId() == null || imageId == null) return;
        try {
            productService.setPrimaryProductImage(currentItem.getId(), imageId);
            refreshCurrentProductImages();
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đã đặt làm ảnh chính"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", e.getMessage()));
        }
    }

    public void deleteGalleryImage(Long imageId) {
        if (currentItem == null || currentItem.getId() == null || imageId == null) return;
        try {
            productService.deleteProductImage(imageId);
            refreshCurrentProductImages();
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đã xóa ảnh khỏi bộ sưu tập"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", e.getMessage()));
        }
    }

    public void refreshCurrentProductImages() {
        if (currentItem != null && currentItem.getId() != null) {
            ProductDTO p = productService.getProductById(currentItem.getId());
            if (p != null) {
                currentItem.setImages(p.getImages());
                currentItem.setImageUrl(p.getImageUrl());
            }
        }
    }

    public boolean isRemoveCurrentImage() {
        return removeCurrentImage;
    }

    public void setRemoveCurrentImage(boolean removeCurrentImage) {
        this.removeCurrentImage = removeCurrentImage;
    }

    private ProductDTO selectedProductForDelete;
    private boolean selectedProductHasLinkedData;

    public void prepareDeleteProduct(ProductDTO p) {
        System.out.println("===> ProductBean.prepareDeleteProduct called for: " + (p != null ? p.getId() + " - " + p.getName() : "null"));
        this.selectedProductForDelete = p;
        if (p != null) {
            this.selectedProductHasLinkedData = productService.hasLinkedData(p.getId());
        } else {
            this.selectedProductHasLinkedData = false;
        }
        System.out.println("===> hasLinkedData = " + this.selectedProductHasLinkedData);
    }

    public void executeDeleteProduct(jakarta.faces.event.ActionEvent event) {
        executeDeleteProduct();
    }

    public void executeDeleteProduct() {
        System.out.println("===> ProductBean.executeDeleteProduct triggered! Selected: " + (selectedProductForDelete != null ? selectedProductForDelete.getId() : "null"));
        if (selectedProductForDelete == null) {
            System.out.println("===> selectedProductForDelete is NULL! Aborting delete.");
            return;
        }
        try {
            Long pid = selectedProductForDelete.getId();
            String pname = selectedProductForDelete.getName();
            productService.deleteProduct(pid);
            if (selectedProductHasLinkedData) {
                addMessage(jakarta.faces.application.FacesMessage.SEVERITY_INFO, "Đã xóa mềm", 
                    "Sản phẩm '" + pname + "' đã được chuyển sang trạng thái ngừng bán và ẩn khỏi Cửa hàng (Lịch sử đơn hàng, kho và đánh giá được bảo toàn).");
            } else {
                addMessage(jakarta.faces.application.FacesMessage.SEVERITY_INFO, "Đã xóa vĩnh viễn", 
                    "Đã xóa hoàn toàn sản phẩm '" + pname + "' khỏi cơ sở dữ liệu.");
            }
            this.selectedProductForDelete = null;
            System.out.println("===> Product deleted successfully from ProductBean!");
        } catch (Exception e) {
            System.err.println("===> Error in executeDeleteProduct: " + e.getMessage());
            e.printStackTrace();
            addMessage(jakarta.faces.application.FacesMessage.SEVERITY_ERROR, "Lỗi", "Không thể xóa sản phẩm: " + e.getMessage());
        }
    }

    public ProductDTO getSelectedProductForDelete() { return selectedProductForDelete; }
    public void setSelectedProductForDelete(ProductDTO selectedProductForDelete) { this.selectedProductForDelete = selectedProductForDelete; }
    public boolean isSelectedProductHasLinkedData() { return selectedProductHasLinkedData; }
    public void setSelectedProductHasLinkedData(boolean selectedProductHasLinkedData) { this.selectedProductHasLinkedData = selectedProductHasLinkedData; }

    @Override
    protected void performDelete(ProductDTO item) throws Exception {
        productService.deleteProduct(item.getId());
    }

    @Override
    protected String getItemName() {
        return "Sản phẩm";
    }

    private CategoryDTO filterCategory;

    public void onFilterCategorySelect(org.primefaces.event.SelectEvent<CategoryDTO> event) {
        if (event.getObject() != null) {
            this.categoryIdFilter = event.getObject().getId();
        } else {
            this.categoryIdFilter = null;
        }
    }

    public Long getSelectedCategoryId() {
        return currentItem != null ? currentItem.getCategoryId() : null;
    }

    public void setSelectedCategoryId(Long categoryId) {
        if (currentItem != null) {
            currentItem.setCategoryId(categoryId);
            if (categoryId != null && categories != null) {
                this.selectedCategory = categories.stream().filter(c -> c.getId().equals(categoryId)).findFirst().orElse(null);
            } else {
                this.selectedCategory = null;
            }
        }
    }

    public String getSelectedCategoryName() {
        if (currentItem != null && currentItem.getCategoryId() != null && categories != null) {
            return categories.stream().filter(c -> c.getId().equals(currentItem.getCategoryId()))
                    .map(CategoryDTO::getName).findFirst().orElse("");
        }
        return "";
    }

    public Long getSelectedSupplierId() {
        return currentItem != null ? currentItem.getSupplierId() : null;
    }

    public void setSelectedSupplierId(Long supplierId) {
        if (currentItem != null) {
            currentItem.setSupplierId(supplierId);
            if (supplierId != null && suppliers != null) {
                this.selectedSupplier = suppliers.stream().filter(s -> s.getId().equals(supplierId)).findFirst().orElse(null);
            } else {
                this.selectedSupplier = null;
            }
        }
    }

    public String getSelectedSupplierName() {
        if (currentItem != null && currentItem.getSupplierId() != null && suppliers != null) {
            return suppliers.stream().filter(s -> s.getId().equals(currentItem.getSupplierId()))
                    .map(com.agrishop.dto.SupplierDTO::getName).findFirst().orElse("");
        }
        return "";
    }

    public void onCategoryFilterChange() {
        // PrimeFaces ajax listener on selectOneMenu in filter toolbar
    }

    public List<CategoryDTO> completeCategory(String query) {
        if (query == null || query.trim().isEmpty()) {
            return categories != null ? categories : categoryService.getAllActiveCategories();
        }
        return categoryService.searchByName(query.trim());
    }

    public List<com.agrishop.dto.SupplierDTO> completeSupplier(String query) {
        if (query == null || query.trim().isEmpty()) {
            return suppliers != null ? suppliers : supplierService.getAllActiveSuppliers();
        }
        return supplierService.searchByName(query.trim());
    }

    public List<CategoryDTO> getCategories() { return categories; }
    public void setCategories(List<CategoryDTO> categories) { this.categories = categories; }
    public List<com.agrishop.dto.SupplierDTO> getSuppliers() { return suppliers; }
    public void setSuppliers(List<com.agrishop.dto.SupplierDTO> suppliers) { this.suppliers = suppliers; }
    public CategoryDTO getSelectedCategory() { return selectedCategory; }
    public void setSelectedCategory(CategoryDTO selectedCategory) { this.selectedCategory = selectedCategory; }
    public com.agrishop.dto.SupplierDTO getSelectedSupplier() { return selectedSupplier; }
    public void setSelectedSupplier(com.agrishop.dto.SupplierDTO selectedSupplier) { this.selectedSupplier = selectedSupplier; }
    public Long getCategoryIdFilter() { return categoryIdFilter; }
    public void setCategoryIdFilter(Long categoryIdFilter) { this.categoryIdFilter = categoryIdFilter; }
    public UploadedFile getImageFile() { return imageFile; }
    public void setImageFile(UploadedFile imageFile) { this.imageFile = imageFile; }
    public UploadedFile getGalleryFile() { return galleryFile; }
    public void setGalleryFile(UploadedFile galleryFile) { this.galleryFile = galleryFile; }
    public CategoryDTO getFilterCategory() { return filterCategory; }
    public void setFilterCategory(CategoryDTO filterCategory) { this.filterCategory = filterCategory; }
}