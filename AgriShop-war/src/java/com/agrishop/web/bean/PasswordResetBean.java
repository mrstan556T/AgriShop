package com.agrishop.web.bean;

import com.agrishop.service.UserServiceLocal;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;

@Named("passwordResetBean")
@ViewScoped
public class PasswordResetBean implements Serializable {

    @EJB
    private UserServiceLocal userService;

    private String identifier; // username hoặc email
    private String token;
    private String newPassword;
    private String confirmPassword;
    private boolean requestSent = false;
    private boolean resetSuccess = false;

    private String getBaseUrl() {
        try {
            HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            String scheme = req.getScheme();
            String serverName = req.getServerName();
            int port = req.getServerPort();
            String contextPath = req.getContextPath();
            if ((scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443)) {
                return scheme + "://" + serverName + contextPath;
            } else {
                return scheme + "://" + serverName + ":" + port + contextPath;
            }
        } catch (Exception e) {
            return "http://localhost:8080/AgriShop-war";
        }
    }

    public void sendResetRequest() {
        if (identifier == null || identifier.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Vui lòng nhập tên đăng nhập hoặc email"));
            return;
        }

        try {
            userService.requestPasswordReset(identifier.trim(), getBaseUrl());
            requestSent = true;
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Yêu cầu thành công", 
                    "Liên kết đặt lại mật khẩu đã được tạo và gửi tới email của bạn (hiệu lực 30 phút). Vui lòng kiểm tra hộp thư!"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Không thể gửi yêu cầu", e.getMessage()));
        }
    }

    public String confirmReset() {
        if (token == null || token.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi token", "Thiếu mã xác nhận (token) trong liên kết."));
            return null;
        }
        if (newPassword == null || newPassword.length() < 6) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi mật khẩu", "Mật khẩu mới phải có ít nhất 6 ký tự."));
            return null;
        }
        if (!newPassword.equals(confirmPassword)) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi xác nhận", "Mật khẩu nhập lại không khớp."));
            return null;
        }

        try {
            boolean ok = userService.resetPasswordWithToken(token.trim(), newPassword);
            if (ok) {
                resetSuccess = true;
                FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Cập nhật thành công", 
                        "Mật khẩu của bạn đã được thay đổi thành công. Hãy đăng nhập bằng mật khẩu mới!"));
                return "/login.xhtml?faces-redirect=true";
            } else {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi liên kết", 
                        "Liên kết đặt lại mật khẩu này đã hết hạn hoặc không tồn tại."));
                return null;
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Thất bại", e.getMessage()));
            return null;
        }
    }

    // Getters và Setters
    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    public boolean isRequestSent() { return requestSent; }
    public boolean isResetSuccess() { return resetSuccess; }
}
