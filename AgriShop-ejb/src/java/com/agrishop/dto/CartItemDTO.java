package com.agrishop.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class CartItemDTO implements Serializable {
    private Long id;
    private Long cartId;
    private Long productId;
    private String productCode;
    private String productName;
    private String productImageUrl;
    private BigDecimal productPrice;
    private BigDecimal quantity = BigDecimal.ONE;
    private BigDecimal stockQuantity = BigDecimal.ZERO;
    private BigDecimal availableQuantity = BigDecimal.ZERO;
    private String productUnit;
    private String unitType = "COUNT";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCartId() { return cartId; }
    public void setCartId(Long cartId) { this.cartId = cartId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductImageUrl() { return productImageUrl; }
    public void setProductImageUrl(String productImageUrl) { this.productImageUrl = productImageUrl; }
    public BigDecimal getProductPrice() { return productPrice; }
    public void setProductPrice(BigDecimal productPrice) { this.productPrice = productPrice; }
    public BigDecimal getQuantity() { return quantity != null ? quantity : BigDecimal.ZERO; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    
    public BigDecimal getStockQuantity() { 
        return getAvailableQuantity(); 
    }
    public void setStockQuantity(BigDecimal stockQuantity) { 
        this.stockQuantity = stockQuantity; 
        this.availableQuantity = stockQuantity;
    }
    public BigDecimal getAvailableQuantity() { 
        if (availableQuantity != null && availableQuantity.compareTo(BigDecimal.ZERO) > 0) return availableQuantity;
        return stockQuantity != null ? stockQuantity : BigDecimal.ZERO; 
    }
    public void setAvailableQuantity(BigDecimal availableQuantity) { 
        this.availableQuantity = availableQuantity; 
        this.stockQuantity = availableQuantity;
    }

    public String getProductUnit() { return productUnit; }
    public void setProductUnit(String productUnit) { this.productUnit = productUnit; }
    public String getUnitType() { return unitType != null ? unitType : "COUNT"; }
    public void setUnitType(String unitType) { this.unitType = unitType; }

    public BigDecimal getUnitPrice() { return productPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.productPrice = unitPrice; }
    public BigDecimal getSubtotal() { return getItemTotal(); }
    public void setSubtotal(BigDecimal subtotal) { /* computed */ }

    // Transient property for item total
    public BigDecimal getItemTotal() {
        if (productPrice != null && quantity != null) {
            return productPrice.multiply(quantity);
        }
        return BigDecimal.ZERO;
    }
}
