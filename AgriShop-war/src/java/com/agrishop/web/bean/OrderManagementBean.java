package com.agrishop.web.bean;

import com.agrishop.dto.CancellationRequestDTO;
import com.agrishop.dto.OrderDTO;
import com.agrishop.service.OrderManagementServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named("orderManagementBean")
@ViewScoped
public class OrderManagementBean implements Serializable {

    @EJB
    private OrderManagementServiceLocal orderService;

    private LazyDataModel<OrderDTO> lazyModel;
    
    // Toolbar Filters
    private String orderCodeFilter;
    private String statusFilter;
    
    // For Dialog
    private OrderDTO selectedOrder;
    private String newStatus;

    // For Cancellation Requests & Refunds
    private List<CancellationRequestDTO> pendingCancellationRequests = new ArrayList<>();
    private String cancellationAdminNote;
    private List<OrderDTO> refundOrders = new ArrayList<>();
    private String refundAdminNote;
    private int activeTabIndex = 0;

    @PostConstruct
    public void init() {
        loadCancellationRequests();
        loadRefundOrders();
        lazyModel = new LazyDataModel<OrderDTO>() {
            @Override
            public int count(Map<String, FilterMeta> filterBy) {
                Map<String, Object> filters = new HashMap<>();
                for (Map.Entry<String, FilterMeta> entry : filterBy.entrySet()) {
                    filters.put(entry.getKey(), entry.getValue().getFilterValue());
                }
                if (orderCodeFilter != null && !orderCodeFilter.trim().isEmpty()) {
                    filters.put("orderCode", orderCodeFilter.trim());
                }
                if (statusFilter != null && !statusFilter.trim().isEmpty()) {
                    filters.put("status", statusFilter.trim());
                }
                return orderService.countOrders(filters);
            }

            @Override
            public List<OrderDTO> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
                Map<String, Object> filters = new HashMap<>();
                for (Map.Entry<String, FilterMeta> entry : filterBy.entrySet()) {
                    filters.put(entry.getKey(), entry.getValue().getFilterValue());
                }
                if (orderCodeFilter != null && !orderCodeFilter.trim().isEmpty()) {
                    filters.put("orderCode", orderCodeFilter.trim());
                }
                if (statusFilter != null && !statusFilter.trim().isEmpty()) {
                    filters.put("status", statusFilter.trim());
                }
                
                String sortField = null;
                String sortOrderStr = null;
                if (sortBy != null && !sortBy.isEmpty()) {
                    SortMeta sm = sortBy.values().iterator().next();
                    sortField = sm.getField();
                    sortOrderStr = sm.getOrder() == SortOrder.ASCENDING ? "ASC" : "DESC";
                }
                
                return orderService.getOrdersLazy(first, pageSize, sortField, sortOrderStr, filters);
            }
        };
    }

    public void resetFilters() {
        this.orderCodeFilter = null;
        this.statusFilter = null;
    }

    public void viewOrderDetails(Long orderId) {
        this.selectedOrder = orderService.getOrderDetails(orderId);
        this.newStatus = this.selectedOrder.getStatus(); // Khởi tạo status mặc định cho dropdown
    }

    public void updateStatus() {
        if (selectedOrder == null) return;
        
        try {
            orderService.updateOrderStatus(selectedOrder.getId(), newStatus);
            this.selectedOrder = orderService.getOrderDetails(selectedOrder.getId());
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đã cập nhật trạng thái đơn hàng thành: " + newStatus));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", e.getMessage()));
        }
    }

    public void processReturn(boolean restockToInventory) {
        if (selectedOrder == null) return;
        try {
            orderService.processReturn(selectedOrder.getId(), restockToInventory, 
                restockToInventory ? "Khách hoàn hàng (còn tốt, nhập lại kho)" : "Khách hoàn hàng (dập/hỏng, hủy bỏ)");
            this.selectedOrder = orderService.getOrderDetails(selectedOrder.getId());
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đã xử lý hoàn trả đơn hàng và cập nhật trạng thái REFUNDED."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi hoàn đơn", e.getMessage()));
        }
    }

    public void loadCancellationRequests() {
        this.pendingCancellationRequests = orderService.getPendingCancellationRequests();
    }

    public void approveCancellation(Long requestId) {
        try {
            orderService.approveCancellationRequest(requestId, cancellationAdminNote);
            loadCancellationRequests();
            this.cancellationAdminNote = null;
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đã chấp thuận yêu cầu hủy đơn hàng. Tồn kho và voucher đã được hoàn trả."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi duyệt hủy", e.getMessage()));
        }
    }

    public void rejectCancellation(Long requestId) {
        try {
            orderService.rejectCancellationRequest(requestId, cancellationAdminNote);
            loadCancellationRequests();
            this.cancellationAdminNote = null;
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đã từ chối yêu cầu hủy đơn hàng."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi từ chối", e.getMessage()));
        }
    }

    public void loadRefundOrders() {
        this.refundOrders = orderService.getRefundOrders();
    }

    public void confirmRefund(Long orderId) {
        try {
            orderService.confirmRefund(orderId, refundAdminNote);
            loadRefundOrders();
            this.refundAdminNote = null;
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đã xác nhận hoàn tiền thành công cho khách hàng (REFUNDED)."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi hoàn tiền", e.getMessage()));
        }
    }

    public void onTabChange(TabChangeEvent event) {
        loadCancellationRequests();
        loadRefundOrders();
    }

    public int getPendingCancellationCount() {
        return pendingCancellationRequests != null ? pendingCancellationRequests.size() : 0;
    }

    public int getRefundPendingCount() {
        if (refundOrders == null) return 0;
        int count = 0;
        for (OrderDTO o : refundOrders) {
            if ("REFUND_PENDING".equalsIgnoreCase(o.getPaymentStatus())) {
                count++;
            }
        }
        return count;
    }

    public LazyDataModel<OrderDTO> getLazyModel() { return lazyModel; }
    public OrderDTO getSelectedOrder() { return selectedOrder; }
    public void setSelectedOrder(OrderDTO selectedOrder) { this.selectedOrder = selectedOrder; }
    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    public String getOrderCodeFilter() { return orderCodeFilter; }
    public void setOrderCodeFilter(String orderCodeFilter) { this.orderCodeFilter = orderCodeFilter; }
    public String getStatusFilter() { return statusFilter; }
    public void setStatusFilter(String statusFilter) { this.statusFilter = statusFilter; }
    public List<CancellationRequestDTO> getPendingCancellationRequests() { return pendingCancellationRequests; }
    public void setPendingCancellationRequests(List<CancellationRequestDTO> pendingCancellationRequests) { this.pendingCancellationRequests = pendingCancellationRequests; }
    public String getCancellationAdminNote() { return cancellationAdminNote; }
    public void setCancellationAdminNote(String cancellationAdminNote) { this.cancellationAdminNote = cancellationAdminNote; }
    public List<OrderDTO> getRefundOrders() { return refundOrders; }
    public void setRefundOrders(List<OrderDTO> refundOrders) { this.refundOrders = refundOrders; }
    public String getRefundAdminNote() { return refundAdminNote; }
    public void setRefundAdminNote(String refundAdminNote) { this.refundAdminNote = refundAdminNote; }
    public int getActiveTabIndex() { return activeTabIndex; }
    public void setActiveTabIndex(int activeTabIndex) { this.activeTabIndex = activeTabIndex; }
}
