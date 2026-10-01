package com.agrishop.payment;

import jakarta.ejb.Local;
import java.util.Map;

@Local
public interface PaymentServiceLocal {
    PaymentResponse initPayment(Long orderId, String gatewayCode, String returnUrl) throws Exception;
    PaymentCallbackResult processCallback(String gatewayCode, Map<String, String> params) throws Exception;
}
