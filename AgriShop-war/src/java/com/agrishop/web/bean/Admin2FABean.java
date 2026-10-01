package com.agrishop.web.bean;

import com.agrishop.dto.UserDTO;
import com.agrishop.service.UserServiceLocal;
import com.agrishop.util.TotpUtils;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;

@Named("admin2faBean")
@ViewScoped
public class Admin2FABean implements Serializable {

    @EJB
    private UserServiceLocal userService;

    private UserDTO pendingUser;
    private String totpCode;
    private String secretKey;
    private String qrCodeUrl;
    private String otpAuthUrl;
    private boolean isSetupMode;

    @PostConstruct
    public void init() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        pendingUser = (UserDTO) facesContext.getExternalContext().getSessionMap().get("pending2faUser");

        if (pendingUser == null) {
            try {
                facesContext.getExternalContext().redirect(
                    facesContext.getExternalContext().getRequestContextPath() + "/login.xhtml");
            } catch (Exception ignored) {}
            return;
        }

        // Lấy secret key từ database hoặc sinh mới nếu chưa có
        secretKey = userService.getOrCreateAdminTotpSecret(pendingUser.getId());
        
        // Nếu admin chưa từng kích hoạt 2FA thành công -> setup mode để hiện QR
        isSetupMode = Boolean.FALSE.equals(pendingUser.getTotpEnabled());
        
        if (secretKey != null && !secretKey.isEmpty()) {
            otpAuthUrl = TotpUtils.getOtpAuthUrl(pendingUser.getUsername(), secretKey, "AgriShop Admin");
            qrCodeUrl = TotpUtils.getQrCodeImageUrl(otpAuthUrl);
        }
    }

    public String verify() {
        if (pendingUser == null) {
            return "/login.xhtml?faces-redirect=true";
        }

        String code = totpCode != null ? totpCode.trim() : "";
        if (code.length() != 6 || !code.matches("^[0-9]{6}$")) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Mã không hợp lệ", 
                    "Vui lòng nhập chính xác mã gồm 6 chữ số từ ứng dụng xác thực."));
            return null;
        }

        boolean valid = userService.verifyAdminTotpLogin(pendingUser.getId(), code);
        if (valid) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            // Đánh dấu xác thực 2FA thành công và cấp phiên làm việc
            pendingUser.setTotpEnabled(true);
            facesContext.getExternalContext().getSessionMap().put("user", pendingUser);
            facesContext.getExternalContext().getSessionMap().put("admin2faVerified", Boolean.TRUE);
            facesContext.getExternalContext().getSessionMap().remove("pending2faUser");

            facesContext.getExternalContext().getFlash().setKeepMessages(true);
            facesContext.addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Xác thực thành công", 
                    "Đăng nhập hệ thống quản trị với 2FA an toàn."));
            return "/admin/dashboard.xhtml?faces-redirect=true";
        } else {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Mã 2FA không chính xác", 
                    "Mã xác thực không đúng hoặc đã hết hạn 30 giây. Vui lòng thử lại."));
            return null;
        }
    }

    public String cancel() {
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("pending2faUser");
        return "/login.xhtml?faces-redirect=true";
    }

    // Getters và Setters
    public UserDTO getPendingUser() { return pendingUser; }
    public String getTotpCode() { return totpCode; }
    public void setTotpCode(String totpCode) { this.totpCode = totpCode; }
    public String getSecretKey() { return secretKey; }
    public String getQrCodeUrl() { return qrCodeUrl; }
    public String getOtpAuthUrl() { return otpAuthUrl; }
    public boolean isSetupMode() { return isSetupMode; }
}
