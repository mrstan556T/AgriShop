package com.agrishop.service;

import com.agrishop.dto.CartDTO;
import com.agrishop.dto.CartItemDTO;
import com.agrishop.entity.Cart;
import com.agrishop.entity.CartItem;
import com.agrishop.entity.Product;
import com.agrishop.entity.User;
import com.agrishop.repository.CartRepositoryLocal;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class CartService implements CartServiceLocal {

    @EJB
    private CartRepositoryLocal cartRepository;

    @PersistenceContext(unitName = "AgriShopPU")
    private EntityManager em;

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public CartDTO getCartByUserId(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            return new CartDTO(); // Empty cart representation
        }
        return mapToDTO(cart);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public CartDTO addToCart(Long userId, Long productId, BigDecimal quantity) throws Exception {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Số lượng phải lớn hơn 0");
        }

        Product product = em.find(Product.class, productId);
        if (product == null) {
            throw new Exception("Sản phẩm không tồn tại");
        }
        if (product.getIsDeleted() != null && product.getIsDeleted()) {
            throw new Exception("Sản phẩm này hiện không được bán");
        }
        
        if (userId != null) {
            User user = em.find(User.class, userId);
            if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
                throw new com.agrishop.exception.BusinessException("Bạn đang xem bằng tài khoản Quản trị viên — không thể thực hiện giao dịch mua sắm trên Storefront.");
            }
        }
        
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            cart = new Cart();
            User user = em.find(User.class, userId);
            cart.setUser(user);
            cart.setCreatedAt(new Date());
            cartRepository.createCart(cart);
        }

        BigDecimal available = product.getAvailableQuantity();
        CartItem item = cartRepository.findCartItemByCartAndProduct(cart.getId(), productId);
        if (item != null) {
            BigDecimal newQuantity = item.getQuantity().add(quantity);
            if (newQuantity.compareTo(available) > 0) {
                throw new Exception("Vượt quá số lượng tồn kho khả dụng (" + available + " " + product.getUnit() + ")");
            }
            item.setQuantity(newQuantity);
            cartRepository.updateCartItem(item);
        } else {
            if (quantity.compareTo(available) > 0) {
                throw new Exception("Vượt quá số lượng tồn kho khả dụng (" + available + " " + product.getUnit() + ")");
            }
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(quantity);
            cartRepository.saveCartItem(item);
            cart.getItems().add(item);
        }

        return mapToDTO(cart);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public CartDTO updateItemQuantity(Long userId, Long cartItemId, BigDecimal quantity) throws Exception {
        if (cartItemId == null) {
            throw new Exception("Mã sản phẩm trong giỏ hàng không hợp lệ (cartItemId is null)");
        }
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) throw new Exception("Không tìm thấy giỏ hàng");

        CartItem item = cartRepository.findCartItemById(cartItemId);
        if (item == null || !item.getCart().getId().equals(cart.getId())) {
            throw new Exception("Sản phẩm không có trong giỏ hàng");
        }

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            cartRepository.deleteCartItem(item);
            cart.getItems().remove(item);
        } else {
            Product product = item.getProduct();
            BigDecimal available = product.getAvailableQuantity();
            if (quantity.compareTo(available) > 0) {
                throw new Exception("Vượt quá số lượng tồn kho khả dụng (" + available + " " + product.getUnit() + ")");
            }
            item.setQuantity(quantity);
            cartRepository.updateCartItem(item);
        }

        return mapToDTO(cart);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public CartDTO removeItem(Long userId, Long cartItemId) throws Exception {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) throw new Exception("Không tìm thấy giỏ hàng");

        CartItem item = cartRepository.findCartItemById(cartItemId);
        if (item == null || !item.getCart().getId().equals(cart.getId())) {
            throw new Exception("Sản phẩm không có trong giỏ hàng");
        }

        cartRepository.deleteCartItem(item);
        cart.getItems().remove(item);
        return mapToDTO(cart);
    }
    
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public CartDTO clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart != null) {
            cartRepository.deleteCartItemsByCartId(cart.getId());
            cart.getItems().clear();
            return mapToDTO(cart);
        }
        return new CartDTO();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public CartDTO mergeGuestCart(Long userId, List<CartItemDTO> guestItems) throws Exception {
        if (userId == null || guestItems == null || guestItems.isEmpty()) {
            return getCartByUserId(userId);
        }

        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            cart = new Cart();
            User user = em.find(User.class, userId);
            cart.setUser(user);
            cart.setCreatedAt(new Date());
            cartRepository.createCart(cart);
        }

        for (CartItemDTO guestItem : guestItems) {
            if (guestItem.getProductId() == null || guestItem.getQuantity() == null || guestItem.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            Product product = em.find(Product.class, guestItem.getProductId());
            if (product == null || (product.getIsDeleted() != null && product.getIsDeleted())) {
                continue; // Skip discontinued products
            }

            BigDecimal available = product.getAvailableQuantity();
            if (available.compareTo(BigDecimal.ZERO) <= 0) {
                continue; // Skip out of stock items
            }

            CartItem existing = cartRepository.findCartItemByCartAndProduct(cart.getId(), product.getId());
            if (existing != null) {
                BigDecimal mergedQty = existing.getQuantity().add(guestItem.getQuantity());
                if (mergedQty.compareTo(available) > 0) {
                    mergedQty = available; // Cap at available stock
                }
                existing.setQuantity(mergedQty);
                cartRepository.updateCartItem(existing);
            } else {
                BigDecimal addQty = guestItem.getQuantity();
                if (addQty.compareTo(available) > 0) {
                    addQty = available; // Cap at available stock
                }
                CartItem newItem = new CartItem();
                newItem.setCart(cart);
                newItem.setProduct(product);
                newItem.setQuantity(addQty);
                cartRepository.saveCartItem(newItem);
                cart.getItems().add(newItem);
            }
        }

        return mapToDTO(cart);
    }

    private CartDTO mapToDTO(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUser().getId());
        if (cart.getItems() != null) {
            dto.setItems(cart.getItems().stream().map(item -> {
                CartItemDTO itemDTO = new CartItemDTO();
                itemDTO.setId(item.getId());
                itemDTO.setCartId(cart.getId());
                itemDTO.setProductId(item.getProduct().getId());
                itemDTO.setProductCode(item.getProduct().getProductCode());
                itemDTO.setProductName(item.getProduct().getName());
                itemDTO.setProductImageUrl(item.getProduct().getImageUrl());
                itemDTO.setProductPrice(item.getProduct().getPrice());
                itemDTO.setQuantity(item.getQuantity());
                BigDecimal availQty = item.getProduct() != null ? item.getProduct().getAvailableQuantity() : BigDecimal.ZERO;
                itemDTO.setAvailableQuantity(availQty);
                itemDTO.setStockQuantity(availQty);
                itemDTO.setProductUnit(item.getProduct().getUnit());
                itemDTO.setUnitType(item.getProduct().getUnitType());
                return itemDTO;
            }).collect(Collectors.toList()));
        }
        return dto;
    }
}
