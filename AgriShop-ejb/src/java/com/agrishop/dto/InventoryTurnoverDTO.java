package com.agrishop.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class InventoryTurnoverDTO implements Serializable {
    private Long productId;
    private String productCode;
    private String productName;
    private String categoryName;
    private String supplierName;
    private String unit;
    private BigDecimal currentStock = BigDecimal.ZERO;
    private BigDecimal costPrice = BigDecimal.ZERO;
    private BigDecimal totalSoldInPeriod = BigDecimal.ZERO;
    private BigDecimal averageStock = BigDecimal.ZERO;
    private double turnoverRatio;
    private BigDecimal tiedUpCapital = BigDecimal.ZERO;
    private boolean slowMoving;
    private String turnoverStatus; // "SLOW_MOVING", "HEALTHY", "FAST_MOVING"

    public InventoryTurnoverDTO() {
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getCurrentStock() { return currentStock != null ? currentStock : BigDecimal.ZERO; }
    public void setCurrentStock(BigDecimal currentStock) { this.currentStock = currentStock; }

    public BigDecimal getCostPrice() { return costPrice != null ? costPrice : BigDecimal.ZERO; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }

    public BigDecimal getTotalSoldInPeriod() { return totalSoldInPeriod != null ? totalSoldInPeriod : BigDecimal.ZERO; }
    public void setTotalSoldInPeriod(BigDecimal totalSoldInPeriod) { this.totalSoldInPeriod = totalSoldInPeriod; }

    public BigDecimal getAverageStock() { return averageStock != null ? averageStock : BigDecimal.ZERO; }
    public void setAverageStock(BigDecimal averageStock) { this.averageStock = averageStock; }

    public double getTurnoverRatio() { return turnoverRatio; }
    public void setTurnoverRatio(double turnoverRatio) { this.turnoverRatio = turnoverRatio; }

    public BigDecimal getTiedUpCapital() { return tiedUpCapital != null ? tiedUpCapital : BigDecimal.ZERO; }
    public void setTiedUpCapital(BigDecimal tiedUpCapital) { this.tiedUpCapital = tiedUpCapital; }

    public boolean isSlowMoving() { return slowMoving; }
    public void setSlowMoving(boolean slowMoving) { this.slowMoving = slowMoving; }

    public String getTurnoverStatus() { return turnoverStatus; }
    public void setTurnoverStatus(String turnoverStatus) { this.turnoverStatus = turnoverStatus; }

    public BigDecimal getTotalSold() { return getTotalSoldInPeriod(); }
    public double getTurnoverRate() { return getTurnoverRatio(); }
}
