package com.agrishop.payment;

import com.agrishop.entity.Orders;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;

@ApplicationScoped
public class CODPaymentGateway implements PaymentGateway {

    @Override
    public PaymentResponse createPayment(Orders order, String returnUrl) {
        return new PaymentResponse(
            false,
            null,
            order != null ? order.getOrderCode() : null,
            "Đơn hàng thanh toán khi nhận hàng (COD). Quý khách sẽ thanh toán tiền mặt trực tiếp cho shipper."
        );
    }

    @Override
    public PaymentCallbackResult verifyCallback(Map<String, String> callbackParams) {
        String orderCode = callbackParams != null ? callbackParams.get("orderCode") : null;
        return new PaymentCallbackResult(true, orderCode, null, "00", "Xác nhận COD thành công.");
    }

    @Override
    public String getGatewayCode() {
        return "COD";
    }
}
