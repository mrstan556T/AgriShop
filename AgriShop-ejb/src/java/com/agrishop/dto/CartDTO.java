package com.agrishop.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartDTO implements Serializable {
    private Long id;
    private Long userId;
    private List<CartItemDTO> items = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public List<CartItemDTO> getItems() { return items; }
    public void setItems(List<CartItemDTO> items) { this.items = items; }

    public BigDecimal getTotalAmount() {
        BigDecimal total = BigDecimal.ZERO;
        if (items != null) {
            for (CartItemDTO item : items) {
                total = total.add(item.getItemTotal());
            }
        }
        return total;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        // Computed dynamically from items, setter supported for DTO serialization
    }
    
    public int getTotalItems() {
        if (items != null) {
            return items.size();
        }
        return 0;
    }

    public void setTotalItems(int totalItems) {
        // Computed dynamically from items
    }
}
