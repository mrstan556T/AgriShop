package com.agrishop.service;

import com.agrishop.dto.WishlistDTO;
import com.agrishop.entity.Product;
import com.agrishop.entity.User;
import com.agrishop.entity.Wishlist;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class WishlistService implements WishlistServiceLocal {

    @PersistenceContext(unitName = "AgriShopPU")
    private EntityManager em;

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<WishlistDTO> getWishlistByUserId(Long userId) {
        List<Wishlist> list = em.createQuery("SELECT DISTINCT w FROM Wishlist w JOIN FETCH w.product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.images WHERE w.user.id = :uid AND (p.isDeleted = false OR p.isDeleted IS NULL) ORDER BY w.createdAt DESC", Wishlist.class)
                                .setParameter("uid", userId)
                                .getResultList();
        List<WishlistDTO> dtos = new ArrayList<>();
        if (list != null) {
            for (Wishlist w : list) {
                Product p = w.getProduct();
                WishlistDTO dto = new WishlistDTO();
                dto.setId(w.getId());
                dto.setUserId(userId);
                dto.setProductId(p.getId());
                dto.setProductCode(p.getProductCode());
                dto.setProductName(p.getName());
                dto.setCategoryName(p.getCategory() != null ? p.getCategory().getName() : "");
                dto.setProductPrice(p.getPrice());
                dto.setProductImageUrl(p.getImageUrl());
                dto.setProductUnit(p.getUnit());
                dto.setUnitType(p.getUnitType());
                dto.setStockQuantity(p.getStockQuantity());
                dto.setReservedQuantity(p.getReservedQuantity());
                dto.setCreatedAt(w.getCreatedAt());
                dtos.add(dto);
            }
        }
        return dtos;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public boolean toggleWishlist(Long userId, Long productId) throws Exception {
        List<Wishlist> existing = em.createQuery("SELECT w FROM Wishlist w WHERE w.user.id = :uid AND w.product.id = :pid", Wishlist.class)
                                    .setParameter("uid", userId)
                                    .setParameter("pid", productId)
                                    .getResultList();
        if (!existing.isEmpty()) {
            em.remove(existing.get(0));
            return false; // Removed from wishlist
        } else {
            User user = em.find(User.class, userId);
            Product product = em.find(Product.class, productId);
            if (user == null) throw new Exception("Tài khoản không hợp lệ.");
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                throw new com.agrishop.exception.BusinessException("Bạn đang xem bằng tài khoản Quản trị viên — không thể thao tác danh sách yêu thích trên Storefront.");
            }
            if (product == null) throw new Exception("Sản phẩm không tồn tại.");

            Wishlist w = new Wishlist(user, product);
            em.persist(w);
            return true; // Added to wishlist
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public boolean isInWishlist(Long userId, Long productId) {
        if (userId == null || productId == null) return false;
        Long count = em.createQuery("SELECT COUNT(w) FROM Wishlist w WHERE w.user.id = :uid AND w.product.id = :pid", Long.class)
                       .setParameter("uid", userId)
                       .setParameter("pid", productId)
                       .getSingleResult();
        return count != null && count > 0;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void removeFromWishlist(Long userId, Long productId) throws Exception {
        List<Wishlist> existing = em.createQuery("SELECT w FROM Wishlist w WHERE w.user.id = :uid AND w.product.id = :pid", Wishlist.class)
                                    .setParameter("uid", userId)
                                    .setParameter("pid", productId)
                                    .getResultList();
        if (!existing.isEmpty()) {
            em.remove(existing.get(0));
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public int getWishlistCount(Long userId) {
        if (userId == null) return 0;
        Long count = em.createQuery("SELECT COUNT(w) FROM Wishlist w WHERE w.user.id = :uid", Long.class)
                       .setParameter("uid", userId)
                       .getSingleResult();
        return count != null ? count.intValue() : 0;
    }
}
