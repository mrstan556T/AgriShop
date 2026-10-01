package com.agrishop.web.bean;

import com.agrishop.dto.UserDTO;
import com.agrishop.service.UserServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named("userManagementBean")
@ViewScoped
public class UserManagementBean implements Serializable {

    @EJB
    private UserServiceLocal userService;

    @Inject
    private LoginBean loginBean;

    private LazyDataModel<UserDTO> lazyModel;

    // Toolbar Filters
    private String usernameFilter;
    private String roleFilter;
    private String statusFilter;

    @PostConstruct
    public void init() {
        lazyModel = new LazyDataModel<UserDTO>() {
            @Override
            public int count(Map<String, FilterMeta> filterBy) {
                Map<String, Object> filters = new HashMap<>();
                for (Map.Entry<String, FilterMeta> entry : filterBy.entrySet()) {
                    filters.put(entry.getKey(), entry.getValue().getFilterValue());
                }
                if (usernameFilter != null && !usernameFilter.trim().isEmpty()) {
                    filters.put("username", usernameFilter.trim());
                }
                if (roleFilter != null && !roleFilter.trim().isEmpty()) {
                    filters.put("role", roleFilter.trim());
                }
                if (statusFilter != null && !statusFilter.trim().isEmpty()) {
                    filters.put("status", statusFilter.trim());
                }
                return userService.countUsers(filters);
            }

            @Override
            public List<UserDTO> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
                Map<String, Object> filters = new HashMap<>();
                for (Map.Entry<String, FilterMeta> entry : filterBy.entrySet()) {
                    filters.put(entry.getKey(), entry.getValue().getFilterValue());
                }
                if (usernameFilter != null && !usernameFilter.trim().isEmpty()) {
                    filters.put("username", usernameFilter.trim());
                }
                if (roleFilter != null && !roleFilter.trim().isEmpty()) {
                    filters.put("role", roleFilter.trim());
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
                
                return userService.getUsersLazy(first, pageSize, sortField, sortOrderStr, filters);
            }
        };
    }

    public void resetFilters() {
        this.usernameFilter = null;
        this.roleFilter = null;
        this.statusFilter = null;
    }

    public void toggleStatus(UserDTO user) {
        if (loginBean.getCurrentUser() != null && loginBean.getCurrentUser().getId().equals(user.getId())) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Lỗi", "Không thể tự khóa tài khoản của chính mình."));
            return;
        }
        try {
            userService.toggleUserStatus(user.getId());
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đã cập nhật trạng thái tài khoản."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", e.getMessage()));
        }
    }

    public void changeRole(UserDTO user) {
        if (loginBean.getCurrentUser() != null && loginBean.getCurrentUser().getId().equals(user.getId())) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Lỗi", "Không thể tự thay đổi quyền của chính mình."));
            return;
        }
        try {
            String newRole = "ADMIN".equals(user.getRole()) ? "CUSTOMER" : "ADMIN";
            userService.updateUserRole(user.getId(), newRole);
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đã phân quyền tài khoản thành " + newRole + "."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", e.getMessage()));
        }
    }

    public LazyDataModel<UserDTO> getLazyModel() { return lazyModel; }
    public String getUsernameFilter() { return usernameFilter; }
    public void setUsernameFilter(String usernameFilter) { this.usernameFilter = usernameFilter; }
    public String getRoleFilter() { return roleFilter; }
    public void setRoleFilter(String roleFilter) { this.roleFilter = roleFilter; }
    public String getStatusFilter() { return statusFilter; }
    public void setStatusFilter(String statusFilter) { this.statusFilter = statusFilter; }
}
