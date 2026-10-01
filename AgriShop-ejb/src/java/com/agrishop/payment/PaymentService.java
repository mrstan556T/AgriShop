package com.agrishop.payment;

import com.agrishop.entity.Orders;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Stateless
public class PaymentService implements PaymentServiceLocal {

    @PersistenceContext(unitName = "AgriShopPU")
    private EntityManager em;

    @Inject
    private CODPaymentGateway codGateway;

    @Inject
    private VnPayPaymentGateway vnpayGateway;

    @jakarta.ejb.EJB
    private com.agrishop.service.AuditLogServiceLocal auditLogService;

    private PaymentGateway resolveGateway(String gatewayCode) {
        if ("VNPAY".equalsIgnoreCase(gatewayCode)) {
            return vnpayGateway;
        }
        return codGateway;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public PaymentResponse initPayment(Long orderId, String gatewayCode, String returnUrl) throws Exception {
        Orders order = em.find(Orders.class, orderId);
        if (order == null) {
            throw new Exception("Không tìm thấy đơn hàng cần thanh toán.");
        }

        String oldMethod = order.getPaymentMethod();
        PaymentGateway gateway = resolveGateway(gatewayCode);
        order.setPaymentMethod(gateway.getGatewayCode());
        order.setUpdatedAt(new Date());
        em.merge(order);

        if (auditLogService != null) {
            Long uId = order.getUser() != null ? order.getUser().getId() : null;
            auditLogService.log(uId, "UPDATE_PAYMENT_METHOD", "Orders", order.getId(),
                "payment_method=" + oldMethod, "payment_method=" + gateway.getGatewayCode());
        }

        return gateway.createPayment(order, returnUrl);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public PaymentCallbackResult processCallback(String gatewayCode, Map<String, String> params) throws Exception {
        PaymentGateway gateway = resolveGateway(gatewayCode);
        PaymentCallbackResult result = gateway.verifyCallback(params);

        if (result != null && result.getOrderCode() != null) {
            List<Orders> list = em.createQuery("SELECT o FROM Orders o WHERE o.orderCode = :code", Orders.class)
                                  .setParameter("code", result.getOrderCode())
                                  .getResultList();
            if (!list.isEmpty()) {
                Orders order = list.get(0);
                String oldStatus = order.getPaymentStatus();
                if (result.isSuccess()) {
                    order.setPaymentStatus("PAID");
                } else {
                    // Nếu thất bại nhưng chưa thanh toán thì đánh dấu PAYMENT_FAILED
                    if (!"PAID".equals(order.getPaymentStatus())) {
                        order.setPaymentStatus("PAYMENT_FAILED");
                    }
                }
                order.setUpdatedAt(new Date());
                em.merge(order);

                if (auditLogService != null) {
                    Long uId = order.getUser() != null ? order.getUser().getId() : null;
                    auditLogService.log(uId, "PAYMENT_CALLBACK_UPDATE", "Orders", order.getId(),
                        "payment_status=" + oldStatus, "payment_status=" + order.getPaymentStatus() + ", txn=" + result.getTransactionRef());
                }
            }
        }

        return result;
    }
}
