package com.agrishop.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class WishlistDTO implements Serializable {
    private Long id;
    private Long userId;
    private Long productId;
    private String productCode;
    private String productName;
    private BigDecimal productPrice;
    private String productImageUrl;
    private String productUnit;
    private String unitType;
    private BigDecimal stockQuantity;
    private BigDecimal reservedQuantity;
    private Date createdAt;
    private String categoryName;

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public BigDecimal getProductPrice() { return productPrice; }
    public void setProductPrice(BigDecimal productPrice) { this.productPrice = productPrice; }

    public String getProductImageUrl() { return productImageUrl; }
    public void setProductImageUrl(String productImageUrl) { this.productImageUrl = productImageUrl; }

    public String getProductUnit() { return productUnit; }
    public void setProductUnit(String productUnit) { this.productUnit = productUnit; }

    public String getUnitType() { return unitType != null ? unitType : "COUNT"; }
    public void setUnitType(String unitType) { this.unitType = unitType; }

    public BigDecimal getStockQuantity() { return stockQuantity != null ? stockQuantity : BigDecimal.ZERO; }
    public void setStockQuantity(BigDecimal stockQuantity) { this.stockQuantity = stockQuantity; }

    public BigDecimal getReservedQuantity() { return reservedQuantity != null ? reservedQuantity : BigDecimal.ZERO; }
    public void setReservedQuantity(BigDecimal reservedQuantity) { this.reservedQuantity = reservedQuantity; }

    public BigDecimal getAvailableQuantity() {
        BigDecimal avail = getStockQuantity().subtract(getReservedQuantity());
        return avail.compareTo(BigDecimal.ZERO) > 0 ? avail : BigDecimal.ZERO;
    }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
