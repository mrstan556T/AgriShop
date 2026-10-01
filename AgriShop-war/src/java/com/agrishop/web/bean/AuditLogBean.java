package com.agrishop.web.bean;

import com.agrishop.dto.AuditLogDTO;
import com.agrishop.service.AuditLogServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;

@Named("auditLogBean")
@ViewScoped
public class AuditLogBean implements Serializable {

    @EJB
    private AuditLogServiceLocal auditLogService;

    private LazyDataModel<AuditLogDTO> lazyModel;
    private String globalFilter;
    private String actionFilter;
    private String entityFilter;

    @PostConstruct
    public void init() {
        lazyModel = new LazyDataModel<AuditLogDTO>() {
            @Override
            public int count(Map<String, FilterMeta> filterBy) {
                Map<String, Object> filters = buildFilters();
                return auditLogService.countAuditLogs(filters);
            }

            @Override
            public List<AuditLogDTO> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
                Map<String, Object> filters = buildFilters();
                String sortField = null;
                String sortOrder = "DESC";

                if (sortBy != null && !sortBy.isEmpty()) {
                    SortMeta sm = sortBy.values().iterator().next();
                    sortField = sm.getField();
                    sortOrder = sm.getOrder().isAscending() ? "ASC" : "DESC";
                }

                List<AuditLogDTO> list = auditLogService.getAuditLogsLazy(first, pageSize, sortField, sortOrder, filters);
                setRowCount(auditLogService.countAuditLogs(filters));
                return list;
            }
        };
    }

    private Map<String, Object> buildFilters() {
        Map<String, Object> filters = new HashMap<>();
        if (globalFilter != null && !globalFilter.trim().isEmpty()) {
            filters.put("globalFilter", globalFilter.trim());
        }
        if (actionFilter != null && !actionFilter.trim().isEmpty()) {
            filters.put("action", actionFilter.trim());
        }
        if (entityFilter != null && !entityFilter.trim().isEmpty()) {
            filters.put("entityName", entityFilter.trim());
        }
        return filters;
    }

    public void resetFilters() {
        this.globalFilter = null;
        this.actionFilter = null;
        this.entityFilter = null;
    }

    // Getters & Setters
    public LazyDataModel<AuditLogDTO> getLazyModel() { return lazyModel; }
    public String getGlobalFilter() { return globalFilter; }
    public void setGlobalFilter(String globalFilter) { this.globalFilter = globalFilter; }
    public String getActionFilter() { return actionFilter; }
    public void setActionFilter(String actionFilter) { this.actionFilter = actionFilter; }
    public String getEntityFilter() { return entityFilter; }
    public void setEntityFilter(String entityFilter) { this.entityFilter = entityFilter; }
}
