package com.agrishop.service;

import com.agrishop.dto.OrderDTO;
import jakarta.ejb.Local;

@Local
public interface CheckoutServiceLocal {
    OrderDTO placeOrder(Long userId, String shippingAddress, String phone) throws Exception;
    OrderDTO placeOrder(Long userId, String shippingAddress, String phone, String couponCode) throws Exception;
    OrderDTO placeOrder(Long userId, String shippingAddress, String phone, String couponCode, String paymentMethod) throws Exception;
}
