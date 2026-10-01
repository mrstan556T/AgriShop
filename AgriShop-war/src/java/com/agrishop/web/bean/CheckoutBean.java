package com.agrishop.web.bean;

import com.agrishop.dto.CouponDTO;
import com.agrishop.dto.OrderDTO;
import com.agrishop.dto.UserDTO;
import com.agrishop.service.CheckoutServiceLocal;
import com.agrishop.service.CouponServiceLocal;
import com.agrishop.service.OrderManagementServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

@Named("checkoutBean")
@ViewScoped
public class CheckoutBean implements Serializable {

    @EJB
    private CheckoutServiceLocal checkoutService;

    @EJB
    private CouponServiceLocal couponService;

    @EJB
    private com.agrishop.payment.PaymentServiceLocal paymentService;

    @EJB
    private OrderManagementServiceLocal orderManagementService;

    @EJB
    private com.agrishop.service.SystemSettingServiceLocal systemSettingService;

    @Inject
    private LoginBean loginBean;
    
    @Inject
    private CartBean cartBean;

    private String shippingAddress;
    private String phone;
    private String paymentMethod = "COD"; // "COD" | "VNPAY" | "VIETQR"
    
    // Coupon fields
    private String couponCode;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private String appliedCouponCode;
    
    // VietQR / SePay: mã đơn đang chờ thanh toán
    private String pendingOrderCode;
    // Cờ hiển thị panel QR
    private boolean showQrPanel = false;
    // Kết quả poll: đã PAID chưa
    private boolean paymentConfirmed = false;
    
    // For passing the successful order to the success page
    private String completedOrderCode;

    @PostConstruct
    public void init() {
        if (loginBean != null && loginBean.getCurrentUser() != null) {
            UserDTO u = loginBean.getCurrentUser();
            if (u.getAddress() != null && !u.getAddress().trim().isEmpty()) {
                this.shippingAddress = u.getAddress().trim();
            }
            if (u.getPhone() != null && !u.getPhone().trim().isEmpty()) {
                this.phone = u.getPhone().trim();
            }
        }
    }

    public void applyCoupon() {
        if (couponCode == null || couponCode.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Thông báo", "Vui lòng nhập mã giảm giá."));
            return;
        }
        String cleanCode = couponCode.trim().toUpperCase();
        CouponDTO coupon = couponService.getCouponByCode(cleanCode);
        if (coupon == null || !"ACTIVE".equalsIgnoreCase(coupon.getStatus())) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Mã giảm giá '" + cleanCode + "' không tồn tại hoặc đã hết hiệu lực."));
            return;
        }

        Date now = new Date();
        if (coupon.getStartDate() != null && now.before(coupon.getStartDate())) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Mã giảm giá '" + cleanCode + "' chưa đến thời gian áp dụng."));
            return;
        }
        if (coupon.getEndDate() != null && now.after(coupon.getEndDate())) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Mã giảm giá '" + cleanCode + "' đã hết hạn sử dụng."));
            return;
        }

        int used = coupon.getUsedCount() != null ? coupon.getUsedCount() : 0;
        if (coupon.getUsageLimit() != null && used >= coupon.getUsageLimit()) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Mã giảm giá '" + cleanCode + "' đã hết lượt sử dụng."));
            return;
        }

        BigDecimal subtotal = (cartBean != null && cartBean.getCart() != null && cartBean.getCart().getTotalAmount() != null)
                ? cartBean.getCart().getTotalAmount() : BigDecimal.ZERO;
        if (coupon.getMinOrderValue() != null && subtotal.compareTo(coupon.getMinOrderValue()) < 0) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Đơn hàng tối thiểu phải từ " + String.format("%,.0f₫", coupon.getMinOrderValue()) + " để áp dụng."));
            return;
        }

        if ("PERCENTAGE".equalsIgnoreCase(coupon.getDiscountType())) {
            discountAmount = subtotal.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        } else {
            discountAmount = coupon.getDiscountValue();
        }

        if (discountAmount.compareTo(subtotal) > 0) {
            discountAmount = subtotal;
        }

        appliedCouponCode = cleanCode;
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đã áp dụng mã giảm giá " + cleanCode + ": -" + String.format("%,.0f₫", discountAmount)));
    }

    public void removeCoupon() {
        couponCode = null;
        appliedCouponCode = null;
        discountAmount = BigDecimal.ZERO;
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đã gỡ bỏ mã giảm giá."));
    }

    public BigDecimal getFinalTotal() {
        BigDecimal subtotal = (cartBean != null && cartBean.getCart() != null && cartBean.getCart().getTotalAmount() != null)
                ? cartBean.getCart().getTotalAmount() : BigDecimal.ZERO;
        BigDecimal res = subtotal.subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
        return res.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : res;
    }

    public void placeOrder() {
        UserDTO currentUser = loginBean.getCurrentUser();
        if (currentUser == null) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect(
                    FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/login.xhtml");
            } catch (Exception e) {}
            return;
        }
        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Tài khoản Quản trị viên",
                    "Bạn đang xem bằng tài khoản Quản trị viên — không thể thực hiện đặt hàng trên Storefront."));
            return;
        }
        
        String addr = shippingAddress != null ? shippingAddress.trim() : "";
        String ph = phone != null ? phone.trim() : "";

        if (addr.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("checkoutForm:address", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Vui lòng nhập địa chỉ nhận hàng."));
            return;
        }
        if (addr.length() < 5 || addr.length() > 255) {
            FacesContext.getCurrentInstance().addMessage("checkoutForm:address", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Địa chỉ nhận hàng phải từ 5 đến 255 ký tự."));
            return;
        }
        if (ph.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("checkoutForm:phone", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Vui lòng nhập số điện thoại người nhận."));
            return;
        }
        if (!ph.matches("^0[0-9]{9,10}$")) {
            FacesContext.getCurrentInstance().addMessage("checkoutForm:phone", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Số điện thoại không hợp lệ (gồm 10-11 số, bắt đầu bằng số 0)."));
            return;
        }

        try {
            OrderDTO order = checkoutService.placeOrder(currentUser.getId(), addr, ph, appliedCouponCode, paymentMethod);
            
            // Reload cart so header badge updates to 0
            cartBean.loadCart();

            if ("VNPAY".equalsIgnoreCase(paymentMethod)) {
                // --- Luồng VNPay mock redirect ---
                String appPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
                String returnUrl = appPath + "/payment-return.xhtml";
                com.agrishop.payment.PaymentResponse pRes = paymentService.initPayment(order.getId(), "VNPAY", returnUrl);
                if (pRes != null && pRes.isRedirectRequired() && pRes.getPaymentUrl() != null) {
                    FacesContext.getCurrentInstance().getExternalContext().redirect(appPath + pRes.getPaymentUrl());
                    return;
                }
            } else if ("VIETQR".equalsIgnoreCase(paymentMethod)) {
                // --- Luồng VietQR: hiện panel QR, chờ SePay callback ---
                pendingOrderCode = order.getOrderCode();
                showQrPanel = true;
                paymentConfirmed = false;
                return; // Không redirect — JSF re-render panel QR
            }
            
            // Standard COD success path
            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("orderCode", order.getOrderCode());
            FacesContext.getCurrentInstance().getExternalContext().redirect(
                FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/order-success.xhtml");
                
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi đặt hàng", e.getMessage()));
        }
    }

    /**
     * Ajax polling từ checkout page — JSF gọi mỗi 3 giây để check trạng thái thanh toán.
     * Nếu đã PAID: set flag paymentConfirmed=true → JSF oncomplete sẽ redirect.
     */
    public void checkPaymentStatus() {
        if (pendingOrderCode == null || pendingOrderCode.isBlank() || paymentConfirmed) return;
        try {
            String status = orderManagementService.getPaymentStatusByOrderCode(pendingOrderCode);
            if ("PAID".equalsIgnoreCase(status)) {
                paymentConfirmed = true;
                showQrPanel = false;
            }
        } catch (Exception e) { /* ignore polling error */ }
    }

    /** VietQR URL chuẩn theo https://vietqr.io — MB Bank (MB) **/
    public String getVietQrUrl() {
        if (pendingOrderCode == null) return "";
        BigDecimal amount = getFinalTotal();
        // Format: https://img.vietqr.io/image/{BANK_ID}-{ACCOUNT_NO}-{TEMPLATE}.png?amount=...&addInfo=...&accountName=...
        String bankId      = "MB";                  // Mã ngân hàng VietQR
        String accountNo   = "0000000000";           // ← THAY BẰNG SỐ TÀI KHOẢN THẬT
        String accountName = "AGRISHOP+COMPANY";    // ← THAY BẰNG TÊN TÀI KHOẢN THẬT
        String template    = "compact2";
        String addInfo     = "AGRI+" + pendingOrderCode.replace("-", "%2D");
        return String.format(
            "https://img.vietqr.io/image/%s-%s-%s.png?amount=%s&addInfo=%s&accountName=%s",
            bankId, accountNo, template, amount.toBigInteger(), addInfo, accountName
        );
    }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getCompletedOrderCode() { return completedOrderCode; }
    public void setCompletedOrderCode(String completedOrderCode) { this.completedOrderCode = completedOrderCode; }
    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public String getAppliedCouponCode() { return appliedCouponCode; }
    public void setAppliedCouponCode(String appliedCouponCode) { this.appliedCouponCode = appliedCouponCode; }
    public String getPendingOrderCode() { return pendingOrderCode; }
    public boolean isShowQrPanel() { return showQrPanel; }
    public boolean isPaymentConfirmed() { return paymentConfirmed; }
    
    public boolean isVietQrPaymentEnabled() {
        return systemSettingService != null && systemSettingService.getBooleanSetting("VIETQR_PAYMENT_ENABLED", false);
    }
    
    public boolean isVnpayPaymentEnabled() {
        return systemSettingService != null && systemSettingService.getBooleanSetting("VNPAY_PAYMENT_ENABLED", false);
    }

    public List<jakarta.faces.model.SelectItem> getPaymentMethodOptions() {
        List<jakarta.faces.model.SelectItem> options = new java.util.ArrayList<>();
        options.add(new jakarta.faces.model.SelectItem("COD", "Thanh toán khi nhận hàng (COD) — Trả tiền mặt cho shipper"));
        if (isVnpayPaymentEnabled()) {
            options.add(new jakarta.faces.model.SelectItem("VNPAY", "Thẻ ATM / Visa / MasterCard qua cổng VNPAY"));
        }
        if (isVietQrPaymentEnabled()) {
            options.add(new jakarta.faces.model.SelectItem("VIETQR", "Chuyển khoản VietQR — Quét mã QR ngân hàng (MB Bank)"));
        }
        return options;
    }
}
