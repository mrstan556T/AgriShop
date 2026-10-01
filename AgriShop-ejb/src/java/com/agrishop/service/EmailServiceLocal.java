package com.agrishop.service;

import com.agrishop.entity.Orders;
import jakarta.ejb.Local;

@Local
public interface EmailServiceLocal {
    void sendOrderStatusEmail(Orders order, String newStatus);
    void sendActivationEmail(String toEmail, String fullName, String activationLink);
    void sendPasswordResetEmail(String toEmail, String fullName, String resetLink);
}
