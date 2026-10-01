package com.agrishop.web.bean;

import com.agrishop.dto.InventoryTransactionDTO;
import com.agrishop.dto.ProductDTO;
import com.agrishop.dto.SupplierDTO;
import com.agrishop.service.InventoryServiceLocal;
import com.agrishop.service.ProductServiceLocal;
import com.agrishop.service.SupplierServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Named("inventoryBean")
@ViewScoped
public class InventoryBean implements Serializable {

    @EJB
    private InventoryServiceLocal inventoryService;

    @EJB
    private ProductServiceLocal productService;

    @EJB
    private SupplierServiceLocal supplierService;

    @Inject
    private LoginBean loginBean;

    // Report data
    private List<ProductDTO> reportList;
    private List<InventoryTransactionDTO> transactionHistory;
    private List<ProductDTO> allProducts;
    private List<SupplierDTO> allSuppliers;

    // KPI Summary
    private long totalInventoryQuantity = 0;
    private BigDecimal totalInventoryValue = BigDecimal.ZERO;
    private int lowStockCount = 0;
    private int outOfStockCount = 0;

    // Import Dialog Form
    private ProductDTO selectedProduct;
    private SupplierDTO selectedSupplier;
    private Long selectedProductId;
    private Long selectedSupplierId;
    private BigDecimal importQuantity = BigDecimal.valueOf(10);
    private BigDecimal importCostPrice;
    private String importReason;

    // Adjustment Dialog Form
    private Long adjustmentProductId;
    private String adjustmentProductName;
    private BigDecimal adjustmentCurrentStock;
    private String adjustmentType; // 'ADD', 'REMOVE', 'CORRECTION'
    private BigDecimal adjustmentQuantity = BigDecimal.ZERO;
    private String adjustmentReason;

    @PostConstruct
    public void init() {
        loadData();
    }

    public void loadData() {
        try {
            reportList = inventoryService.getInventoryProfitReport();
            transactionHistory = inventoryService.getTransactionHistory(50);
            allProducts = productService.getAllActiveProducts();
            allSuppliers = supplierService.getAllActiveSuppliers();

            totalInventoryQuantity = inventoryService.getTotalInventoryQuantity();
            totalInventoryValue = inventoryService.getTotalInventoryValue();
            lowStockCount = inventoryService.getLowStockCount();
            outOfStockCount = inventoryService.getOutOfStockCount();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void prepareNewImport() {
        this.selectedProduct = null;
        this.selectedSupplier = null;
        this.selectedProductId = null;
        this.selectedSupplierId = null;
        this.importQuantity = BigDecimal.valueOf(10);
        this.importCostPrice = BigDecimal.ZERO;
        this.importReason = "Nhập hàng định kỳ từ nhà cung cấp";
    }

    public void prepareQuickImport(ProductDTO prod) {
        if (prod != null) {
            this.selectedProduct = prod;
            this.selectedProductId = prod.getId();
            this.selectedSupplierId = prod.getSupplierId();
            if (this.selectedSupplierId != null && allSuppliers != null) {
                this.selectedSupplier = allSuppliers.stream()
                    .filter(s -> s.getId().equals(this.selectedSupplierId))
                    .findFirst().orElse(null);
            } else {
                this.selectedSupplier = null;
            }
            if (prod.getSuggestedRestockQuantity() != null && prod.getSuggestedRestockQuantity().compareTo(BigDecimal.ZERO) > 0) {
                this.importQuantity = prod.getSuggestedRestockQuantity();
                this.importReason = "Nhập bổ sung theo gợi ý (" + prod.getSuggestedRestockQuantity() + " " + prod.getUnit() + ") cho " + prod.getName();
            } else {
                this.importQuantity = BigDecimal.valueOf(20);
                this.importReason = "Nhập bổ sung cho " + prod.getName();
            }
            this.importCostPrice = prod.getCostPrice() != null ? prod.getCostPrice() : BigDecimal.ZERO;
        }
    }

    public void onProductSelected() {
        if (selectedProduct != null) {
            this.selectedProductId = selectedProduct.getId();
            if (selectedProduct.getSupplierId() != null) {
                this.selectedSupplierId = selectedProduct.getSupplierId();
                if (allSuppliers != null) {
                    this.selectedSupplier = allSuppliers.stream()
                        .filter(s -> s.getId().equals(this.selectedSupplierId))
                        .findFirst().orElse(null);
                }
            }
            if (selectedProduct.getCostPrice() != null && selectedProduct.getCostPrice().compareTo(BigDecimal.ZERO) > 0) {
                this.importCostPrice = selectedProduct.getCostPrice();
            }
        }
    }

    public List<ProductDTO> completeProduct(String query) {
        if (allProducts == null) {
            allProducts = productService.getAllActiveProducts();
        }
        if (query == null || query.trim().isEmpty()) {
            return allProducts.stream().limit(15).collect(java.util.stream.Collectors.toList());
        }
        String q = query.trim().toLowerCase();
        return allProducts.stream()
            .filter(p -> (p.getName() != null && p.getName().toLowerCase().contains(q)) 
                      || (p.getProductCode() != null && p.getProductCode().toLowerCase().contains(q)))
            .limit(15)
            .collect(java.util.stream.Collectors.toList());
    }

    public List<SupplierDTO> completeSupplier(String query) {
        if (allSuppliers == null) {
            allSuppliers = supplierService.getAllActiveSuppliers();
        }
        if (query == null || query.trim().isEmpty()) {
            return allSuppliers.stream().limit(15).collect(java.util.stream.Collectors.toList());
        }
        String q = query.trim().toLowerCase();
        return allSuppliers.stream()
            .filter(s -> (s.getName() != null && s.getName().toLowerCase().contains(q)) 
                      || (s.getSupplierCode() != null && s.getSupplierCode().toLowerCase().contains(q)))
            .limit(15)
            .collect(java.util.stream.Collectors.toList());
    }

    public void executeStockImport() {
        if (selectedProduct != null) {
            selectedProductId = selectedProduct.getId();
        }
        if (selectedSupplier != null) {
            selectedSupplierId = selectedSupplier.getId();
        }

        boolean hasError = false;

        if (selectedProductId == null) {
            FacesContext.getCurrentInstance().addMessage("importDialogForm:prodSelect",
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Vui lòng chọn sản phẩm"));
            hasError = true;
        }

        if (importQuantity == null || importQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            FacesContext.getCurrentInstance().addMessage("importDialogForm:qtyInput",
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Số lượng nhập phải lớn hơn 0"));
            hasError = true;
        }

        if (importCostPrice == null || importCostPrice.compareTo(BigDecimal.ZERO) < 0) {
            FacesContext.getCurrentInstance().addMessage("importDialogForm:costInput",
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Đơn giá vốn không hợp lệ"));
            hasError = true;
        }

        if (hasError) {
            return;
        }

        try {
            Long adminId = loginBean.getCurrentUser() != null ? loginBean.getCurrentUser().getId() : null;
            inventoryService.importStock(selectedProductId, selectedSupplierId, importQuantity, importCostPrice, importReason, adminId);

            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đã nhập kho thành công " + importQuantity + " sản phẩm."));

            // Reload data
            loadData();
            org.primefaces.PrimeFaces.current().executeScript("PF('importDialog').hide();");
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Thất bại", e.getMessage()));
        }
    }

    public void prepareAdjustment(ProductDTO prod) {
        if (prod != null) {
            this.adjustmentProductId = prod.getId();
            this.adjustmentProductName = prod.getName();
            this.adjustmentCurrentStock = prod.getStockQuantity();
            this.adjustmentType = "CORRECTION";
            this.adjustmentQuantity = prod.getStockQuantity();
            this.adjustmentReason = "";
        }
    }

    public void executeStockAdjustment() {
        if (adjustmentProductId == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Vui lòng chọn sản phẩm cần kiểm kê"));
            return;
        }
        if (adjustmentReason == null || adjustmentReason.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("adjustDialogForm:adjustReasonInput",
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Vui lòng nhập lý do điều chỉnh tồn kho"));
            return;
        }
        if (adjustmentQuantity == null || adjustmentQuantity.compareTo(BigDecimal.ZERO) < 0) {
            FacesContext.getCurrentInstance().addMessage("adjustDialogForm:adjustQtyInput",
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", "Vui lòng nhập số lượng không âm"));
            return;
        }

        try {
            Long adminId = loginBean.getCurrentUser() != null ? loginBean.getCurrentUser().getId() : null;
            inventoryService.adjustStock(adjustmentProductId, adjustmentType, adjustmentQuantity, adjustmentReason, adminId);

            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đã cập nhật tồn kho sản phẩm: " + adjustmentProductName));

            loadData();
            org.primefaces.PrimeFaces.current().executeScript("PF('adjustDialog').hide();");
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi điều chỉnh", e.getMessage()));
        }
    }

    // Getters and Setters
    public List<ProductDTO> getReportList() { return reportList; }
    public List<InventoryTransactionDTO> getTransactionHistory() { return transactionHistory; }
    public List<ProductDTO> getAllProducts() { return allProducts; }
    public List<SupplierDTO> getAllSuppliers() { return allSuppliers; }

    public long getTotalInventoryQuantity() { return totalInventoryQuantity; }
    public BigDecimal getTotalInventoryValue() { return totalInventoryValue; }
    public int getLowStockCount() { return lowStockCount; }
    public int getOutOfStockCount() { return outOfStockCount; }

    public ProductDTO getSelectedProduct() { return selectedProduct; }
    public void setSelectedProduct(ProductDTO selectedProduct) { this.selectedProduct = selectedProduct; }

    public SupplierDTO getSelectedSupplier() { return selectedSupplier; }
    public void setSelectedSupplier(SupplierDTO selectedSupplier) { this.selectedSupplier = selectedSupplier; }

    public Long getSelectedProductId() { return selectedProductId; }
    public void setSelectedProductId(Long selectedProductId) { this.selectedProductId = selectedProductId; }

    public Long getSelectedSupplierId() { return selectedSupplierId; }
    public void setSelectedSupplierId(Long selectedSupplierId) { this.selectedSupplierId = selectedSupplierId; }

    public BigDecimal getImportQuantity() { return importQuantity; }
    public void setImportQuantity(BigDecimal importQuantity) { this.importQuantity = importQuantity; }

    public BigDecimal getImportCostPrice() { return importCostPrice; }
    public void setImportCostPrice(BigDecimal importCostPrice) { this.importCostPrice = importCostPrice; }

    public String getImportReason() { return importReason; }
    public void setImportReason(String importReason) { this.importReason = importReason; }

    public Long getAdjustmentProductId() { return adjustmentProductId; }
    public void setAdjustmentProductId(Long adjustmentProductId) { this.adjustmentProductId = adjustmentProductId; }

    public String getAdjustmentProductName() { return adjustmentProductName; }
    public void setAdjustmentProductName(String adjustmentProductName) { this.adjustmentProductName = adjustmentProductName; }

    public BigDecimal getAdjustmentCurrentStock() { return adjustmentCurrentStock; }
    public void setAdjustmentCurrentStock(BigDecimal adjustmentCurrentStock) { this.adjustmentCurrentStock = adjustmentCurrentStock; }

    public String getAdjustmentType() { return adjustmentType; }
    public void setAdjustmentType(String adjustmentType) { this.adjustmentType = adjustmentType; }

    public BigDecimal getAdjustmentQuantity() { return adjustmentQuantity; }
    public void setAdjustmentQuantity(BigDecimal adjustmentQuantity) { this.adjustmentQuantity = adjustmentQuantity; }

    public String getAdjustmentReason() { return adjustmentReason; }
    public void setAdjustmentReason(String adjustmentReason) { this.adjustmentReason = adjustmentReason; }
}
