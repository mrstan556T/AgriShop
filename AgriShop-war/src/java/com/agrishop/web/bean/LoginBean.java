package com.agrishop.web.bean;

import com.agrishop.dto.UserDTO;
import com.agrishop.service.CartServiceLocal;
import com.agrishop.service.LoginAttemptServiceLocal;
import com.agrishop.service.UserServiceLocal;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;

@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    @EJB
    private UserServiceLocal userService;

    @EJB
    private CartServiceLocal cartService;

    @EJB
    private LoginAttemptServiceLocal loginAttemptService;

    @Inject
    private CartBean cartBean;

    @Inject
    private WishlistBean wishlistBean;

    private String username;
    private String password;
    private UserDTO currentUser;

    private String getClientIp() {
        try {
            HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            String xfHeader = req.getHeader("X-Forwarded-For");
            if (xfHeader == null || xfHeader.isEmpty()) {
                return req.getRemoteAddr();
            }
            return xfHeader.split(",")[0].trim();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    public String login() {
        String u = username != null ? username.trim() : "";
        String p = password != null ? password.trim() : "";
        String clientIp = getClientIp();

        if (u.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("loginForm:username", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Vui lòng nhập tên đăng nhập"));
            return null;
        }
        if (p.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("loginForm:password", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Vui lòng nhập mật khẩu"));
            return null;
        }

        // 1. Kiểm tra Brute-Force lockout
        long lockMinUser = loginAttemptService != null ? loginAttemptService.getRemainingLockMinutes(u) : 0;
        long lockMinIp = loginAttemptService != null ? loginAttemptService.getRemainingLockMinutes(clientIp) : 0;
        long remainingLock = Math.max(lockMinUser, lockMinIp);

        if (remainingLock > 0) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Tài khoản bị tạm khóa", 
                    "Bạn đã nhập sai mật khẩu quá 5 lần liên tiếp. Để bảo vệ an toàn, hệ thống tạm thời khóa đăng nhập. Vui lòng thử lại sau " + remainingLock + " phút."));
            return null;
        }

        currentUser = userService.login(u, password);
        
        if (currentUser != null) {
            // Kiểm tra trạng thái tài khoản
            if ("INACTIVE".equalsIgnoreCase(currentUser.getStatus())) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Tài khoản bị vô hiệu", 
                        "Tài khoản này đã bị khóa bởi quản trị viên. Vui lòng liên hệ hotline hỗ trợ."));
                return null;
            }

            // Kiểm tra xác thực email (chỉ cho phép khi isEmailVerified == true và status != PENDING_ACTIVATION)
            if (Boolean.FALSE.equals(currentUser.getIsEmailVerified()) || "PENDING_ACTIVATION".equalsIgnoreCase(currentUser.getStatus())) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Tài khoản chưa kích hoạt", 
                        "Tài khoản của bạn chưa được kích hoạt qua email. Vui lòng kiểm tra hộp thư đến hoặc thư rác để nhấn vào liên kết kích hoạt."));
                return null;
            }

            // Đăng nhập thành công -> Reset số lần brute-force
            if (loginAttemptService != null) {
                loginAttemptService.resetAttempts(u);
                loginAttemptService.resetAttempts(clientIp);
            }

            // 2FA TOTP cho Admin
            if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
                // Lưu user tạm thời chờ xác thực 2FA, CHƯA cấp phiên "user" và "admin2faVerified"
                FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("pending2faUser", currentUser);
                FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("user");
                FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("admin2faVerified");
                return "/admin-2fa.xhtml?faces-redirect=true";
            }

            // Người dùng thường (Customer / Staff)
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("user", currentUser);
            
            // Auto-merge guest cart if exists
            if (cartBean != null) {
                cartBean.mergeAfterLogin(currentUser.getId());
            }
            if (wishlistBean != null) {
                wishlistBean.loadWishlist();
            }

            if ("STAFF".equalsIgnoreCase(currentUser.getRole())) {
                return "/admin/order.xhtml?faces-redirect=true";
            } else {
                return "/index.xhtml?faces-redirect=true";
            }
        } else {
            // Đăng nhập sai -> Ghi nhận brute-force attempt
            int attempts = 1;
            if (loginAttemptService != null) {
                loginAttemptService.recordFailedAttempt(u);
                attempts = loginAttemptService.recordFailedAttempt(clientIp);
            }

            if (attempts >= 5) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Tài khoản đã bị khóa", 
                        "Bạn đã nhập sai mật khẩu 5 lần liên tiếp. Tài khoản và thiết bị của bạn bị tạm khóa 15 phút."));
            } else {
                int left = 5 - attempts;
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi đăng nhập", 
                        "Tài khoản hoặc mật khẩu không chính xác. Bạn còn " + left + " lần thử trước khi bị tạm khóa 15 phút."));
            }
            return null;
        }
    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        currentUser = null;
        return "/login.xhtml?faces-redirect=true";
    }

    // Getters và Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public UserDTO getCurrentUser() { return currentUser; }
}