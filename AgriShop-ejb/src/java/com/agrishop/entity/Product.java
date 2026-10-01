package com.agrishop.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Products")
@Cacheable(true)
public class Product implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_code", unique = true, nullable = false, length = 20)
    private String productCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 50)
    private String unit;

    @Column(name = "unit_type", nullable = false, length = 20)
    private String unitType = "COUNT"; // 'WEIGHT', 'COUNT'

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "cost_price")
    private BigDecimal costPrice;

    @Column(name = "stock_quantity", nullable = false)
    private BigDecimal stockQuantity = BigDecimal.ZERO;

    @Column(name = "reserved_quantity")
    private BigDecimal reservedQuantity = BigDecimal.ZERO;

    @Column(name = "min_stock")
    private BigDecimal minStock = BigDecimal.valueOf(10);

    @Column(name = "total_imported")
    private BigDecimal totalImported = BigDecimal.ZERO;

    @Column(name = "total_sold")
    private BigDecimal totalSold = BigDecimal.ZERO;

    private String description;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC, id ASC")
    private List<ProductImage> images = new ArrayList<>();

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", insertable = false, updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

    // Getters và Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
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
    public BigDecimal getTotalImported() { return totalImported != null ? totalImported : BigDecimal.ZERO; }
    public void setTotalImported(BigDecimal totalImported) { this.totalImported = totalImported; }
    public BigDecimal getTotalSold() { return totalSold != null ? totalSold : BigDecimal.ZERO; }
    public void setTotalSold(BigDecimal totalSold) { this.totalSold = totalSold; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public List<ProductImage> getImages() { return images; }
    public void setImages(List<ProductImage> images) { this.images = images; }
    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public BigDecimal getAvailableQuantity() {
        BigDecimal avail = getStockQuantity().subtract(getReservedQuantity());
        return avail.compareTo(BigDecimal.ZERO) > 0 ? avail : BigDecimal.ZERO;
    }
}