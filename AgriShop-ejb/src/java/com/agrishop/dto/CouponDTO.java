package com.agrishop.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class CouponDTO implements Serializable {
    private Long id;
    private String code;
    private String discountType; // 'PERCENTAGE', 'FIXED'
    private BigDecimal discountValue;
    private BigDecimal minOrderValue;
    private Date startDate;
    private Date endDate;
    private Integer usageLimit;
    private Integer usedCount;
    private String status; // 'ACTIVE', 'INACTIVE', 'EXPIRED'
    private Date createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public BigDecimal getDiscountValue() { return discountValue != null ? discountValue : BigDecimal.ZERO; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public BigDecimal getMinOrderValue() { return minOrderValue != null ? minOrderValue : BigDecimal.ZERO; }
    public void setMinOrderValue(BigDecimal minOrderValue) { this.minOrderValue = minOrderValue; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }
    public Integer getUsedCount() { return usedCount != null ? usedCount : 0; }
    public void setUsedCount(Integer usedCount) { this.usedCount = usedCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
