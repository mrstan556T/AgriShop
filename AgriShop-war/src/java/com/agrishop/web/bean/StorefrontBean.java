package com.agrishop.web.bean;

import com.agrishop.dto.CategoryDTO;
import com.agrishop.dto.PageRequestDTO;
import com.agrishop.dto.PageResponseDTO;
import com.agrishop.dto.ProductDTO;
import com.agrishop.service.CategoryServiceLocal;
import com.agrishop.service.ProductServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;

@Named("storefrontBean")
@ViewScoped
public class StorefrontBean implements Serializable {

    @EJB
    private ProductServiceLocal productService;
    
    @EJB
    private CategoryServiceLocal categoryService;

    @Inject
    private CartBean cartBean;

    @Inject
    private WishlistBean wishlistBean;

    private List<CategoryDTO> categories;
    private List<ProductDTO> trendingProducts;
    
    // For Store Page
    private LazyDataModel<ProductDTO> lazyModel;
    private Long selectedCategoryId;
    private String searchQuery;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean inStockOnly = false;
    private String sortOption = "newest";
    
    // For Contact Page
    private String contactName;
    private String contactEmail;
    private String contactSubject;
    private String contactMessage;

    @PostConstruct
    public void init() {
        // Load categories for filters/display
        categories = categoryService.getAllActiveCategories();
        
        // Load some trending products for homepage (e.g. 8 newest)
        PageRequestDTO req = new PageRequestDTO();
        req.setPageIndex(0);
        req.setPageSize(8);
        req.setSortField("createdAt");
        req.setSortOrder("DESC");
        PageResponseDTO<ProductDTO> resp = productService.getProductsWithPagination(req, null);
        if (resp != null) {
            trendingProducts = resp.getData();
        }

        // Setup lazy loading for store.xhtml
        lazyModel = new LazyDataModel<ProductDTO>() {
            @Override
            public int count(Map<String, FilterMeta> filterBy) {
                return 0; // Set via response
            }

            @Override
            public List<ProductDTO> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
                PageRequestDTO request = new PageRequestDTO();
                int size = pageSize > 0 ? pageSize : 12;
                request.setPageIndex(first / size);
                request.setPageSize(size);
                request.setSearchKeyword(searchQuery);
                request.setMinPrice(minPrice);
                request.setMaxPrice(maxPrice);
                request.setInStockOnly(inStockOnly);
                request.setSortOption(sortOption);

                if (sortBy != null && !sortBy.isEmpty()) {
                    SortMeta sm = sortBy.values().iterator().next();
                    request.setSortField(sm.getField());
                    request.setSortOrder(sm.getOrder().isAscending() ? "ASC" : "DESC");
                }

                PageResponseDTO<ProductDTO> response = productService.getProductsWithPagination(request, selectedCategoryId);
                this.setRowCount((int) response.getTotalRecords());
                return response.getData();
            }
        };
    }

    public void applyFilter() {
        // Triggers re-render and reload of lazyModel
    }

    public void resetFilter() {
        this.searchQuery = null;
        this.selectedCategoryId = null;
        this.minPrice = null;
        this.maxPrice = null;
        this.inStockOnly = false;
        this.sortOption = "newest";
    }

    public void search() {
        // Triggered by search button
    }
    
    public void selectCategory(Long categoryId) {
        this.selectedCategoryId = categoryId;
    }

    public void addToCart(ProductDTO product) {
        if (product != null) {
            BigDecimal qty = "WEIGHT".equalsIgnoreCase(product.getUnitType()) ? new BigDecimal("0.5") : BigDecimal.ONE;
            cartBean.addToCart(product.getId(), qty);
        }
    }

    public void toggleWishlist(Long productId) {
        if (productId != null) {
            wishlistBean.toggleWishlist(productId);
        }
    }

    public boolean isWishlisted(Long productId) {
        return wishlistBean.isWishlisted(productId);
    }

    // Getters & Setters
    public List<CategoryDTO> getCategories() { return categories; }
    public List<ProductDTO> getTrendingProducts() { return trendingProducts; }
    public LazyDataModel<ProductDTO> getLazyModel() { return lazyModel; }
    public Long getSelectedCategoryId() { return selectedCategoryId; }
    public void setSelectedCategoryId(Long selectedCategoryId) { this.selectedCategoryId = selectedCategoryId; }
    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }
    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
    public Boolean getInStockOnly() { return inStockOnly; }
    public void setInStockOnly(Boolean inStockOnly) { this.inStockOnly = inStockOnly; }
    public String getSortOption() { return sortOption; }
    public void setSortOption(String sortOption) { this.sortOption = sortOption; }

    public void submitContact() {
        jakarta.faces.context.FacesContext.getCurrentInstance().addMessage(null, 
            new jakarta.faces.application.FacesMessage(jakarta.faces.application.FacesMessage.SEVERITY_INFO, 
                "Gửi tin nhắn thành công", 
                "Cảm ơn " + (contactName != null && !contactName.trim().isEmpty() ? contactName : "quý khách") + "! Đội ngũ AgriShop sẽ liên hệ hỗ trợ trong thời gian sớm nhất."));
        contactName = null;
        contactEmail = null;
        contactSubject = null;
        contactMessage = null;
    }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getContactSubject() { return contactSubject; }
    public void setContactSubject(String contactSubject) { this.contactSubject = contactSubject; }
    public String getContactMessage() { return contactMessage; }
    public void setContactMessage(String contactMessage) { this.contactMessage = contactMessage; }

    public String getSelectedCategoryName() {
        if (selectedCategoryId != null && categories != null) {
            for (CategoryDTO c : categories) {
                if (c.getId().equals(selectedCategoryId)) {
                    return c.getName();
                }
            }
        }
        return null;
    }
}
