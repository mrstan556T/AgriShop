package com.agrishop.web.bean;

import com.agrishop.dto.UserDTO;
import com.agrishop.dto.WishlistDTO;
import com.agrishop.service.WishlistServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Named("wishlistBean")
@SessionScoped
public class WishlistBean implements Serializable {

    @EJB
    private WishlistServiceLocal wishlistService;

    @Inject
    private LoginBean loginBean;

    @Inject
    private CartBean cartBean;

    private List<WishlistDTO> wishlistItems;
    private Set<Long> wishlistedProductIds = new HashSet<>();

    @PostConstruct
    public void init() {
        loadWishlist();
    }

    public void loadWishlist() {
        UserDTO currentUser = loginBean.getCurrentUser();
        if (currentUser != null) {
            wishlistItems = wishlistService.getWishlistByUserId(currentUser.getId());
            wishlistedProductIds.clear();
            if (wishlistItems != null) {
                for (WishlistDTO item : wishlistItems) {
                    wishlistedProductIds.add(item.getProductId());
                }
            }
        } else {
            wishlistItems = Collections.emptyList();
            wishlistedProductIds.clear();
        }
    }

    public boolean isWishlisted(Long productId) {
        if (productId == null || loginBean.getCurrentUser() == null) {
            return false;
        }
        return wishlistedProductIds.contains(productId);
    }

    public void toggleWishlist(Long productId) {
        UserDTO currentUser = loginBean.getCurrentUser();
        if (currentUser == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Yêu cầu đăng nhập", "Vui lòng đăng nhập để thêm sản phẩm vào danh sách yêu thích!"));
            return;
        }
        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Tài khoản Quản trị viên", 
                    "Bạn đang xem bằng tài khoản Quản trị viên — đăng nhập bằng tài khoản khách hàng để trải nghiệm mua sắm đầy đủ."));
            return;
        }

        try {
            boolean added = wishlistService.toggleWishlist(currentUser.getId(), productId);
            if (added) {
                wishlistedProductIds.add(productId);
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Yêu thích", "Đã thêm vào danh sách yêu thích!"));
            } else {
                wishlistedProductIds.remove(productId);
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Yêu thích", "Đã xóa khỏi danh sách yêu thích!"));
            }
            // Reload full list
            wishlistItems = wishlistService.getWishlistByUserId(currentUser.getId());
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", e.getMessage()));
        }
    }

    public void removeFromWishlist(Long productId) {
        UserDTO currentUser = loginBean.getCurrentUser();
        if (currentUser == null) return;

        try {
            wishlistService.removeFromWishlist(currentUser.getId(), productId);
            wishlistedProductIds.remove(productId);
            wishlistItems = wishlistService.getWishlistByUserId(currentUser.getId());
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đã bỏ sản phẩm khỏi danh sách yêu thích!"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", e.getMessage()));
        }
    }

    public void addToCart(Long productId) {
        cartBean.addToCart(productId, BigDecimal.ONE);
    }

    public int getWishlistCount() {
        return wishlistedProductIds.size();
    }

    public List<WishlistDTO> getWishlistItems() {
        return wishlistItems;
    }
}
