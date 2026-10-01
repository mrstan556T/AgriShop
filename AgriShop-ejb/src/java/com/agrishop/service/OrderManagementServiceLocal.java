package com.agrishop.service;

import com.agrishop.dto.OrderDTO;
import jakarta.ejb.Local;
import java.util.List;
import java.util.Map;

@Local
public interface OrderManagementServiceLocal {
    List<OrderDTO> getOrdersLazy(int first, int pageSize, String sortField, String sortOrder, Map<String, Object> filters);
    int countOrders(Map<String, Object> filters);
    OrderDTO getOrderDetails(Long orderId);
    void updateOrderStatus(Long orderId, String newStatus) throws Exception;
    void processReturn(Long orderId, boolean restockToInventory, String returnReason) throws Exception;
    List<OrderDTO> getCustomerOrderHistory(Long userId);
    void cancelOrderByCustomer(Long orderId, Long userId) throws Exception;
    void requestOrderCancellation(Long orderId, Long userId, String reason) throws Exception;
    List<com.agrishop.dto.CancellationRequestDTO> getPendingCancellationRequests();
    void approveCancellationRequest(Long requestId, String adminNote) throws Exception;
    void rejectCancellationRequest(Long requestId, String adminNote) throws Exception;
    void confirmRefund(Long orderId, String adminNote) throws Exception;
    List<OrderDTO> getRefundOrders();
    /** SePay webhook: xác nhận thanh toán theo mã đơn hàng trong nội dung CK */
    boolean confirmPaymentByOrderCode(String orderCode) throws Exception;
    /** Checkout polling: trả về payment_status của đơn hàng */
    String getPaymentStatusByOrderCode(String orderCode);
}
