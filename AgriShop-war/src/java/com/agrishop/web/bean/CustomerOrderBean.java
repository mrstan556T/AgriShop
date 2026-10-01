package com.agrishop.web.bean;

import com.agrishop.dto.OrderDTO;
import com.agrishop.dto.UserDTO;
import com.agrishop.service.OrderManagementServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("customerOrderBean")
@ViewScoped
public class CustomerOrderBean implements Serializable {

    @EJB
    private OrderManagementServiceLocal orderService;

    @Inject
    private LoginBean loginBean;

    private List<OrderDTO> myOrders;
    private OrderDTO selectedOrder;
    private OrderDTO selectedOrderForCancel;
    private String cancelReason;
    private String statusFilter = "ALL";

    @PostConstruct
    public void init() {
        loadMyOrders();
    }

    public void loadMyOrders() {
        UserDTO currentUser = loginBean.getCurrentUser();
        if (currentUser != null) {
            myOrders = orderService.getCustomerOrderHistory(currentUser.getId());
        }
    }

    public void filterOrders(String status) {
        this.statusFilter = status;
    }

    public List<OrderDTO> getFilteredOrders() {
        if (myOrders == null) return null;
        if ("ALL".equals(statusFilter)) return myOrders;
        return myOrders.stream()
                .filter(o -> o.getStatus().equals(statusFilter))
                .collect(java.util.stream.Collectors.toList());
    }

    public void viewOrderDetails(OrderDTO order) {
        this.selectedOrder = order;
    }

    public void cancelOrder(Long orderId) {
        UserDTO currentUser = loginBean.getCurrentUser();
        if (currentUser == null) return;
        try {
            orderService.cancelOrderByCustomer(orderId, currentUser.getId());
            loadMyOrders();
            jakarta.faces.application.FacesMessage msg = new jakarta.faces.application.FacesMessage(
                jakarta.faces.application.FacesMessage.SEVERITY_INFO, "Thành công", "Đã hủy đơn hàng thành công. Nông sản đang giữ đã được hoàn lại kho.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } catch (Exception e) {
            jakarta.faces.application.FacesMessage msg = new jakarta.faces.application.FacesMessage(
                jakarta.faces.application.FacesMessage.SEVERITY_ERROR, "Lỗi hủy đơn", e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    public void prepareRequestCancel(OrderDTO order) {
        this.selectedOrderForCancel = order;
        this.cancelReason = "";
    }

    public void submitCancellationRequest() {
        UserDTO currentUser = loginBean.getCurrentUser();
        if (currentUser == null || selectedOrderForCancel == null) return;
        try {
            orderService.requestOrderCancellation(selectedOrderForCancel.getId(), currentUser.getId(), cancelReason);
            loadMyOrders();
            jakarta.faces.application.FacesMessage msg = new jakarta.faces.application.FacesMessage(
                jakarta.faces.application.FacesMessage.SEVERITY_INFO, "Đã gửi yêu cầu", "Yêu cầu hủy đơn đã được gửi tới Ban quản trị để xem xét.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } catch (Exception e) {
            jakarta.faces.application.FacesMessage msg = new jakarta.faces.application.FacesMessage(
                jakarta.faces.application.FacesMessage.SEVERITY_ERROR, "Lỗi gửi yêu cầu", e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    public List<OrderDTO> getMyOrders() { return myOrders; }
    public OrderDTO getSelectedOrder() { return selectedOrder; }
    public void setSelectedOrder(OrderDTO selectedOrder) { this.selectedOrder = selectedOrder; }
    public OrderDTO getSelectedOrderForCancel() { return selectedOrderForCancel; }
    public void setSelectedOrderForCancel(OrderDTO selectedOrderForCancel) { this.selectedOrderForCancel = selectedOrderForCancel; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public String getStatusFilter() { return statusFilter; }
    public void setStatusFilter(String statusFilter) { this.statusFilter = statusFilter; }
}
