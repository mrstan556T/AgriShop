package com.agrishop.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class OrderDTO implements Serializable {
    private Long id;
    private String orderCode;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private String couponCode;
    private String paymentStatus;
    private String paymentMethod;
    private boolean cancellationRequested;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String phone;
    private String status;
    private Date createdAt;
    private List<OrderDetailDTO> orderDetails;
    private String customerName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
    public BigDecimal getSubtotal() { return subtotal != null ? subtotal : (totalAmount != null ? totalAmount : BigDecimal.ZERO); }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getDiscountAmount() { return discountAmount != null ? discountAmount : BigDecimal.ZERO; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getShippingFee() { return shippingFee != null ? shippingFee : BigDecimal.ZERO; }
    public void setShippingFee(BigDecimal shippingFee) { this.shippingFee = shippingFee; }
    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    public String getPaymentStatus() { return paymentStatus != null ? paymentStatus : "UNPAID"; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getPaymentMethod() { return paymentMethod != null ? paymentMethod : "COD"; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public boolean isCancellationRequested() { return cancellationRequested; }
    public void setCancellationRequested(boolean cancellationRequested) { this.cancellationRequested = cancellationRequested; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public List<OrderDetailDTO> getOrderDetails() { return orderDetails; }
    public void setOrderDetails(List<OrderDetailDTO> orderDetails) { this.orderDetails = orderDetails; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getFirstProductImageUrl() {
        if (orderDetails != null && !orderDetails.isEmpty()) {
            for (OrderDetailDTO d : orderDetails) {
                if (d.getImageUrl() != null && !d.getImageUrl().trim().isEmpty()) {
                    return d.getImageUrl();
                }
            }
        }
        return null;
    }

    public String getFirstProductName() {
        if (orderDetails != null && !orderDetails.isEmpty()) {
            return orderDetails.get(0).getProductName();
        }
        return "Nông sản";
    }

    public int getExtraItemsCount() {
        if (orderDetails != null && orderDetails.size() > 1) {
            return orderDetails.size() - 1;
        }
        return 0;
    }
}
