package com.agrishop.service;

import com.agrishop.dto.OrderDTO;
import com.agrishop.dto.UserDTO;
import com.agrishop.entity.Orders;
import com.agrishop.entity.User;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Stateless
public class CustomerService implements CustomerServiceLocal {

    @PersistenceContext(unitName = "AgriShopPU")
    private EntityManager em;

    @Override
    public List<UserDTO> getCustomersLazy(int first, int pageSize, String sortField, String sortOrder, Map<String, Object> filters) {
        StringBuilder jpql = new StringBuilder("SELECT u FROM User u WHERE u.role = 'CUSTOMER'");
        applyFilters(jpql, filters);

        if (sortField != null && !sortField.isEmpty()) {
            String dir = "DESC".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";
            jpql.append(" ORDER BY u.").append(sortField).append(" ").append(dir);
        } else {
            jpql.append(" ORDER BY u.id DESC");
        }

        TypedQuery<User> query = em.createQuery(jpql.toString(), User.class);
        setFilterParameters(query, filters);
        query.setFirstResult(first);
        query.setMaxResults(pageSize);

        List<User> users = query.getResultList();
        List<UserDTO> dtoList = new ArrayList<>();

        for (User u : users) {
            UserDTO dto = toDTO(u);
            populateCustomerStats(dto);
            dtoList.add(dto);
        }
        return dtoList;
    }

    @Override
    public int countCustomers(Map<String, Object> filters) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(u) FROM User u WHERE u.role = 'CUSTOMER'");
        applyFilters(jpql, filters);

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
        setFilterParameters(query, filters);
        Long count = query.getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public UserDTO getCustomerById(Long customerId) {
        User u = em.find(User.class, customerId);
        if (u != null && "CUSTOMER".equalsIgnoreCase(u.getRole())) {
            UserDTO dto = toDTO(u);
            populateCustomerStats(dto);
            return dto;
        }
        return null;
    }

    @Override
    public List<OrderDTO> getCustomerOrders(Long customerId) {
        List<Orders> orders = em.createQuery("SELECT o FROM Orders o WHERE o.user.id = :cId ORDER BY o.id DESC", Orders.class)
                                .setParameter("cId", customerId)
                                .getResultList();
        List<OrderDTO> list = new ArrayList<>();
        for (Orders o : orders) {
            OrderDTO dto = new OrderDTO();
            dto.setId(o.getId());
            dto.setOrderCode(o.getOrderCode());
            dto.setTotalAmount(o.getTotalAmount());
            dto.setShippingAddress(o.getShippingAddress());
            dto.setPhone(o.getPhone());
            dto.setStatus(o.getStatus());
            dto.setCreatedAt(o.getCreatedAt());
            list.add(dto);
        }
        return list;
    }

    @Override
    public void toggleCustomerStatus(Long customerId) throws Exception {
        User u = em.find(User.class, customerId);
        if (u == null) throw new Exception("Không tìm thấy thông tin khách hàng.");
        if ("ACTIVE".equalsIgnoreCase(u.getStatus())) {
            u.setStatus("INACTIVE");
        } else {
            u.setStatus("ACTIVE");
        }
        em.merge(u);
    }

    private void populateCustomerStats(UserDTO dto) {
        try {
            Object[] stats = (Object[]) em.createQuery(
                "SELECT COUNT(o), SUM(CASE WHEN o.status != 'CANCELLED' THEN o.totalAmount ELSE 0 END) " +
                "FROM Orders o WHERE o.user.id = :uId")
                .setParameter("uId", dto.getId())
                .getSingleResult();
            Long orderCount = stats != null && stats[0] != null ? (Long) stats[0] : 0L;
            BigDecimal totalSpent = stats != null && stats[1] != null ? (BigDecimal) stats[1] : BigDecimal.ZERO;
            dto.setTotalOrders(orderCount.intValue());
            dto.setTotalSpent(totalSpent);
        } catch (Exception e) {
            dto.setTotalOrders(0);
            dto.setTotalSpent(BigDecimal.ZERO);
        }
    }

    private UserDTO toDTO(User u) {
        UserDTO dto = new UserDTO();
        dto.setId(u.getId());
        dto.setUserCode(u.getUserCode());
        dto.setUsername(u.getUsername());
        dto.setFullName(u.getFullName());
        dto.setEmail(u.getEmail());
        dto.setPhone(u.getPhone());
        dto.setAddress(u.getAddress());
        dto.setAvatarUrl(u.getAvatarUrl());
        dto.setRole(u.getRole());
        dto.setStatus(u.getStatus());
        dto.setCreatedAt(u.getCreatedAt());
        return dto;
    }

    private void applyFilters(StringBuilder jpql, Map<String, Object> filters) {
        if (filters != null) {
            if (filters.containsKey("globalFilter") && filters.get("globalFilter") != null) {
                jpql.append(" AND (LOWER(u.username) LIKE :globalFilter OR LOWER(u.fullName) LIKE :globalFilter OR LOWER(u.email) LIKE :globalFilter OR LOWER(u.phone) LIKE :globalFilter OR LOWER(u.userCode) LIKE :globalFilter)");
            }
            if (filters.containsKey("status") && filters.get("status") != null && !filters.get("status").toString().isEmpty()) {
                jpql.append(" AND u.status = :status");
            }
        }
    }

    private void setFilterParameters(TypedQuery<?> query, Map<String, Object> filters) {
        if (filters != null) {
            if (filters.containsKey("globalFilter") && filters.get("globalFilter") != null) {
                query.setParameter("globalFilter", "%" + filters.get("globalFilter").toString().trim().toLowerCase() + "%");
            }
            if (filters.containsKey("status") && filters.get("status") != null && !filters.get("status").toString().isEmpty()) {
                query.setParameter("status", filters.get("status").toString());
            }
        }
    }
}
