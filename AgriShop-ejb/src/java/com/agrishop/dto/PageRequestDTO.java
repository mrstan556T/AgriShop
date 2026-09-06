package com.agrishop.dto;

import java.io.Serializable;

public class PageRequestDTO implements Serializable {
    private int pageIndex;
    private int pageSize;
    private String searchKeyword;
    private String sortField;
    private String sortOrder;

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
}
