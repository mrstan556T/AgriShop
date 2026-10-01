package com.agrishop.web.bean;

import com.agrishop.service.UserServiceLocal;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;

@Named("registerBean")
@ViewScoped
public class RegisterBean implements Serializable {

    @EJB
    private UserServiceLocal userService;

    private String username;
    private String password;
    private String confirmPassword;
    private String fullName;
    private String phone;
    private String email;

    public String register() {
        String u = username != null ? username.trim() : "";
        String f = fullName != null ? fullName.trim() : "";
        String p = phone != null ? phone.trim() : "";
        String e = email != null ? email.trim() : "";
        String pwd = password != null ? password : "";
        String cpwd = confirmPassword != null ? confirmPassword : "";

        if (f.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("registerForm:fullName", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Vui lòng nhập họ và tên"));
            return null;
        }
        if (f.length() < 3 || f.length() > 50) {
            FacesContext.getCurrentInstance().addMessage("registerForm:fullName", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Họ và tên phải có từ 3 đến 50 ký tự"));
            return null;
        }
        if (u.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("registerForm:username", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Vui lòng nhập tên đăng nhập"));
            return null;
        }
        if (!u.matches("^[a-zA-Z0-9_]{4,20}$")) {
            FacesContext.getCurrentInstance().addMessage("registerForm:username", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Tên đăng nhập từ 4-20 ký tự, chỉ gồm chữ cái, số và dấu gạch dưới"));
            return null;
        }
        if (e.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("registerForm:email", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Vui lòng nhập email"));
            return null;
        }
        if (!e.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            FacesContext.getCurrentInstance().addMessage("registerForm:email", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Email không đúng định dạng (ví dụ: email@domain.com)"));
            return null;
        }
        if (p.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("registerForm:phone", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Vui lòng nhập số điện thoại"));
            return null;
        }
        if (!p.matches("^0[0-9]{9,10}$")) {
            FacesContext.getCurrentInstance().addMessage("registerForm:phone", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Số điện thoại không hợp lệ (gồm 10-11 số, bắt đầu bằng 0)"));
            return null;
        }
        if (pwd.length() < 6) {
            FacesContext.getCurrentInstance().addMessage("registerForm:password", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Mật khẩu phải có ít nhất 6 ký tự"));
            return null;
        }
        if (!pwd.equals(cpwd)) {
            FacesContext.getCurrentInstance().addMessage("registerForm:confirmPassword", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Mật khẩu xác nhận không khớp"));
            return null;
        }

        try {
            jakarta.servlet.http.HttpServletRequest req = (jakarta.servlet.http.HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            String scheme = req.getScheme();
            String serverName = req.getServerName();
            int port = req.getServerPort();
            String contextPath = req.getContextPath();
            String baseUrl = ((scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443)) 
                    ? (scheme + "://" + serverName + contextPath) 
                    : (scheme + "://" + serverName + ":" + port + contextPath);

            userService.registerCustomer(u, pwd, f, p, e, baseUrl);
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Đăng ký thành công!", 
                    "Liên kết kích hoạt tài khoản đã được gửi đến email " + e + ". Vui lòng kiểm tra hộp thư để kích hoạt tài khoản trước khi đăng nhập."));
            return "/login.xhtml?faces-redirect=true";
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi đăng ký", ex.getMessage()));
            return null;
        }
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
