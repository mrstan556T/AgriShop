package com.agrishop.repository;

import com.agrishop.entity.Orders;
import jakarta.ejb.Local;

@Local
public interface OrderRepositoryLocal {
    void createOrder(Orders order);
    String generateOrderCode();
    
    java.util.List<Orders> getOrdersLazy(int first, int pageSize, String sortField, String sortOrder, java.util.Map<String, Object> filters);
    int countOrders(java.util.Map<String, Object> filters);
    Orders findByIdWithDetails(Long orderId);
    void updateOrderStatus(Long orderId, String newStatus);
    java.util.List<Orders> getOrdersByUserId(Long userId);
    java.util.List<com.agrishop.entity.OrderDetail> getOrderDetailsByOrderIds(java.util.List<Long> orderIds);
}
