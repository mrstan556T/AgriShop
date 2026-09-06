package com.agrishop.web.bean;

import com.agrishop.dto.UserDTO;
import com.agrishop.service.UserServiceLocal;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;

@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    @EJB
    private UserServiceLocal userService;

    private String username;
    private String password;
    private UserDTO currentUser;

    public String login() {
        currentUser = userService.login(username, password);
        
        if (currentUser != null) {
            // Lưu thông tin vào Session
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("user", currentUser);
            
            if ("ADMIN".equals(currentUser.getRole())) {
                return "/admin/dashboard.xhtml?faces-redirect=true";
            } else {
                return "/index.xhtml?faces-redirect=true";
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi đăng nhập", "Tài khoản hoặc mật khẩu không chính xác"));
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