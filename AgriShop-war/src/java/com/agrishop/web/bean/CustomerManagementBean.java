package com.agrishop.web.bean;

import com.agrishop.dto.OrderDTO;
import com.agrishop.dto.UserDTO;
import com.agrishop.service.CustomerServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;

@Named("customerManagementBean")
@ViewScoped
public class CustomerManagementBean implements Serializable {

    @EJB
    private CustomerServiceLocal customerService;

    private LazyDataModel<UserDTO> lazyModel;
    private String globalFilter;
    private String statusFilter;

    private UserDTO selectedCustomer;
    private List<OrderDTO> customerOrders;

    @PostConstruct
    public void init() {
        lazyModel = new LazyDataModel<UserDTO>() {
            @Override
            public int count(Map<String, FilterMeta> filterBy) {
                Map<String, Object> filters = buildFilters();
                return customerService.countCustomers(filters);
            }

            @Override
            public List<UserDTO> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
                Map<String, Object> filters = buildFilters();
                String sortField = null;
                String sortOrder = "DESC";

                if (sortBy != null && !sortBy.isEmpty()) {
                    SortMeta sm = sortBy.values().iterator().next();
                    sortField = sm.getField();
                    sortOrder = sm.getOrder().isAscending() ? "ASC" : "DESC";
                }

                List<UserDTO> list = customerService.getCustomersLazy(first, pageSize, sortField, sortOrder, filters);
                setRowCount(customerService.countCustomers(filters));
                return list;
            }
        };
    }

    private Map<String, Object> buildFilters() {
        Map<String, Object> filters = new HashMap<>();
        if (globalFilter != null && !globalFilter.trim().isEmpty()) {
            filters.put("globalFilter", globalFilter.trim());
        }
        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            filters.put("status", statusFilter.trim());
        }
        return filters;
    }

    public void viewCustomerDetails(Long customerId) {
        if (customerId != null) {
            this.selectedCustomer = customerService.getCustomerById(customerId);
            this.customerOrders = customerService.getCustomerOrders(customerId);
        }
    }

    public void toggleCustomerStatus(Long customerId) {
        try {
            customerService.toggleCustomerStatus(customerId);
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đã cập nhật trạng thái khách hàng."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", e.getMessage()));
        }
    }

    public void resetFilters() {
        this.globalFilter = null;
        this.statusFilter = null;
    }

    // Getters & Setters
    public LazyDataModel<UserDTO> getLazyModel() { return lazyModel; }
    public String getGlobalFilter() { return globalFilter; }
    public void setGlobalFilter(String globalFilter) { this.globalFilter = globalFilter; }
    public String getStatusFilter() { return statusFilter; }
    public void setStatusFilter(String statusFilter) { this.statusFilter = statusFilter; }
    public UserDTO getSelectedCustomer() { return selectedCustomer; }
    public void setSelectedCustomer(UserDTO selectedCustomer) { this.selectedCustomer = selectedCustomer; }
    public List<OrderDTO> getCustomerOrders() { return customerOrders; }
}
