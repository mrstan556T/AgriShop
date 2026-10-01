package com.agrishop.dto;

import java.io.Serializable;

public class UserDTO implements Serializable {
    private Long id;
    private String userCode;
    private String username;
    private String fullName;
    private String role;
    private String email;
    private String status;
    private String phone;
    private String address;
    private String avatarUrl;
    private Integer totalOrders;
    private java.math.BigDecimal totalSpent;
    private java.util.Date createdAt;
    private Boolean isEmailVerified = true;
    private Boolean totpEnabled = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserCode() { return userCode; }
    public void setUserCode(String userCode) { this.userCode = userCode; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public Integer getTotalOrders() { return totalOrders != null ? totalOrders : 0; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }
    public java.math.BigDecimal getTotalSpent() { return totalSpent != null ? totalSpent : java.math.BigDecimal.ZERO; }
    public void setTotalSpent(java.math.BigDecimal totalSpent) { this.totalSpent = totalSpent; }
    public java.util.Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.util.Date createdAt) { this.createdAt = createdAt; }
    public Boolean getIsEmailVerified() { return isEmailVerified != null ? isEmailVerified : true; }
    public void setIsEmailVerified(Boolean isEmailVerified) { this.isEmailVerified = isEmailVerified; }
    public Boolean getTotpEnabled() { return totpEnabled != null ? totpEnabled : false; }
    public void setTotpEnabled(Boolean totpEnabled) { this.totpEnabled = totpEnabled; }
}