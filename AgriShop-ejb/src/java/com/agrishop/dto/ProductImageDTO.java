package com.agrishop.dto;

import java.io.Serializable;

public class ProductImageDTO implements Serializable {
    private Long id;
    private Long productId;
    private String imageUrl;
    private Integer displayOrder = 0;
    private Boolean isPrimary = false;

    public ProductImageDTO() {}

    public ProductImageDTO(Long id, Long productId, String imageUrl, Integer displayOrder, Boolean isPrimary) {
        this.id = id;
        this.productId = productId;
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
        this.isPrimary = isPrimary != null ? isPrimary : false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Integer getDisplayOrder() { return displayOrder != null ? displayOrder : 0; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public Boolean getIsPrimary() { return isPrimary != null ? isPrimary : false; }
    public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }
}
