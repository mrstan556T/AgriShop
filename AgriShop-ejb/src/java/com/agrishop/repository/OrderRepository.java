package com.agrishop.repository;

import com.agrishop.entity.Orders;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

@Stateless
public class OrderRepository implements OrderRepositoryLocal {

    @PersistenceContext(unitName = "AgriShopPU")
    private EntityManager em;

    @Override
    public void createOrder(Orders order) {
        order.setCreatedAt(new Date());
        order.setUpdatedAt(new Date());
        em.persist(order);
    }

    @Override
    public String generateOrderCode() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String prefix = sdf.format(new Date());
        int randomNum = new Random().nextInt(900) + 100; // Random 3 digits
        return "ORD-" + prefix + "-" + randomNum;
    }

    @Override
    public java.util.List<Orders> getOrdersLazy(int first, int pageSize, String sortField, String sortOrder, java.util.Map<String, Object> filters) {
        StringBuilder queryStr = new StringBuilder("SELECT o FROM Orders o LEFT JOIN FETCH o.user WHERE 1=1 ");
        
        if (filters != null) {
            for (java.util.Map.Entry<String, Object> filter : filters.entrySet()) {
                if (filter.getValue() != null && !filter.getValue().toString().isEmpty()) {
                    if (filter.getKey().equals("status")) {
                        queryStr.append(" AND o.status = :").append(filter.getKey());
                    } else if (filter.getKey().equals("orderCode")) {
                        queryStr.append(" AND LOWER(o.orderCode) LIKE LOWER(:").append(filter.getKey()).append(")");
                    }
                }
            }
        }
        
        if (sortField != null && !sortField.isEmpty()) {
            queryStr.append(" ORDER BY o.").append(sortField).append(" ").append(sortOrder);
        } else {
            queryStr.append(" ORDER BY o.createdAt DESC"); // Default sort
        }
        
        jakarta.persistence.TypedQuery<Orders> query = em.createQuery(queryStr.toString(), Orders.class);
        
        if (filters != null) {
            for (java.util.Map.Entry<String, Object> filter : filters.entrySet()) {
                if (filter.getValue() != null && !filter.getValue().toString().isEmpty()) {
                    if (filter.getKey().equals("status")) {
                        query.setParameter(filter.getKey(), filter.getValue());
                    } else if (filter.getKey().equals("orderCode")) {
                        query.setParameter(filter.getKey(), "%" + filter.getValue() + "%");
                    }
                }
            }
        }
        
        query.setFirstResult(first);
        query.setMaxResults(pageSize);
        return query.getResultList();
    }

    @Override
    public int countOrders(java.util.Map<String, Object> filters) {
        StringBuilder queryStr = new StringBuilder("SELECT COUNT(o) FROM Orders o WHERE 1=1 ");
        
        if (filters != null) {
            for (java.util.Map.Entry<String, Object> filter : filters.entrySet()) {
                if (filter.getValue() != null && !filter.getValue().toString().isEmpty()) {
                    if (filter.getKey().equals("status")) {
                        queryStr.append(" AND o.status = :").append(filter.getKey());
                    } else if (filter.getKey().equals("orderCode")) {
                        queryStr.append(" AND LOWER(o.orderCode) LIKE LOWER(:").append(filter.getKey()).append(")");
                    }
                }
            }
        }
        
        jakarta.persistence.TypedQuery<Long> query = em.createQuery(queryStr.toString(), Long.class);
        
        if (filters != null) {
            for (java.util.Map.Entry<String, Object> filter : filters.entrySet()) {
                if (filter.getValue() != null && !filter.getValue().toString().isEmpty()) {
                    if (filter.getKey().equals("status")) {
                        query.setParameter(filter.getKey(), filter.getValue());
                    } else if (filter.getKey().equals("orderCode")) {
                        query.setParameter(filter.getKey(), "%" + filter.getValue() + "%");
                    }
                }
            }
        }
        
        return query.getSingleResult().intValue();
    }

    @Override
    public Orders findByIdWithDetails(Long orderId) {
        try {
            java.util.List<Orders> list = em.createQuery(
                "SELECT DISTINCT o FROM Orders o LEFT JOIN FETCH o.orderDetails od LEFT JOIN FETCH od.product p LEFT JOIN FETCH o.user u WHERE o.id = :id", Orders.class)
                .setParameter("id", orderId)
                .getResultList();
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void updateOrderStatus(Long orderId, String newStatus) {
        Orders order = em.find(Orders.class, orderId);
        if (order != null) {
            order.setStatus(newStatus);
            order.setUpdatedAt(new Date());
            em.merge(order);
        }
    }

    @Override
    public java.util.List<Orders> getOrdersByUserId(Long userId) {
        return em.createQuery(
            "SELECT DISTINCT o FROM Orders o LEFT JOIN FETCH o.orderDetails od LEFT JOIN FETCH od.product p WHERE o.user.id = :userId ORDER BY o.createdAt DESC", Orders.class)
            .setParameter("userId", userId)
            .getResultList();
    }

    @Override
    public java.util.List<com.agrishop.entity.OrderDetail> getOrderDetailsByOrderIds(java.util.List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return em.createQuery(
            "SELECT od FROM OrderDetail od LEFT JOIN FETCH od.product p WHERE od.order.id IN :orderIds", com.agrishop.entity.OrderDetail.class)
            .setParameter("orderIds", orderIds)
            .getResultList();
    }
}
