package com.agrishop.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class InventoryTransactionDTO implements Serializable {
    private Long id;
    private Long productId;
    private String productCode;
    private String productName;
    private String unit;
    private Long userId;
    private String userName;
    private Long supplierId;
    private String supplierName;
    private BigDecimal quantityChanged = BigDecimal.ZERO;
    private BigDecimal unitCost;
    private String transactionType;
    private String reason;
    private Date createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public BigDecimal getQuantityChanged() { return quantityChanged != null ? quantityChanged : BigDecimal.ZERO; }
    public void setQuantityChanged(BigDecimal quantityChanged) { this.quantityChanged = quantityChanged; }

    public BigDecimal getUnitCost() { return unitCost != null ? unitCost : BigDecimal.ZERO; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
