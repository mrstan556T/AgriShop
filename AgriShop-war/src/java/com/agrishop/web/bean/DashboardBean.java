package com.agrishop.web.bean;

import com.agrishop.dto.InventoryTurnoverDTO;
import com.agrishop.dto.OrderDTO;
import com.agrishop.dto.ProductDTO;
import com.agrishop.dto.SupplierPerformanceDTO;
import com.agrishop.service.DashboardServiceLocal;
import com.agrishop.service.OrderManagementServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;

@Named("dashboardBean")
@ViewScoped
public class DashboardBean implements Serializable {

    @EJB
    private DashboardServiceLocal dashboardService;

    @EJB
    private OrderManagementServiceLocal orderService;

    private String dateFilter = "30_DAYS"; // Default to 30 days
    private Date fromDate;
    private Date toDate;

    // Prompt 3 Analytics
    private String turnoverPeriod = "MONTH"; // "MONTH", "QUARTER"
    private List<InventoryTurnoverDTO> turnoverReportList = new ArrayList<>();
    private BigDecimal totalTiedUpCapital = BigDecimal.ZERO;
    private BarChartModel turnoverChartModel;
    private List<SupplierPerformanceDTO> supplierPerformanceList = new ArrayList<>();
    private List<ProductDTO> lowStockProductsWithSuggestions = new ArrayList<>();

    private long totalProducts;
    private BigDecimal grossSales;
    private BigDecimal totalDiscount;
    private BigDecimal totalRevenue; // Net Revenue
    private BigDecimal totalCost;    // COGS
    private BigDecimal totalProfit;  // Gross Profit
    private double profitMargin;
    private long totalOrders;
    private long pendingOrders;
    private long totalCustomers;
    private long newCustomers;
    private long totalInventoryQuantity;
    private BigDecimal totalInventoryValue;
    private int lowStockCount;
    private int outOfStockCount;
    private List<ProductDTO> topProfitableProducts;
    private List<OrderDTO> recentOrders;
    private BarChartModel revenueChartModel;

    @PostConstruct
    public void init() {
        calculateDateRange();
        loadData();
    }

    public void onDateFilterChange() {
        calculateDateRange();
        loadData();
    }

    private void calculateDateRange() {
        Calendar cal = Calendar.getInstance();
        toDate = cal.getTime();

        if ("TODAY".equals(dateFilter)) {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            fromDate = cal.getTime();
        } else if ("7_DAYS".equals(dateFilter)) {
            cal.add(Calendar.DAY_OF_YEAR, -7);
            fromDate = cal.getTime();
        } else if ("30_DAYS".equals(dateFilter)) {
            cal.add(Calendar.DAY_OF_YEAR, -30);
            fromDate = cal.getTime();
        } else if ("THIS_MONTH".equals(dateFilter)) {
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            fromDate = cal.getTime();
        } else {
            // ALL TIME
            fromDate = null;
            toDate = null;
        }
    }

    private void loadData() {
        totalProducts = dashboardService.getTotalProducts();
        grossSales = dashboardService.getGrossSales(fromDate, toDate);
        totalDiscount = dashboardService.getTotalDiscount(fromDate, toDate);
        totalRevenue = dashboardService.getNetRevenue(fromDate, toDate);
        totalCost = dashboardService.getTotalCost(fromDate, toDate);
        totalProfit = dashboardService.getTotalProfit(fromDate, toDate);
        profitMargin = dashboardService.getProfitMargin(fromDate, toDate);
        totalOrders = dashboardService.getTotalOrders(fromDate, toDate);
        pendingOrders = dashboardService.getPendingOrders(fromDate, toDate);
        totalCustomers = dashboardService.getTotalCustomers();
        newCustomers = dashboardService.getNewCustomers(fromDate, toDate);
        totalInventoryQuantity = dashboardService.getTotalInventoryQuantity();
        totalInventoryValue = dashboardService.getTotalInventoryValue();
        lowStockCount = dashboardService.getLowStockCount();
        outOfStockCount = dashboardService.getOutOfStockCount();
        topProfitableProducts = dashboardService.getTopProfitableProducts(5, fromDate, toDate);
        
        try {
            recentOrders = orderService.getOrdersLazy(0, 5, "id", "DESC", null);
        } catch (Exception e) {
            recentOrders = new ArrayList<>();
        }

        // Prompt 3: Supplier performance ranking
        try {
            supplierPerformanceList = dashboardService.getSupplierPerformanceRanking();
        } catch (Exception e) {
            supplierPerformanceList = new ArrayList<>();
        }

        // Prompt 3: Low stock with suggested restock quantities
        try {
            lowStockProductsWithSuggestions = dashboardService.getLowStockWithSuggestions();
        } catch (Exception e) {
            lowStockProductsWithSuggestions = new ArrayList<>();
        }

        // Prompt 3: Inventory Turnover
        loadTurnoverData();

        createRevenueChart();
    }

    public void onTurnoverPeriodChange() {
        loadTurnoverData();
    }

    private void loadTurnoverData() {
        Calendar cal = Calendar.getInstance();
        Date tTo = cal.getTime();
        Date tFrom;
        if ("QUARTER".equals(turnoverPeriod)) {
            cal.add(Calendar.DAY_OF_YEAR, -90);
            tFrom = cal.getTime();
        } else {
            cal.add(Calendar.DAY_OF_YEAR, -30);
            tFrom = cal.getTime();
        }

        try {
            turnoverReportList = dashboardService.getInventoryTurnoverReport(tFrom, tTo);
        } catch (Exception e) {
            turnoverReportList = new ArrayList<>();
        }

        BigDecimal tiedUp = BigDecimal.ZERO;
        if (turnoverReportList != null) {
            for (InventoryTurnoverDTO it : turnoverReportList) {
                if (it.isSlowMoving() && it.getTiedUpCapital() != null) {
                    tiedUp = tiedUp.add(it.getTiedUpCapital());
                }
            }
        }
        totalTiedUpCapital = tiedUp;

        createTurnoverChart();
    }

    private void createTurnoverChart() {
        turnoverChartModel = new BarChartModel();
        ChartData data = new ChartData();

        BarChartDataSet dataSet = new BarChartDataSet();
        dataSet.setLabel("Hệ số vòng quay (" + ("QUARTER".equals(turnoverPeriod) ? "Quý" : "30 ngày") + ")");

        List<Object> values = new ArrayList<>();
        List<String> bgColors = new ArrayList<>();
        List<String> borderColors = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int count = 0;
        if (turnoverReportList != null) {
            for (InventoryTurnoverDTO it : turnoverReportList) {
                if (count++ >= 8) break; // Display top 8 in chart for clean rendering
                values.add(it.getTurnoverRatio());
                labels.add(it.getProductName());

                if (it.isSlowMoving()) {
                    bgColors.add("rgba(239, 68, 68, 0.7)"); // Red for slow moving
                    borderColors.add("rgb(239, 68, 68)");
                } else if (it.getTurnoverRatio() >= 2.0) {
                    bgColors.add("rgba(16, 185, 129, 0.7)"); // Green for fast moving
                    borderColors.add("rgb(16, 185, 129)");
                } else {
                    bgColors.add("rgba(59, 130, 246, 0.7)"); // Blue for healthy
                    borderColors.add("rgb(59, 130, 246)");
                }
            }
        }

        dataSet.setData(values);
        dataSet.setBackgroundColor(bgColors);
        dataSet.setBorderColor(borderColors);
        dataSet.setBorderWidth(1);

        data.addChartDataSet(dataSet);
        data.setLabels(labels);

        turnoverChartModel.setData(data);
    }

    private void createRevenueChart() {
        revenueChartModel = new BarChartModel();
        ChartData data = new ChartData();

        // Dataset 1: Doanh Thu Thuần
        BarChartDataSet revDataSet = new BarChartDataSet();
        revDataSet.setLabel("Doanh thu thuần (₫)");
        List<Object> revValues = new ArrayList<>();
        revValues.add(totalRevenue != null ? totalRevenue.longValue() : 0);
        revDataSet.setData(revValues);
        revDataSet.setBackgroundColor("rgba(5, 150, 105, 0.7)");
        revDataSet.setBorderColor("rgb(5, 150, 105)");
        revDataSet.setBorderWidth(1);

        // Dataset 2: Giá Vốn (COGS)
        BarChartDataSet costDataSet = new BarChartDataSet();
        costDataSet.setLabel("Giá vốn COGS (₫)");
        List<Object> costValues = new ArrayList<>();
        costValues.add(totalCost != null ? totalCost.longValue() : 0);
        costDataSet.setData(costValues);
        costDataSet.setBackgroundColor("rgba(234, 88, 12, 0.7)");
        costDataSet.setBorderColor("rgb(234, 88, 12)");
        costDataSet.setBorderWidth(1);

        // Dataset 3: Lợi Nhuận Gộp
        BarChartDataSet profitDataSet = new BarChartDataSet();
        profitDataSet.setLabel("Lợi nhuận gộp (₫)");
        List<Object> profitValues = new ArrayList<>();
        profitValues.add(totalProfit != null ? totalProfit.longValue() : 0);
        profitDataSet.setData(profitValues);
        profitDataSet.setBackgroundColor("rgba(16, 185, 129, 0.7)");
        profitDataSet.setBorderColor("rgb(16, 185, 129)");
        profitDataSet.setBorderWidth(1);

        data.addChartDataSet(revDataSet);
        data.addChartDataSet(costDataSet);
        data.addChartDataSet(profitDataSet);

        List<String> labels = new ArrayList<>();
        String label = "Kỳ Báo Cáo (" + ("TODAY".equals(dateFilter) ? "Hôm nay" : 
                       ("7_DAYS".equals(dateFilter) ? "7 Ngày Qua" : 
                       ("30_DAYS".equals(dateFilter) ? "30 Ngày Qua" : 
                       ("THIS_MONTH".equals(dateFilter) ? "Tháng Này" : "Toàn Thời Gian")))) + ")";
        labels.add(label);
        data.setLabels(labels);

        revenueChartModel.setData(data);
    }

    public String getDateFilter() { return dateFilter; }
    public void setDateFilter(String dateFilter) { this.dateFilter = dateFilter; }
    public long getTotalProducts() { return totalProducts; }
    public BigDecimal getGrossSales() { return grossSales != null ? grossSales : BigDecimal.ZERO; }
    public BigDecimal getTotalDiscount() { return totalDiscount != null ? totalDiscount : BigDecimal.ZERO; }
    public BigDecimal getTotalRevenue() { return totalRevenue != null ? totalRevenue : BigDecimal.ZERO; }
    public BigDecimal getTotalCost() { return totalCost != null ? totalCost : BigDecimal.ZERO; }
    public BigDecimal getTotalProfit() { return totalProfit != null ? totalProfit : BigDecimal.ZERO; }
    public double getProfitMargin() { return profitMargin; }
    public long getTotalOrders() { return totalOrders; }
    public long getPendingOrders() { return pendingOrders; }
    public long getTotalCustomers() { return totalCustomers; }
    public long getNewCustomers() { return newCustomers; }
    public long getTotalInventoryQuantity() { return totalInventoryQuantity; }
    public BigDecimal getTotalInventoryValue() { return totalInventoryValue != null ? totalInventoryValue : BigDecimal.ZERO; }
    public int getLowStockCount() { return lowStockCount; }
    public int getOutOfStockCount() { return outOfStockCount; }
    public List<ProductDTO> getTopProfitableProducts() { return topProfitableProducts; }
    public List<OrderDTO> getRecentOrders() { return recentOrders; }
    public BarChartModel getRevenueChartModel() { return revenueChartModel; }

    public String getTurnoverPeriod() { return turnoverPeriod; }
    public void setTurnoverPeriod(String turnoverPeriod) { this.turnoverPeriod = turnoverPeriod; }
    public List<InventoryTurnoverDTO> getTurnoverReportList() { return turnoverReportList; }
    public BigDecimal getTotalTiedUpCapital() { return totalTiedUpCapital != null ? totalTiedUpCapital : BigDecimal.ZERO; }
    public BarChartModel getTurnoverChartModel() { return turnoverChartModel; }
    public List<SupplierPerformanceDTO> getSupplierPerformanceList() { return supplierPerformanceList; }
    public List<ProductDTO> getLowStockProductsWithSuggestions() { return lowStockProductsWithSuggestions; }
}
