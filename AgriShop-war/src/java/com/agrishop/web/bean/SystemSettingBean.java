package com.agrishop.web.bean;

import com.agrishop.service.SystemSettingServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;

@Named("systemSettingBean")
@ViewScoped
public class SystemSettingBean implements Serializable {

    @EJB
    private SystemSettingServiceLocal settingService;

    private BigDecimal freeShippingThreshold;
    private BigDecimal shippingFee;
    private int returnWindowDays;

    @PostConstruct
    public void init() {
        loadSettings();
    }

    public void loadSettings() {
        this.freeShippingThreshold = settingService.getBigDecimalSetting("FREE_SHIPPING_THRESHOLD", new BigDecimal("300000"));
        this.shippingFee = settingService.getBigDecimalSetting("SHIPPING_FEE", new BigDecimal("30000"));
        this.returnWindowDays = settingService.getIntSetting("RETURN_WINDOW_DAYS", 7);
    }

    public void saveSettings() {
        if (freeShippingThreshold == null || freeShippingThreshold.compareTo(BigDecimal.ZERO) < 0) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Ngưỡng miễn phí vận chuyển phải lớn hơn hoặc bằng 0."));
            return;
        }
        if (shippingFee == null || shippingFee.compareTo(BigDecimal.ZERO) < 0) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Phí giao hàng & bảo quản phải lớn hơn hoặc bằng 0."));
            return;
        }
        if (returnWindowDays < 1 || returnWindowDays > 90) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Thời hạn cho phép đổi trả phải từ 1 đến 90 ngày."));
            return;
        }

        try {
            settingService.updateSetting("FREE_SHIPPING_THRESHOLD", freeShippingThreshold.toPlainString());
            settingService.updateSetting("SHIPPING_FEE", shippingFee.toPlainString());
            settingService.updateSetting("RETURN_WINDOW_DAYS", String.valueOf(returnWindowDays));

            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đã cập nhật cấu hình hệ thống thành công."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi cập nhật", e.getMessage()));
        }
    }

    public BigDecimal getFreeShippingThreshold() {
        return freeShippingThreshold;
    }

    public void setFreeShippingThreshold(BigDecimal freeShippingThreshold) {
        this.freeShippingThreshold = freeShippingThreshold;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }

    public int getReturnWindowDays() {
        return returnWindowDays;
    }

    public void setReturnWindowDays(int returnWindowDays) {
        this.returnWindowDays = returnWindowDays;
    }
}
