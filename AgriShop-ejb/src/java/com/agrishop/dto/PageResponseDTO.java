package com.agrishop.dto;

import java.io.Serializable;
import java.util.List;

public class PageResponseDTO<T> implements Serializable {
    private List<T> data;
    private long totalRecords;

    public PageResponseDTO() {}

    public PageResponseDTO(List<T> data, long totalRecords) {
        this.data = data;
        this.totalRecords = totalRecords;
    }

    public List<T> getData() { return data; }
    public void setData(List<T> data) { this.data = data; }
    public long getTotalRecords() { return totalRecords; }
    public void setTotalRecords(long totalRecords) { this.totalRecords = totalRecords; }
}
