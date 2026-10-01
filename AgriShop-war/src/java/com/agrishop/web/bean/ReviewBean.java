package com.agrishop.web.bean;

import com.agrishop.dto.ReviewDTO;
import com.agrishop.service.ReviewServiceLocal;
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

@Named("reviewBean")
@ViewScoped
public class ReviewBean implements Serializable {

    @EJB
    private ReviewServiceLocal reviewService;

    private LazyDataModel<ReviewDTO> lazyModel;
    private String globalFilter;
    private String statusFilter;
    private Integer ratingFilter;

    @PostConstruct
    public void init() {
        lazyModel = new LazyDataModel<ReviewDTO>() {
            @Override
            public int count(Map<String, FilterMeta> filterBy) {
                Map<String, Object> filters = buildFilters();
                return reviewService.countReviews(filters);
            }

            @Override
            public List<ReviewDTO> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
                Map<String, Object> filters = buildFilters();
                String sortField = null;
                String sortOrder = "DESC";

                if (sortBy != null && !sortBy.isEmpty()) {
                    SortMeta sm = sortBy.values().iterator().next();
                    sortField = sm.getField();
                    sortOrder = sm.getOrder().isAscending() ? "ASC" : "DESC";
                }

                List<ReviewDTO> list = reviewService.getReviewsLazy(first, pageSize, sortField, sortOrder, filters);
                setRowCount(reviewService.countReviews(filters));
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
        if (ratingFilter != null && ratingFilter > 0) {
            filters.put("rating", ratingFilter);
        }
        return filters;
    }

    public void toggleReviewStatus(Long reviewId) {
        try {
            reviewService.toggleReviewStatus(reviewId);
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đã cập nhật trạng thái kiểm duyệt đánh giá."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", e.getMessage()));
        }
    }

    public void resetFilters() {
        this.globalFilter = null;
        this.statusFilter = null;
        this.ratingFilter = null;
    }

    private ReviewDTO selectedReview;
    private String replyText;

    public void prepareReply(ReviewDTO r) {
        this.selectedReview = r;
        this.replyText = r != null ? r.getAdminReply() : "";
    }

    public void submitReply() {
        if (selectedReview == null) return;
        try {
            Long adminId = null;
            Object userObj = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("currentUser");
            if (userObj instanceof com.agrishop.dto.UserDTO) {
                adminId = ((com.agrishop.dto.UserDTO) userObj).getId();
            }

            reviewService.replyReview(selectedReview.getId(), replyText, adminId);
            selectedReview.setAdminReply(replyText);
            selectedReview.setAdminReplyAt(new java.util.Date());

            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đã gửi phản hồi đánh giá thành công."));
            org.primefaces.PrimeFaces.current().executeScript("PF('replyDialog').hide();");
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", e.getMessage()));
        }
    }

    // Getters & Setters
    public LazyDataModel<ReviewDTO> getLazyModel() { return lazyModel; }
    public String getGlobalFilter() { return globalFilter; }
    public void setGlobalFilter(String globalFilter) { this.globalFilter = globalFilter; }
    public String getStatusFilter() { return statusFilter; }
    public void setStatusFilter(String statusFilter) { this.statusFilter = statusFilter; }
    public Integer getRatingFilter() { return ratingFilter; }
    public void setRatingFilter(Integer ratingFilter) { this.ratingFilter = ratingFilter; }
    public ReviewDTO getSelectedReview() { return selectedReview; }
    public void setSelectedReview(ReviewDTO selectedReview) { this.selectedReview = selectedReview; }
    public String getReplyText() { return replyText; }
    public void setReplyText(String replyText) { this.replyText = replyText; }
}
