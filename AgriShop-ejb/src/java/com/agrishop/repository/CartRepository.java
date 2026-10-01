package com.agrishop.repository;

import com.agrishop.entity.Cart;
import com.agrishop.entity.CartItem;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;

@Stateless
public class CartRepository implements CartRepositoryLocal {

    @PersistenceContext(unitName = "AgriShopPU")
    private EntityManager em;

    @Override
    public Cart findByUserId(Long userId) {
        try {
            List<Cart> list = em.createQuery("SELECT DISTINCT c FROM Cart c LEFT JOIN FETCH c.items i LEFT JOIN FETCH i.product WHERE c.user.id = :userId", Cart.class)
                    .setParameter("userId", userId)
                    .getResultList();
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void createCart(Cart cart) {
        cart.setUpdatedAt(new Date());
        em.persist(cart);
        em.flush();
    }

    @Override
    public void updateCart(Cart cart) {
        cart.setUpdatedAt(new Date());
        em.merge(cart);
    }

    @Override
    public void saveCartItem(CartItem item) {
        em.persist(item);
        em.flush();
        if (item.getCart() != null) {
            item.getCart().setUpdatedAt(new Date());
            em.merge(item.getCart());
        }
    }

    @Override
    public void updateCartItem(CartItem item) {
        em.merge(item);
        if (item.getCart() != null) {
            item.getCart().setUpdatedAt(new Date());
            em.merge(item.getCart());
        }
    }

    @Override
    public void deleteCartItem(CartItem item) {
        if (!em.contains(item)) {
            item = em.merge(item);
        }
        
        Cart cart = item.getCart();
        if (cart != null) {
            cart.getItems().remove(item);
            cart.setUpdatedAt(new Date());
            em.merge(cart);
        }
        
        em.remove(item);
    }

    @Override
    public CartItem findCartItemById(Long id) {
        return em.find(CartItem.class, id);
    }

    @Override
    public CartItem findCartItemByCartAndProduct(Long cartId, Long productId) {
        try {
            return em.createQuery("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.product.id = :productId", CartItem.class)
                    .setParameter("cartId", cartId)
                    .setParameter("productId", productId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public void deleteCartItemsByCartId(Long cartId) {
        em.createQuery("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
          .setParameter("cartId", cartId)
          .executeUpdate();
    }
}
