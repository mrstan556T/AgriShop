package com.agrishop.web.bean;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import org.primefaces.model.LazyDataModel;
import org.primefaces.PrimeFaces;

public abstract class AbstractCrudBean<T> implements Serializable {

    protected LazyDataModel<T> lazyModel;
    protected T currentItem;
    protected boolean editMode;
    protected boolean viewMode;
    protected String globalFilter;

    protected abstract void initItem();
    protected abstract void performSave() throws Exception;
    protected abstract void performDelete(T item) throws Exception;
    protected abstract String getItemName();

    public void openNew() {
        initItem();
        this.editMode = false;
        this.viewMode = false;
    }

    public void prepareEdit(T item) {
        this.currentItem = item;
        this.editMode = true;
        this.viewMode = false;
    }
    
    public void prepareView(T item) {
        this.currentItem = item;
        this.editMode = false;
        this.viewMode = true;
    }

    public void save() {
        try {
            performSave();
            addMessage(FacesMessage.SEVERITY_INFO, "Thành công", 
                (editMode ? "Cập nhật " : "Thêm mới ") + getItemName() + " thành công!");
            
            // Đóng dialog phía UI (ví dụ popup có widgetVar='crudDialog')
            PrimeFaces.current().executeScript("PF('crudDialog').hide();");
            PrimeFaces.current().ajax().update("crudForm:messages", "mainForm:dataTable");
        } catch (com.agrishop.exception.BusinessException e) {
            addMessage(FacesMessage.SEVERITY_WARN, "Cảnh báo", e.getMessage());
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Lỗi Hệ Thống", e.getMessage());
        }
    }

    public void delete(T item) {
        try {
            performDelete(item);
            addMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Xóa " + getItemName() + " thành công!");
            this.currentItem = null;
        } catch (com.agrishop.exception.BusinessException e) {
            addMessage(FacesMessage.SEVERITY_WARN, "Lỗi nghiệp vụ", e.getMessage());
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Lỗi Hệ Thống", "Không thể xóa: " + e.getMessage());
        }
    }

    protected void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    public LazyDataModel<T> getLazyModel() { return lazyModel; }
    public void setLazyModel(LazyDataModel<T> lazyModel) { this.lazyModel = lazyModel; }
    public T getCurrentItem() { return currentItem; }
    public void setCurrentItem(T currentItem) { this.currentItem = currentItem; }
    public boolean isEditMode() { return editMode; }
    public void setEditMode(boolean editMode) { this.editMode = editMode; }
    public boolean isViewMode() { return viewMode; }
    public void setViewMode(boolean viewMode) { this.viewMode = viewMode; }
    public String getGlobalFilter() { return globalFilter; }
    public void setGlobalFilter(String globalFilter) { this.globalFilter = globalFilter; }
}
