package com.agrishop.web.bean;

import com.agrishop.dto.CouponDTO;
import com.agrishop.service.CouponServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;

@Named("couponBean")
@ViewScoped
public class CouponBean implements Serializable {

    @EJB
    private CouponServiceLocal couponService;

    private LazyDataModel<CouponDTO> lazyModel;
    private String globalFilter;
    private String statusFilter;

    private CouponDTO currentItem;
    private boolean editMode;

    @PostConstruct
    public void init() {
        openNew();
        lazyModel = new LazyDataModel<CouponDTO>() {
            @Override
            public int count(Map<String, FilterMeta> filterBy) {
                Map<String, Object> filters = buildFilters();
                return couponService.countCoupons(filters);
            }

            @Override
            public List<CouponDTO> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
                Map<String, Object> filters = buildFilters();
                String sortField = null;
                String sortOrder = "DESC";

                if (sortBy != null && !sortBy.isEmpty()) {
                    SortMeta sm = sortBy.values().iterator().next();
                    sortField = sm.getField();
                    sortOrder = sm.getOrder().isAscending() ? "ASC" : "DESC";
                }

                List<CouponDTO> list = couponService.getCouponsLazy(first, pageSize, sortField, sortOrder, filters);
                setRowCount(couponService.countCoupons(filters));
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

    public void openNew() {
        this.currentItem = new CouponDTO();
        this.currentItem.setDiscountType("PERCENTAGE");
        this.currentItem.setDiscountValue(new BigDecimal(10));
        this.currentItem.setMinOrderValue(BigDecimal.ZERO);
        this.currentItem.setStartDate(new Date());
        this.currentItem.setEndDate(new Date(System.currentTimeMillis() + 30L * 24 * 3600 * 1000));
        this.currentItem.setUsageLimit(100);
        this.editMode = false;
    }

    public void prepareEdit(CouponDTO item) {
        if (item != null) {
            this.currentItem = item;
            this.editMode = true;
        }
    }

    public void saveCoupon() {
        try {
            if (currentItem.getCode() == null || currentItem.getCode().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage("crudForm:code",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Vui lòng nhập mã giảm giá"));
                return;
            }
            if (currentItem.getDiscountValue() == null || currentItem.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
                FacesContext.getCurrentInstance().addMessage("crudForm:discountVal",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Giá trị giảm phải lớn hơn 0"));
                return;
            }
            if ("PERCENTAGE".equalsIgnoreCase(currentItem.getDiscountType()) && currentItem.getDiscountValue().compareTo(new BigDecimal(100)) > 0) {
                FacesContext.getCurrentInstance().addMessage("crudForm:discountVal",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Giảm theo phần trăm tối đa là 100%"));
                return;
            }
            if (currentItem.getEndDate() != null && currentItem.getStartDate() != null && currentItem.getEndDate().before(currentItem.getStartDate())) {
                FacesContext.getCurrentInstance().addMessage("crudForm:endDate",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Ngày kết thúc phải sau ngày bắt đầu"));
                return;
            }

            if (editMode) {
                couponService.updateCoupon(currentItem);
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đã cập nhật mã giảm giá: " + currentItem.getCode()));
            } else {
                couponService.createCoupon(currentItem);
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đã tạo mã giảm giá mới: " + currentItem.getCode()));
            }

            org.primefaces.PrimeFaces.current().executeScript("PF('crudDialog').hide();");
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi lưu mã", e.getMessage()));
        }
    }

    public void toggleStatus(Long couponId) {
        try {
            couponService.toggleCouponStatus(couponId);
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đã cập nhật trạng thái voucher."));
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
    public LazyDataModel<CouponDTO> getLazyModel() { return lazyModel; }
    public String getGlobalFilter() { return globalFilter; }
    public void setGlobalFilter(String globalFilter) { this.globalFilter = globalFilter; }
    public String getStatusFilter() { return statusFilter; }
    public void setStatusFilter(String statusFilter) { this.statusFilter = statusFilter; }
    public CouponDTO getCurrentItem() { return currentItem; }
    public void setCurrentItem(CouponDTO currentItem) { this.currentItem = currentItem; }
    public boolean isEditMode() { return editMode; }
    public void setEditMode(boolean editMode) { this.editMode = editMode; }
}
