package com.agrishop.web.bean;

import com.agrishop.service.UserServiceLocal;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;

@Named("accountActivationBean")
@ViewScoped
public class AccountActivationBean implements Serializable {

    @EJB
    private UserServiceLocal userService;

    private String token;
    private Boolean activated;
    private String message;

    public void processActivation() {
        if (token == null || token.trim().isEmpty()) {
            activated = false;
            message = "Mã xác nhận (token) kích hoạt không hợp lệ hoặc thiếu trong liên kết.";
            return;
        }

        try {
            boolean success = userService.activateAccount(token.trim());
            if (success) {
                activated = true;
                message = "Tài khoản thành viên AgriShop của bạn đã được kích hoạt thành công! Giờ đây bạn có thể đăng nhập và trải nghiệm mua sắm.";
            } else {
                activated = false;
                message = "Liên kết kích hoạt này không tồn tại, đã được sử dụng trước đó hoặc đã hết hạn (24 giờ).";
            }
        } catch (Exception e) {
            activated = false;
            message = "Có lỗi xảy ra trong quá trình kích hoạt: " + e.getMessage();
        }
    }

    // Getters và Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Boolean getActivated() { return activated; }
    public void setActivated(Boolean activated) { this.activated = activated; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
