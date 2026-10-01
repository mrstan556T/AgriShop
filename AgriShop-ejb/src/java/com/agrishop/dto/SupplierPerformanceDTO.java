package com.agrishop.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class SupplierPerformanceDTO implements Serializable {
    private Long supplierId;
    private String supplierCode;
    private String supplierName;
    private String supplierPhone;
    private long totalProductsSupplied;
    private BigDecimal totalImportValue = BigDecimal.ZERO;
    private BigDecimal totalSoldQuantity = BigDecimal.ZERO;
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private long bestSellerCount;
    private double bestSellerRatio; // Percentage of supplier's products that are best sellers (0 - 100)
    private int rank;
    private String topSellingProductName = "Tất cả sản phẩm";
    private Double rating = 4.8;

    public SupplierPerformanceDTO() {
    }

    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }

    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getSupplierPhone() { return supplierPhone; }
    public void setSupplierPhone(String supplierPhone) { this.supplierPhone = supplierPhone; }

    public long getTotalProductsSupplied() { return totalProductsSupplied; }
    public void setTotalProductsSupplied(long totalProductsSupplied) { this.totalProductsSupplied = totalProductsSupplied; }

    public BigDecimal getTotalImportValue() { return totalImportValue != null ? totalImportValue : BigDecimal.ZERO; }
    public void setTotalImportValue(BigDecimal totalImportValue) { this.totalImportValue = totalImportValue; }

    public BigDecimal getTotalSoldQuantity() { return totalSoldQuantity != null ? totalSoldQuantity : BigDecimal.ZERO; }
    public void setTotalSoldQuantity(BigDecimal totalSoldQuantity) { this.totalSoldQuantity = totalSoldQuantity; }

    public BigDecimal getTotalRevenue() { return totalRevenue != null ? totalRevenue : BigDecimal.ZERO; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public long getBestSellerCount() { return bestSellerCount; }
    public void setBestSellerCount(long bestSellerCount) { this.bestSellerCount = bestSellerCount; }

    public double getBestSellerRatio() { return bestSellerRatio; }
    public void setBestSellerRatio(double bestSellerRatio) { this.bestSellerRatio = bestSellerRatio; }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public String getTopSellingProductName() { return topSellingProductName != null ? topSellingProductName : "Nông sản tiêu chuẩn"; }
    public void setTopSellingProductName(String topSellingProductName) { this.topSellingProductName = topSellingProductName; }

    public Double getRating() { return rating != null ? rating : 4.8; }
    public void setRating(Double rating) { this.rating = rating; }
}
