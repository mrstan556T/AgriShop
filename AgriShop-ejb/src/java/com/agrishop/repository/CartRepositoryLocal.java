package com.agrishop.repository;

import com.agrishop.entity.Cart;
import com.agrishop.entity.CartItem;
import jakarta.ejb.Local;

@Local
public interface CartRepositoryLocal {
    Cart findByUserId(Long userId);
    void createCart(Cart cart);
    void updateCart(Cart cart);
    void saveCartItem(CartItem item);
    void updateCartItem(CartItem item);
    void deleteCartItem(CartItem item);
    CartItem findCartItemById(Long id);
    CartItem findCartItemByCartAndProduct(Long cartId, Long productId);
    void deleteCartItemsByCartId(Long cartId);
}
