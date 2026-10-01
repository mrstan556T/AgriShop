package com.agrishop.service;

import com.agrishop.dto.ProductDTO;
import jakarta.ejb.Local;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Local
public interface DashboardServiceLocal {
    long getTotalProducts();
    BigDecimal getTotalRevenue();
    BigDecimal getTotalCost();
    BigDecimal getTotalProfit();
    double getProfitMargin();
    long getTotalOrders();
    long getTotalCustomers();
    long getTotalInventoryQuantity();
    BigDecimal getTotalInventoryValue();
    int getLowStockCount();
    int getOutOfStockCount();
    List<ProductDTO> getTopProfitableProducts(int limit);

    // Filtered by Date Range (Section 12 Enterprise Dashboard Requirements)
    BigDecimal getGrossSales(Date fromDate, Date toDate);
    BigDecimal getTotalDiscount(Date fromDate, Date toDate);
    BigDecimal getNetRevenue(Date fromDate, Date toDate);
    BigDecimal getTotalCost(Date fromDate, Date toDate);
    BigDecimal getTotalProfit(Date fromDate, Date toDate);
    double getProfitMargin(Date fromDate, Date toDate);
    long getTotalOrders(Date fromDate, Date toDate);
    long getPendingOrders(Date fromDate, Date toDate);
    long getNewCustomers(Date fromDate, Date toDate);
    List<ProductDTO> getTopProfitableProducts(int limit, Date fromDate, Date toDate);

    // Prompt 3 Analytics & Reports
    List<com.agrishop.dto.SupplierPerformanceDTO> getSupplierPerformanceRanking();
    List<com.agrishop.dto.InventoryTurnoverDTO> getInventoryTurnoverReport(Date fromDate, Date toDate);
    List<ProductDTO> getLowStockWithSuggestions();
}
