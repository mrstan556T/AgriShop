package com.agrishop.web.bean;

import com.agrishop.dto.CartDTO;
import com.agrishop.dto.CartItemDTO;
import com.agrishop.dto.ProductDTO;
import com.agrishop.dto.UserDTO;
import com.agrishop.service.CartServiceLocal;
import com.agrishop.service.ProductServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;

@Named("cartBean")
@SessionScoped
public class CartBean implements Serializable {

    @EJB
    private CartServiceLocal cartService;

    @EJB
    private ProductServiceLocal productService;

    @Inject
    private LoginBean loginBean;

    private CartDTO cart;

    @PostConstruct
    public void init() {
        loadCart();
    }

    public void loadCart() {
        UserDTO currentUser = loginBean != null ? loginBean.getCurrentUser() : null;
        if (currentUser != null) {
            cart = cartService.getCartByUserId(currentUser.getId());
        } else if (cart == null) {
            cart = new CartDTO();
            cart.setItems(new ArrayList<>());
            cart.setTotalAmount(BigDecimal.ZERO);
            cart.setTotalItems(0);
        }
    }

    public CartDTO getCart() {
        if (cart == null) {
            loadCart();
        }
        return cart;
    }

    public void addToCart(Long productId) {
        addToCart(productId, BigDecimal.ONE);
    }

    public void addToCart(Long productId, Long quantity) {
        addToCart(productId, quantity != null ? BigDecimal.valueOf(quantity) : BigDecimal.ONE);
    }

    public void addToCart(Long productId, int quantity) {
        addToCart(productId, BigDecimal.valueOf(quantity));
    }

    public void addToCart(Long productId, BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Số lượng sản phẩm phải lớn hơn 0"));
            return;
        }

        UserDTO currentUser = loginBean != null ? loginBean.getCurrentUser() : null;
        if (currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Tài khoản Quản trị viên", 
                    "Bạn đang xem bằng tài khoản Quản trị viên — đăng nhập bằng tài khoản khách hàng để trải nghiệm mua sắm đầy đủ."));
            return;
        }
        if (currentUser != null) {
            // Authenticated user: persist directly to DB
            try {
                cart = cartService.addToCart(currentUser.getId(), productId, quantity);
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đã thêm vào giỏ hàng"));
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", e.getMessage()));
            }
        } else {
            // Guest user: save in Session-scoped CartBean
            try {
                ProductDTO product = productService.getProductById(productId);
                if (product == null || !Boolean.TRUE.equals(product.getIsActive())) {
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Sản phẩm không tồn tại hoặc đã ngừng kinh doanh"));
                    return;
                }

                if (cart == null) {
                    cart = new CartDTO();
                    cart.setItems(new ArrayList<>());
                } else if (cart.getItems() == null) {
                    cart.setItems(new ArrayList<>());
                }

                CartItemDTO existingItem = null;
                for (CartItemDTO it : cart.getItems()) {
                    if (it.getProductId().equals(productId)) {
                        existingItem = it;
                        break;
                    }
                }

                BigDecimal newQuantity = quantity;
                if (existingItem != null) {
                    newQuantity = existingItem.getQuantity().add(quantity);
                }

                // Check stock
                if (product.getStockQuantity() != null && newQuantity.compareTo(product.getStockQuantity()) > 0) {
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Cảnh báo", 
                            "Số lượng vượt quá tồn kho khả dụng (" + product.getStockQuantity() + " " + (product.getUnit() != null ? product.getUnit() : "") + ")"));
                    return;
                }

                if (existingItem != null) {
                    existingItem.setQuantity(newQuantity);
                    existingItem.setSubtotal(product.getPrice().multiply(newQuantity));
                } else {
                    CartItemDTO newItem = new CartItemDTO();
                    newItem.setId(-(System.currentTimeMillis() % 10000000L)); // Temporary unique ID for UI identification
                    newItem.setProductId(product.getId());
                    newItem.setProductCode(product.getProductCode());
                    newItem.setProductName(product.getName());
                    newItem.setProductImageUrl(product.getImageUrl());
                    newItem.setProductUnit(product.getUnit());
                    newItem.setUnitType(product.getUnitType() != null ? product.getUnitType() : "COUNT");
                    newItem.setProductPrice(product.getPrice());
                    newItem.setUnitPrice(product.getPrice());
                    newItem.setStockQuantity(product.getStockQuantity());
                    newItem.setQuantity(quantity);
                    newItem.setSubtotal(product.getPrice().multiply(quantity));
                    cart.getItems().add(newItem);
                }

                recalculateGuestCart(cart);
                FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("guestCart", cart);

                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đã thêm vào giỏ hàng"));
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", e.getMessage()));
            }
        }
    }

    public void updateQuantity(Long cartItemId, int newQuantity) {
        updateQuantity(cartItemId, BigDecimal.valueOf(newQuantity));
    }

    public void updateQuantity(Long cartItemId, BigDecimal newQuantity) {
        if (cartItemId == null) {
            System.err.println("[CartBean] updateQuantity called with null cartItemId! Reloading cart from database...");
            loadCart();
            return;
        }

        if (newQuantity == null || newQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            removeItem(cartItemId);
            return;
        }

        UserDTO currentUser = loginBean != null ? loginBean.getCurrentUser() : null;
        if (currentUser != null) {
            try {
                cart = cartService.updateItemQuantity(currentUser.getId(), cartItemId, newQuantity);
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", e.getMessage()));
            }
        } else {
            if (cart != null && cart.getItems() != null) {
                for (CartItemDTO item : cart.getItems()) {
                    if (item.getId().equals(cartItemId)) {
                        BigDecimal limit = item.getAvailableQuantity();
                        if (limit != null && limit.compareTo(BigDecimal.ZERO) > 0 && newQuantity.compareTo(limit) > 0) {
                            FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_WARN, "Cảnh báo", "Vượt quá tồn kho khả dụng (" + limit + ")"));
                            return;
                        }
                        item.setQuantity(newQuantity);
                        if (item.getProductPrice() != null) {
                            item.setSubtotal(item.getProductPrice().multiply(newQuantity));
                        }
                        break;
                    }
                }
                recalculateGuestCart(cart);
                FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("guestCart", cart);
            }
        }
    }

    public void increaseQuantity(Long cartItemId) {
        System.out.println("[CartBean] increaseQuantity invoked for cartItemId=" + cartItemId);
        if (cartItemId == null) {
            System.err.println("[CartBean] increaseQuantity received null cartItemId! Refreshing cart state...");
            loadCart();
            return;
        }
        if (cart != null && cart.getItems() != null) {
            for (CartItemDTO item : cart.getItems()) {
                if (java.util.Objects.equals(item.getId(), cartItemId)) {
                    BigDecimal step = "WEIGHT".equalsIgnoreCase(item.getUnitType()) ? new BigDecimal("0.5") : BigDecimal.ONE;
                    BigDecimal newQty = item.getQuantity().add(step);
                    BigDecimal limit = item.getAvailableQuantity();
                    if (limit != null && limit.compareTo(BigDecimal.ZERO) > 0 
                            && newQty.compareTo(limit) > 0) {
                        FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN, "Thông báo", "Đã đạt số lượng tồn kho khả dụng tối đa (" + limit + ")"));
                        return;
                    }
                    System.out.println("[CartBean] Updating cartItemId=" + cartItemId + " to newQty=" + newQty);
                    updateQuantity(cartItemId, newQty);
                    return;
                }
            }
        }
    }

    public void decreaseQuantity(Long cartItemId) {
        System.out.println("[CartBean] decreaseQuantity invoked for cartItemId=" + cartItemId);
        if (cartItemId == null) {
            System.err.println("[CartBean] decreaseQuantity received null cartItemId! Refreshing cart state...");
            loadCart();
            return;
        }
        if (cart != null && cart.getItems() != null) {
            for (CartItemDTO item : cart.getItems()) {
                if (java.util.Objects.equals(item.getId(), cartItemId)) {
                    BigDecimal step = "WEIGHT".equalsIgnoreCase(item.getUnitType()) ? new BigDecimal("0.5") : BigDecimal.ONE;
                    BigDecimal minQty = step;
                    if (item.getQuantity().compareTo(minQty) > 0) {
                        BigDecimal newQty = item.getQuantity().subtract(step);
                        System.out.println("[CartBean] Decreasing cartItemId=" + cartItemId + " to newQty=" + newQty);
                        updateQuantity(cartItemId, newQty);
                    } else {
                        System.out.println("[CartBean] Quantity <= minQty, removing cartItemId=" + cartItemId);
                        removeItem(cartItemId);
                    }
                    return;
                }
            }
        }
    }

    public void removeItem(Long cartItemId) {
        if (cartItemId == null) {
            System.err.println("[CartBean] removeItem received null cartItemId! Refreshing cart state...");
            loadCart();
            return;
        }
        UserDTO currentUser = loginBean != null ? loginBean.getCurrentUser() : null;
        if (currentUser != null) {
            try {
                cart = cartService.removeItem(currentUser.getId(), cartItemId);
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đã xóa sản phẩm khỏi giỏ hàng"));
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", e.getMessage()));
            }
        } else {
            if (cart != null && cart.getItems() != null) {
                cart.getItems().removeIf(it -> it.getId().equals(cartItemId));
                recalculateGuestCart(cart);
                FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("guestCart", cart);
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đã xóa sản phẩm khỏi giỏ hàng"));
            }
        }
    }

    public void clearCart() {
        UserDTO currentUser = loginBean != null ? loginBean.getCurrentUser() : null;
        if (currentUser != null) {
            try {
                cart = cartService.clearCart(currentUser.getId());
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đã làm trống giỏ hàng"));
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", e.getMessage()));
            }
        } else {
            cart = new CartDTO();
            cart.setItems(new ArrayList<>());
            cart.setTotalAmount(BigDecimal.ZERO);
            cart.setTotalItems(0);
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("guestCart", cart);
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đã làm trống giỏ hàng"));
        }
    }

    public void mergeAfterLogin(Long userId) {
        if (cart != null && cart.getItems() != null && !cart.getItems().isEmpty()) {
            try {
                cart = cartService.mergeGuestCart(userId, cart.getItems());
                FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("guestCart");
            } catch (Exception e) {
                cart = cartService.getCartByUserId(userId);
            }
        } else {
            cart = cartService.getCartByUserId(userId);
        }
    }

    public String proceedToCheckout() {
        UserDTO currentUser = loginBean != null ? loginBean.getCurrentUser() : null;
        if (currentUser == null) {
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Yêu cầu đăng nhập", "Vui lòng đăng nhập để tiến hành đặt hàng và thanh toán."));
            return "/login.xhtml?faces-redirect=true";
        }
        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Tài khoản Quản trị viên", 
                    "Bạn đang xem bằng tài khoản Quản trị viên — không thể thực hiện đặt hàng trên Storefront."));
            return null;
        }
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Giỏ hàng trống", "Vui lòng chọn sản phẩm vào giỏ hàng trước khi thanh toán."));
            return null;
        }
        return "/checkout.xhtml?faces-redirect=true";
    }

    private void recalculateGuestCart(CartDTO guestCart) {
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        if (guestCart.getItems() != null) {
            for (CartItemDTO it : guestCart.getItems()) {
                if (it.getItemTotal() != null) {
                    total = total.add(it.getItemTotal());
                }
                count++;
            }
        }
        guestCart.setTotalAmount(total);
        guestCart.setTotalItems(count);
    }
}
