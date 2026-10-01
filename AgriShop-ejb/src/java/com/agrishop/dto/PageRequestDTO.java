package com.agrishop.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class PageRequestDTO implements Serializable {
    private int pageIndex;
    private int pageSize;
    private String searchKeyword;
    private String sortField;
    private String sortOrder;

    // Advanced filters for Storefront
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean inStockOnly;
    private String sortOption = "NEWEST"; // 'NEWEST', 'PRICE_ASC', 'PRICE_DESC', 'BEST_SELLER', 'NAME_ASC'

    public PageRequestDTO() {
        this.pageIndex = 0;
        this.pageSize = 10;
        this.sortOrder = "ASC";
    }

    public int getPageIndex() { return pageIndex; }
    public void setPageIndex(int pageIndex) { this.pageIndex = pageIndex; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public String getSearchKeyword() { return searchKeyword; }
    public void setSearchKeyword(String searchKeyword) { this.searchKeyword = searchKeyword; }
    public String getSortField() { return sortField; }
    public void setSortField(String sortField) { this.sortField = sortField; }
    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }
    
    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
    public Boolean getInStockOnly() { return inStockOnly; }
    public void setInStockOnly(Boolean inStockOnly) { this.inStockOnly = inStockOnly; }
    public String getSortOption() { return sortOption != null ? sortOption : "NEWEST"; }
    public void setSortOption(String sortOption) { this.sortOption = sortOption; }
}
