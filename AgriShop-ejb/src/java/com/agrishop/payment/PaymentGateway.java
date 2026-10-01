package com.agrishop.payment;

import com.agrishop.entity.Orders;
import java.util.Map;

public interface PaymentGateway {

    /**
     * Khởi tạo giao dịch thanh toán cho đơn hàng
     * @param order Đơn hàng cần thanh toán
     * @param returnUrl URL callback sau khi thanh toán xong
     * @return PaymentResponse chứa URL redirect (nếu có) và trạng thái
     */
    PaymentResponse createPayment(Orders order, String returnUrl);

    /**
     * Xác thực kết quả callback nhận về từ cổng thanh toán
     * @param callbackParams Toàn bộ tham số nhận từ callback request
     * @return PaymentCallbackResult trạng thái thành công/thất bại và mã đơn
     */
    PaymentCallbackResult verifyCallback(Map<String, String> callbackParams);

    /**
     * Mã định danh cổng thanh toán (COD, VNPAY...)
     */
    String getGatewayCode();
}
