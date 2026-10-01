package com.agrishop.service;

import com.agrishop.dto.CartDTO;
import com.agrishop.dto.CartItemDTO;
import jakarta.ejb.Local;
import java.math.BigDecimal;
import java.util.List;

@Local
public interface CartServiceLocal {
    CartDTO getCartByUserId(Long userId);
    CartDTO addToCart(Long userId, Long productId, BigDecimal quantity) throws Exception;
    CartDTO updateItemQuantity(Long userId, Long cartItemId, BigDecimal quantity) throws Exception;
    CartDTO removeItem(Long userId, Long cartItemId) throws Exception;
    CartDTO clearCart(Long userId);
    CartDTO mergeGuestCart(Long userId, List<CartItemDTO> guestItems) throws Exception;
}
