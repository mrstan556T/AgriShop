package com.agrishop.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class OrderDetailDTO implements Serializable {
    private Long id;
    private Long productId;
    private String productCode;
    private String productName;
    private String unit;
    private String unitType = "COUNT";
    private BigDecimal unitPrice;
    private BigDecimal costPrice;
    private BigDecimal quantity = BigDecimal.ZERO;
    private String imageUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getUnit() { return unit != null ? unit : "kg"; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getUnitType() { return unitType != null ? unitType : "COUNT"; }
    public void setUnitType(String unitType) { this.unitType = unitType; }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getCostPrice() { return costPrice != null ? costPrice : BigDecimal.ZERO; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    
    public BigDecimal getQuantity() { return quantity != null ? quantity : BigDecimal.ZERO; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public BigDecimal getItemTotal() {
        if (unitPrice != null && quantity != null) {
            return unitPrice.multiply(quantity);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getItemProfit() {
        if (unitPrice != null && costPrice != null && quantity != null) {
            return unitPrice.subtract(costPrice).multiply(quantity);
        }
        return BigDecimal.ZERO;
    }
}
