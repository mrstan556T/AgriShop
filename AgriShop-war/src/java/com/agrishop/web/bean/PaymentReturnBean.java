package com.agrishop.web.bean;

import com.agrishop.payment.PaymentCallbackResult;
import com.agrishop.payment.PaymentServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Map;

@Named("paymentReturnBean")
@ViewScoped
public class PaymentReturnBean implements Serializable {

    @EJB
    private PaymentServiceLocal paymentService;

    private boolean success;
    private String orderCode;
    private String transactionRef;
    private String responseCode;
    private String message;
    private boolean processed;

    @PostConstruct
    public void init() {
        // Prevent browser caching / bfcache of payment return result
        try {
            Object respObj = FacesContext.getCurrentInstance().getExternalContext().getResponse();
            if (respObj instanceof jakarta.servlet.http.HttpServletResponse) {
                jakarta.servlet.http.HttpServletResponse response = (jakarta.servlet.http.HttpServletResponse) respObj;
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                response.setHeader("Pragma", "no-cache");
                response.setDateHeader("Expires", 0);
            }
        } catch (Exception ignored) {
        }

        Map<String, String> params = FacesContext.getCurrentInstance()
                                                 .getExternalContext()
                                                 .getRequestParameterMap();
        String txnRef = params != null ? params.get("vnp_TxnRef") : null;
        String respCode = params != null ? params.get("vnp_ResponseCode") : null;

        if (txnRef != null && !txnRef.trim().isEmpty() && respCode != null && !respCode.trim().isEmpty()) {
            try {
                PaymentCallbackResult res = paymentService.processCallback("VNPAY", params);
                if (res != null) {
                    this.success = res.isSuccess();
                    this.orderCode = res.getOrderCode();
                    this.transactionRef = res.getTransactionRef();
                    this.responseCode = res.getResponseCode();
                    this.message = res.getMessage();
                } else {
                    this.success = false;
                    this.message = "Không nhận được phản hồi hợp lệ từ cổng thanh toán.";
                }
            } catch (Exception e) {
                this.success = false;
                this.message = "Lỗi xử lý phản hồi thanh toán: " + e.getMessage();
            }
            this.processed = true;
        } else {
            this.processed = false;
            this.success = false;
            this.message = "Không tìm thấy thông tin giao dịch thanh toán hợp lệ hoặc giao dịch đã kết thúc.";
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public String getMessage() {
        return message;
    }

    public boolean isProcessed() {
        return processed;
    }
}
