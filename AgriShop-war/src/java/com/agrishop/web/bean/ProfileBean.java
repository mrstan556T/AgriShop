package com.agrishop.web.bean;

import com.agrishop.dto.OrderDTO;
import com.agrishop.service.OrderManagementServiceLocal;
import com.agrishop.service.UserServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import org.primefaces.model.file.UploadedFile;
import com.agrishop.web.util.FileUploadUtil;

@Named("profileBean")
@ViewScoped
public class ProfileBean implements Serializable {

    @EJB
    private UserServiceLocal userService;

    @EJB
    private OrderManagementServiceLocal orderService;

    @Inject
    private LoginBean loginBean;

    private String fullName;
    private String phone;
    private String address;
    private UploadedFile avatarFile;
    private boolean removeCurrentImage = false;
    private int totalOrders = 0;

    // Password fields
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;

    @PostConstruct
    public void init() {
        this.removeCurrentImage = false;
        if (loginBean.getCurrentUser() != null) {
            this.fullName = loginBean.getCurrentUser().getFullName();
            this.phone = loginBean.getCurrentUser().getPhone();
            this.address = loginBean.getCurrentUser().getAddress();
            try {
                List<OrderDTO> orders = orderService.getCustomerOrderHistory(loginBean.getCurrentUser().getId());
                this.totalOrders = (orders != null) ? orders.size() : 0;
            } catch (Exception e) {
                this.totalOrders = 0;
            }
        }
    }

    public void updateProfileInfo() {
        if (loginBean.getCurrentUser() == null) {
            return;
        }

        String fn = fullName != null ? fullName.trim() : "";
        String ph = phone != null ? phone.trim() : "";
        String addr = address != null ? address.trim() : "";

        if (fn.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("profileInfoForm:fullName", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Vui lòng nhập họ và tên"));
            return;
        }
        if (fn.length() < 3 || fn.length() > 50) {
            FacesContext.getCurrentInstance().addMessage("profileInfoForm:fullName", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Họ và tên phải có từ 3 đến 50 ký tự"));
            return;
        }

        if (!ph.isEmpty() && !ph.matches("^0[0-9]{9,10}$")) {
            FacesContext.getCurrentInstance().addMessage("profileInfoForm:phone", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Số điện thoại không hợp lệ (gồm 10-11 số, bắt đầu bằng 0)"));
            return;
        }

        String avatarUrl = loginBean.getCurrentUser().getAvatarUrl();
        if (removeCurrentImage) {
            avatarUrl = "";
        }
        if (avatarFile != null && avatarFile.getSize() > 0) {
            try {
                String path = FileUploadUtil.saveFile(avatarFile, "avatars");
                if (path != null) {
                    avatarUrl = path;
                }
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi tải ảnh đại diện", e.getMessage()));
                return;
            }
        }

        boolean success = userService.updateProfile(loginBean.getCurrentUser().getId(), fn, null, ph, addr, avatarUrl);
        
        if (success) {
            loginBean.getCurrentUser().setFullName(fn);
            loginBean.getCurrentUser().setPhone(ph);
            loginBean.getCurrentUser().setAddress(addr);
            loginBean.getCurrentUser().setAvatarUrl((avatarUrl != null && !avatarUrl.isEmpty()) ? avatarUrl : null);
            removeCurrentImage = false;
            avatarFile = null;
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đã cập nhật thông tin cá nhân thành công."));
        } else {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Không thể cập nhật hồ sơ!"));
        }
    }

    public void changePassword() {
        if (loginBean.getCurrentUser() == null) {
            return;
        }

        String cur = currentPassword != null ? currentPassword.trim() : "";
        String np = newPassword != null ? newPassword.trim() : "";
        String cp = confirmPassword != null ? confirmPassword.trim() : "";

        boolean hasError = false;

        if (cur.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("passwordForm:curPass", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Vui lòng nhập mật khẩu hiện tại"));
            hasError = true;
        }

        if (np.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("passwordForm:newPass", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Vui lòng nhập mật khẩu mới"));
            hasError = true;
        } else if (np.length() < 6) {
            FacesContext.getCurrentInstance().addMessage("passwordForm:newPass", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Mật khẩu mới phải có ít nhất 6 ký tự"));
            hasError = true;
        }

        if (cp.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("passwordForm:confirmPass", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Vui lòng xác nhận mật khẩu mới"));
            hasError = true;
        } else if (!np.isEmpty() && !np.equals(cp)) {
            FacesContext.getCurrentInstance().addMessage("passwordForm:confirmPass", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Mật khẩu xác nhận không khớp"));
            hasError = true;
        }

        if (hasError) {
            return;
        }

        try {
            userService.changePassword(loginBean.getCurrentUser().getId(), cur, np);
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đã đổi mật khẩu thành công."));
            currentPassword = null;
            newPassword = null;
            confirmPassword = null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("passwordForm:curPass", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", e.getMessage()));
        }
    }

    public void updateProfile() {
        updateProfileInfo();
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public UploadedFile getAvatarFile() { return avatarFile; }
    public void setAvatarFile(UploadedFile avatarFile) { this.avatarFile = avatarFile; }
    public boolean isRemoveCurrentImage() { return removeCurrentImage; }
    public void setRemoveCurrentImage(boolean removeCurrentImage) { this.removeCurrentImage = removeCurrentImage; }
    public int getTotalOrders() { return totalOrders; }
}
