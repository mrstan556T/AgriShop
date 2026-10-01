package com.agrishop.payment;

import com.agrishop.entity.Orders;
import jakarta.enterprise.context.ApplicationScoped;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@ApplicationScoped
public class VnPayPaymentGateway implements PaymentGateway {

    @Override
    public PaymentResponse createPayment(Orders order, String returnUrl) {
        if (order == null) {
            return new PaymentResponse(false, null, null, "Đơn hàng không hợp lệ.");
        }

        try {
            String encodedOrderCode = URLEncoder.encode(order.getOrderCode(), StandardCharsets.UTF_8);
            String encodedOrderInfo = URLEncoder.encode("Thanh toan don hang " + order.getOrderCode(), StandardCharsets.UTF_8);
            String amount = order.getTotalAmount() != null ? order.getTotalAmount().toPlainString() : "0";
            
            // Xây dựng URL điều hướng sang giao diện mô phỏng thanh toán VNPAY / VietQR
            String targetReturn = returnUrl != null ? returnUrl : "/payment-return.xhtml";
            String encodedReturnUrl = URLEncoder.encode(targetReturn, StandardCharsets.UTF_8);

            String mockPaymentUrl = "/vnpay-mock.xhtml?vnp_TxnRef=" + encodedOrderCode 
                                  + "&vnp_Amount=" + amount 
                                  + "&vnp_OrderInfo=" + encodedOrderInfo 
                                  + "&returnUrl=" + encodedReturnUrl;

            return new PaymentResponse(
                true,
                mockPaymentUrl,
                order.getOrderCode(),
                "Chuyển hướng đến cổng thanh toán trực tuyến VNPAY / VietQR..."
            );
        } catch (Exception e) {
            return new PaymentResponse(false, null, order.getOrderCode(), "Lỗi tạo giao dịch thanh toán: " + e.getMessage());
        }
    }

    @Override
    public PaymentCallbackResult verifyCallback(Map<String, String> callbackParams) {
        if (callbackParams == null || callbackParams.isEmpty()) {
            return new PaymentCallbackResult(false, null, null, "99", "Dữ liệu callback rỗng.");
        }

        String orderCode = callbackParams.get("vnp_TxnRef");
        String responseCode = callbackParams.get("vnp_ResponseCode");
        String transactionRef = callbackParams.get("vnp_TransactionNo");

        // Theo chuẩn VNPAY: mã phản hồi "00" biểu thị thanh toán thành công
        boolean isSuccess = "00".equals(responseCode);
        String message = isSuccess 
            ? "Thanh toán trực tuyến VNPAY thành công." 
            : ("Giao dịch thanh toán VNPAY không thành công hoặc đã bị hủy (Mã phản hồi: " + responseCode + ").");

        return new PaymentCallbackResult(isSuccess, orderCode, transactionRef, responseCode, message);
    }

    @Override
    public String getGatewayCode() {
        return "VNPAY";
    }
}
