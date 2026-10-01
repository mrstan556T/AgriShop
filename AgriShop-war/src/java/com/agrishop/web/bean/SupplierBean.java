package com.agrishop.web.bean;

import com.agrishop.dto.SupplierDTO;
import com.agrishop.dto.PageRequestDTO;
import com.agrishop.dto.PageResponseDTO;
import com.agrishop.service.SupplierServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

@Named("supplierBean")
@ViewScoped
public class SupplierBean extends AbstractCrudBean<SupplierDTO> implements Serializable {

    @EJB
    private SupplierServiceLocal supplierService;

    @EJB
    private com.agrishop.service.DashboardServiceLocal dashboardService;

    private List<com.agrishop.dto.SupplierPerformanceDTO> performanceList;

    @PostConstruct
    public void init() {
        loadPerformanceData();
        lazyModel = new LazyDataModel<SupplierDTO>() {
            @Override
            public int count(Map<String, FilterMeta> filterBy) {
                return 0; // Not used directly in PF 12+, load() sets rowCount
            }

            @Override
            public List<SupplierDTO> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
                PageRequestDTO request = new PageRequestDTO();
                request.setPageIndex(first / pageSize);
                request.setPageSize(pageSize);
                request.setSearchKeyword(globalFilter);

                if (sortBy != null && !sortBy.isEmpty()) {
                    SortMeta sm = sortBy.values().iterator().next();
                    request.setSortField(sm.getField());
                    request.setSortOrder(sm.getOrder() == SortOrder.ASCENDING ? "ASC" : "DESC");
                }

                PageResponseDTO<SupplierDTO> response = supplierService.getSuppliersWithPagination(request);
                this.setRowCount((int) response.getTotalRecords());
                return response.getData();
            }
        };
    }

    @Override
    protected void initItem() {
        this.currentItem = new SupplierDTO();
    }

    @Override
    protected void performSave() throws Exception {
        if (currentItem.getSupplierCode() == null || currentItem.getSupplierCode().trim().isEmpty()) {
            throw new com.agrishop.exception.BusinessException("Mã nhà cung cấp không được để trống");
        }
        if (currentItem.getName() == null || currentItem.getName().trim().isEmpty()) {
            throw new com.agrishop.exception.BusinessException("Tên nhà cung cấp không được để trống");
        }
        if (currentItem.getPhone() != null && !currentItem.getPhone().trim().isEmpty() && !currentItem.getPhone().trim().matches("^0[0-9]{9,10}$")) {
            throw new com.agrishop.exception.BusinessException("Số điện thoại không hợp lệ (gồm 10-11 số, bắt đầu bằng 0)");
        }
        if (editMode) {
            supplierService.updateSupplier(currentItem);
        } else {
            supplierService.createSupplier(currentItem);
        }
    }

    @Override
    protected void performDelete(SupplierDTO item) {
        supplierService.deleteSupplier(item.getId());
    }

    @Override
    protected String getItemName() {
        return "Nhà cung cấp";
    }

    public void loadPerformanceData() {
        try {
            if (dashboardService != null) {
                this.performanceList = dashboardService.getSupplierPerformanceRanking();
            }
        } catch (Exception e) {
            this.performanceList = new java.util.ArrayList<>();
        }
    }

    public List<com.agrishop.dto.SupplierPerformanceDTO> getPerformanceList() {
        return performanceList;
    }
}
