package com.agrishop.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductDTO implements Serializable {
    private Long id;
    private String productCode;
    private Long categoryId;
    private String categoryName;
    private Long supplierId;
    private String supplierName;
    private String name;
    private String unit;
    private String unitType = "COUNT"; // 'WEIGHT', 'COUNT'
    private BigDecimal price;
    private BigDecimal costPrice;
    private BigDecimal stockQuantity = BigDecimal.ZERO;
    private BigDecimal reservedQuantity = BigDecimal.ZERO;
    private BigDecimal minStock = BigDecimal.valueOf(10);
    private BigDecimal totalImported = BigDecimal.ZERO;
    private BigDecimal totalSold = BigDecimal.ZERO;
    private BigDecimal revenue;
    private BigDecimal profit;
    private String description;
    private String imageUrl;
    private Boolean isDeleted = false;
    private BigDecimal averageDailySale30d = BigDecimal.ZERO;
    private BigDecimal suggestedRestockQuantity = BigDecimal.ZERO;
    private List<ProductImageDTO> images = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getUnitType() { return unitType != null ? unitType : "COUNT"; }
    public void setUnitType(String unitType) { this.unitType = unitType; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getCostPrice() { return costPrice != null ? costPrice : BigDecimal.ZERO; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    public BigDecimal getStockQuantity() { return stockQuantity != null ? stockQuantity : BigDecimal.ZERO; }
    public void setStockQuantity(BigDecimal stockQuantity) { this.stockQuantity = stockQuantity; }
    public BigDecimal getReservedQuantity() { return reservedQuantity != null ? reservedQuantity : BigDecimal.ZERO; }
    public void setReservedQuantity(BigDecimal reservedQuantity) { this.reservedQuantity = reservedQuantity; }
    public BigDecimal getMinStock() { return minStock != null ? minStock : BigDecimal.valueOf(10); }
    public void setMinStock(BigDecimal minStock) { this.minStock = minStock; }
    
    public BigDecimal getAvailableQuantity() {
        BigDecimal avail = getStockQuantity().subtract(getReservedQuantity());
        return avail.compareTo(BigDecimal.ZERO) > 0 ? avail : BigDecimal.ZERO;
    }

    public String getStockStatus() {
        BigDecimal avail = getAvailableQuantity();
        if (avail.compareTo(BigDecimal.ZERO) <= 0) return "OUT_OF_STOCK";
        if (avail.compareTo(getMinStock()) <= 0) return "LOW_STOCK";
        return "IN_STOCK";
    }

    public BigDecimal getTotalImported() { return totalImported != null ? totalImported : BigDecimal.ZERO; }
    public void setTotalImported(BigDecimal totalImported) { this.totalImported = totalImported; }
    public BigDecimal getTotalSold() { return totalSold != null ? totalSold : BigDecimal.ZERO; }
    public void setTotalSold(BigDecimal totalSold) { this.totalSold = totalSold; }
    public BigDecimal getRevenue() { return revenue != null ? revenue : BigDecimal.ZERO; }
    public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
    public BigDecimal getProfit() { return profit != null ? profit : BigDecimal.ZERO; }
    public void setProfit(BigDecimal profit) { this.profit = profit; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Boolean getIsDeleted() { return isDeleted != null && isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
    public Boolean getIsActive() { return isDeleted == null || !isDeleted; }
    public BigDecimal getAverageDailySale30d() { return averageDailySale30d != null ? averageDailySale30d : BigDecimal.ZERO; }
    public void setAverageDailySale30d(BigDecimal averageDailySale30d) { this.averageDailySale30d = averageDailySale30d; }
    public BigDecimal getSuggestedRestockQuantity() { return suggestedRestockQuantity != null ? suggestedRestockQuantity : BigDecimal.ZERO; }
    public void setSuggestedRestockQuantity(BigDecimal suggestedRestockQuantity) { this.suggestedRestockQuantity = suggestedRestockQuantity; }
    public List<ProductImageDTO> getImages() { return images; }
    public void setImages(List<ProductImageDTO> images) { this.images = images; }
}