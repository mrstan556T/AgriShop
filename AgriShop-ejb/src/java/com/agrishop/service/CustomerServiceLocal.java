package com.agrishop.service;

import com.agrishop.dto.OrderDTO;
import com.agrishop.dto.UserDTO;
import jakarta.ejb.Local;
import java.util.List;
import java.util.Map;

@Local
public interface CustomerServiceLocal {
    List<UserDTO> getCustomersLazy(int first, int pageSize, String sortField, String sortOrder, Map<String, Object> filters);
    int countCustomers(Map<String, Object> filters);
    UserDTO getCustomerById(Long customerId);
    List<OrderDTO> getCustomerOrders(Long customerId);
    void toggleCustomerStatus(Long customerId) throws Exception;
}
