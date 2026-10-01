package com.agrishop.service;

import com.agrishop.dto.WishlistDTO;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface WishlistServiceLocal {
    List<WishlistDTO> getWishlistByUserId(Long userId);
    boolean toggleWishlist(Long userId, Long productId) throws Exception;
    boolean isInWishlist(Long userId, Long productId);
    void removeFromWishlist(Long userId, Long productId) throws Exception;
    int getWishlistCount(Long userId);
}
