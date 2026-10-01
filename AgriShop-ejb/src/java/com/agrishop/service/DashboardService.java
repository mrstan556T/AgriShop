package com.agrishop.service;

import com.agrishop.dto.InventoryTurnoverDTO;
import com.agrishop.dto.ProductDTO;
import com.agrishop.dto.SupplierPerformanceDTO;
import com.agrishop.entity.Product;
import com.agrishop.entity.Supplier;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Stateless
public class DashboardService implements DashboardServiceLocal {

    @PersistenceContext(unitName = "AgriShopPU")
    private EntityManager em;

    @Override
    public long getTotalProducts() {
        return em.createQuery("SELECT COUNT(p) FROM Product p WHERE (p.isDeleted = false OR p.isDeleted IS NULL)", Long.class)
                 .getSingleResult();
    }

    @Override
    public BigDecimal getTotalRevenue() {
        return getNetRevenue(null, null);
    }

    @Override
    public BigDecimal getTotalCost() {
        return getTotalCost(null, null);
    }

    @Override
    public BigDecimal getTotalProfit() {
        return getTotalProfit(null, null);
    }

    @Override
    public double getProfitMargin() {
        return getProfitMargin(null, null);
    }

    @Override
    public long getTotalOrders() {
        return getTotalOrders(null, null);
    }

    @Override
    public long getTotalCustomers() {
        return em.createQuery("SELECT COUNT(u) FROM User u WHERE u.role = 'CUSTOMER' AND u.status = 'ACTIVE'", Long.class)
                 .getSingleResult();
    }

    @Override
    public long getTotalInventoryQuantity() {
        BigDecimal sum = em.createQuery("SELECT SUM(p.stockQuantity) FROM Product p WHERE (p.isDeleted = false OR p.isDeleted IS NULL)", BigDecimal.class)
                           .getSingleResult();
        return sum != null ? sum.longValue() : 0L;
    }

    @Override
    public BigDecimal getTotalInventoryValue() {
        BigDecimal val = em.createQuery("SELECT SUM(p.stockQuantity * p.costPrice) FROM Product p WHERE (p.isDeleted = false OR p.isDeleted IS NULL)", BigDecimal.class)
                           .getSingleResult();
        return val != null ? val : BigDecimal.ZERO;
    }

    @Override
    public int getLowStockCount() {
        Long count = em.createQuery("SELECT COUNT(p) FROM Product p WHERE (p.isDeleted = false OR p.isDeleted IS NULL) AND p.stockQuantity > 0 AND p.stockQuantity <= p.minStock", Long.class)
                       .getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public int getOutOfStockCount() {
        Long count = em.createQuery("SELECT COUNT(p) FROM Product p WHERE (p.isDeleted = false OR p.isDeleted IS NULL) AND p.stockQuantity <= 0", Long.class)
                       .getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public List<ProductDTO> getTopProfitableProducts(int limit) {
        return getTopProfitableProducts(limit, null, null);
    }

    // ==================== DATE-FILTERED IMPLEMENTATIONS ====================

    @Override
    public BigDecimal getGrossSales(Date fromDate, Date toDate) {
        StringBuilder jpql = new StringBuilder("SELECT SUM(o.subtotal) FROM Orders o WHERE o.status = 'DELIVERED'");
        appendDateFilter(jpql, "o.createdAt", fromDate, toDate);
        TypedQuery<BigDecimal> q = em.createQuery(jpql.toString(), BigDecimal.class);
        setDateParams(q, fromDate, toDate);
        BigDecimal val = q.getSingleResult();
        return val != null ? val : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalDiscount(Date fromDate, Date toDate) {
        StringBuilder jpql = new StringBuilder("SELECT SUM(o.discountAmount) FROM Orders o WHERE o.status = 'DELIVERED'");
        appendDateFilter(jpql, "o.createdAt", fromDate, toDate);
        TypedQuery<BigDecimal> q = em.createQuery(jpql.toString(), BigDecimal.class);
        setDateParams(q, fromDate, toDate);
        BigDecimal val = q.getSingleResult();
        return val != null ? val : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getNetRevenue(Date fromDate, Date toDate) {
        BigDecimal gross = getGrossSales(fromDate, toDate);
        BigDecimal discount = getTotalDiscount(fromDate, toDate);
        BigDecimal net = gross.subtract(discount);
        return net.compareTo(BigDecimal.ZERO) >= 0 ? net : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalCost(Date fromDate, Date toDate) {
        StringBuilder jpql = new StringBuilder("SELECT SUM(od.costPrice * od.quantity) FROM OrderDetail od WHERE od.order.status = 'DELIVERED'");
        appendDateFilter(jpql, "od.order.createdAt", fromDate, toDate);
        TypedQuery<BigDecimal> q = em.createQuery(jpql.toString(), BigDecimal.class);
        setDateParams(q, fromDate, toDate);
        BigDecimal cost = q.getSingleResult();
        return cost != null ? cost : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalProfit(Date fromDate, Date toDate) {
        return getNetRevenue(fromDate, toDate).subtract(getTotalCost(fromDate, toDate));
    }

    @Override
    public double getProfitMargin(Date fromDate, Date toDate) {
        BigDecimal rev = getNetRevenue(fromDate, toDate);
        if (rev.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal profit = getTotalProfit(fromDate, toDate);
            return profit.multiply(BigDecimal.valueOf(100))
                         .divide(rev, 2, RoundingMode.HALF_UP)
                         .doubleValue();
        }
        return 0.0;
    }

    @Override
    public long getTotalOrders(Date fromDate, Date toDate) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(o) FROM Orders o WHERE 1=1");
        appendDateFilter(jpql, "o.createdAt", fromDate, toDate);
        TypedQuery<Long> q = em.createQuery(jpql.toString(), Long.class);
        setDateParams(q, fromDate, toDate);
        return q.getSingleResult();
    }

    @Override
    public long getPendingOrders(Date fromDate, Date toDate) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(o) FROM Orders o WHERE o.status = 'PENDING'");
        appendDateFilter(jpql, "o.createdAt", fromDate, toDate);
        TypedQuery<Long> q = em.createQuery(jpql.toString(), Long.class);
        setDateParams(q, fromDate, toDate);
        return q.getSingleResult();
    }

    @Override
    public long getNewCustomers(Date fromDate, Date toDate) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(u) FROM User u WHERE u.role = 'CUSTOMER'");
        appendDateFilter(jpql, "u.createdAt", fromDate, toDate);
        TypedQuery<Long> q = em.createQuery(jpql.toString(), Long.class);
        setDateParams(q, fromDate, toDate);
        return q.getSingleResult();
    }

    @Override
    public List<ProductDTO> getTopProfitableProducts(int limit, Date fromDate, Date toDate) {
        int max = limit > 0 ? limit : 5;

        // Optimized Aggregated Single Query - Solves N+1 Query completely
        StringBuilder jpql = new StringBuilder(
            "SELECT od.product.id, od.productCode, od.productName, od.unit, " +
            "SUM(od.unitPrice * od.quantity), " +
            "SUM(od.costPrice * od.quantity), " +
            "SUM(od.quantity) " +
            "FROM OrderDetail od " +
            "WHERE od.order.status = 'DELIVERED' "
        );
        appendDateFilter(jpql, "od.order.createdAt", fromDate, toDate);
        jpql.append(" GROUP BY od.product.id, od.productCode, od.productName, od.unit ");
        jpql.append(" ORDER BY SUM(od.unitPrice * od.quantity) DESC ");

        TypedQuery<Object[]> q = em.createQuery(jpql.toString(), Object[].class);
        setDateParams(q, fromDate, toDate);

        List<Object[]> rows = q.getResultList();
        List<ProductDTO> dtoList = new ArrayList<>();

        for (Object[] row : rows) {
            ProductDTO dto = new ProductDTO();
            dto.setId((Long) row[0]);
            dto.setProductCode((String) row[1]);
            dto.setName((String) row[2]);
            dto.setUnit((String) row[3]);
            BigDecimal rev = row[4] != null ? (BigDecimal) row[4] : BigDecimal.ZERO;
            BigDecimal cost = row[5] != null ? (BigDecimal) row[5] : BigDecimal.ZERO;
            BigDecimal qty = row[6] != null ? (row[6] instanceof BigDecimal ? (BigDecimal) row[6] : BigDecimal.valueOf(((Number) row[6]).doubleValue())) : BigDecimal.ZERO;

            dto.setRevenue(rev);
            dto.setProfit(rev.subtract(cost));
            dto.setTotalSold(qty);
            dtoList.add(dto);
        }

        // Sort descending by profit
        dtoList.sort((a, b) -> b.getProfit().compareTo(a.getProfit()));

        if (dtoList.size() > max) {
            return dtoList.subList(0, max);
        }
        return dtoList;
    }

    private void appendDateFilter(StringBuilder jpql, String dateField, Date fromDate, Date toDate) {
        if (fromDate != null) {
            jpql.append(" AND ").append(dateField).append(" >= :fromDate ");
        }
        if (toDate != null) {
            jpql.append(" AND ").append(dateField).append(" <= :toDate ");
        }
    }

    private void setDateParams(TypedQuery<?> q, Date fromDate, Date toDate) {
        if (fromDate != null) {
            q.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            q.setParameter("toDate", toDate);
        }
    }

    // ==================== PROMPT 3: BÁO CÁO HIỆU SUẤT NHÀ CUNG CẤP ====================
    @Override
    public List<SupplierPerformanceDTO> getSupplierPerformanceRanking() {
        List<Supplier> suppliers = em.createQuery("SELECT s FROM Supplier s WHERE (s.isDeleted = false OR s.isDeleted IS NULL) ORDER BY s.name ASC", Supplier.class)
                                     .getResultList();
        List<SupplierPerformanceDTO> result = new ArrayList<>();

        for (Supplier s : suppliers) {
            SupplierPerformanceDTO dto = new SupplierPerformanceDTO();
            dto.setSupplierId(s.getId());
            dto.setSupplierCode(s.getSupplierCode());
            dto.setSupplierName(s.getName());
            dto.setSupplierPhone(s.getPhone());

            // 1. Tổng số sản phẩm cung cấp
            Long prodCount = em.createQuery("SELECT COUNT(p) FROM Product p WHERE p.supplier.id = :sId AND (p.isDeleted = false OR p.isDeleted IS NULL)", Long.class)
                               .setParameter("sId", s.getId())
                               .getSingleResult();
            dto.setTotalProductsSupplied(prodCount != null ? prodCount : 0L);

            // 2. Tổng giá trị nhập hàng (InventoryTransactions type = 'IMPORT')
            BigDecimal importVal = em.createQuery("SELECT SUM(tx.quantityChanged * tx.unitCost) FROM InventoryTransaction tx WHERE (tx.supplier.id = :sId OR tx.product.supplier.id = :sId) AND tx.transactionType = 'IMPORT'", BigDecimal.class)
                                     .setParameter("sId", s.getId())
                                     .getSingleResult();
            dto.setTotalImportValue(importVal != null ? importVal : BigDecimal.ZERO);

            // 3. Tổng lượng đã bán và tổng doanh thu sinh ra (từ OrderDetails của đơn DELIVERED)
            List<Object[]> salesData = em.createQuery("SELECT SUM(od.quantity), SUM(od.quantity * od.unitPrice) FROM OrderDetail od WHERE od.product.supplier.id = :sId AND od.order.status = 'DELIVERED'", Object[].class)
                                         .setParameter("sId", s.getId())
                                         .getResultList();
            if (!salesData.isEmpty() && salesData.get(0) != null) {
                Object[] row = salesData.get(0);
                BigDecimal soldQty = row[0] != null ? (BigDecimal) row[0] : BigDecimal.ZERO;
                BigDecimal rev = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
                dto.setTotalSoldQuantity(soldQty);
                dto.setTotalRevenue(rev);
            }

            // 4. Tỷ lệ sản phẩm bán chạy: số sản phẩm có totalSold > 10 hoặc > 0
            if (dto.getTotalProductsSupplied() > 0) {
                Long bestSellers = em.createQuery("SELECT COUNT(p) FROM Product p WHERE p.supplier.id = :sId AND (p.isDeleted = false OR p.isDeleted IS NULL) AND p.totalSold >= 5", Long.class)
                                     .setParameter("sId", s.getId())
                                     .getSingleResult();
                long bsCount = bestSellers != null ? bestSellers : 0L;
                dto.setBestSellerCount(bsCount);
                double ratio = (bsCount * 100.0) / dto.getTotalProductsSupplied();
                dto.setBestSellerRatio(Math.round(ratio * 10.0) / 10.0);
            }

            result.add(dto);
        }

        // Xếp hạng giảm dần theo Doanh Thu (nếu doanh thu bằng nhau thì theo Giá trị nhập)
        result.sort((a, b) -> {
            int comp = b.getTotalRevenue().compareTo(a.getTotalRevenue());
            if (comp != 0) return comp;
            return b.getTotalImportValue().compareTo(a.getTotalImportValue());
        });

        int rank = 1;
        for (SupplierPerformanceDTO dto : result) {
            dto.setRank(rank++);
        }

        return result;
    }

    // ==================== PROMPT 3: BÁO CÁO VÒNG QUAY TỒN KHO ====================
    @Override
    public List<InventoryTurnoverDTO> getInventoryTurnoverReport(Date fromDate, Date toDate) {
        if (fromDate == null) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -30);
            fromDate = cal.getTime();
        }
        if (toDate == null) {
            toDate = new Date();
        }

        List<Product> products = em.createQuery("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.supplier WHERE (p.isDeleted = false OR p.isDeleted IS NULL) ORDER BY p.name ASC", Product.class)
                                   .getResultList();
        List<InventoryTurnoverDTO> list = new ArrayList<>();

        for (Product p : products) {
            InventoryTurnoverDTO dto = new InventoryTurnoverDTO();
            dto.setProductId(p.getId());
            dto.setProductCode(p.getProductCode());
            dto.setProductName(p.getName());
            dto.setCategoryName(p.getCategory() != null ? p.getCategory().getName() : "Khác");
            dto.setSupplierName(p.getSupplier() != null ? p.getSupplier().getName() : "Chưa gán");
            dto.setUnit(p.getUnit());

            BigDecimal currentStock = p.getStockQuantity() != null ? p.getStockQuantity() : BigDecimal.ZERO;
            BigDecimal costPrice = p.getCostPrice() != null ? p.getCostPrice() : BigDecimal.ZERO;
            dto.setCurrentStock(currentStock);
            dto.setCostPrice(costPrice);

            // 1. Tổng lượng bán trong kỳ từ OrderDetails (đơn DELIVERED)
            BigDecimal soldInPeriod = em.createQuery("SELECT SUM(od.quantity) FROM OrderDetail od WHERE od.product.id = :pId AND od.order.status = 'DELIVERED' AND od.order.createdAt >= :fDate AND od.order.createdAt <= :tDate", BigDecimal.class)
                                        .setParameter("pId", p.getId())
                                        .setParameter("fDate", fromDate)
                                        .setParameter("tDate", toDate)
                                        .getSingleResult();
            if (soldInPeriod == null) soldInPeriod = BigDecimal.ZERO;
            dto.setTotalSoldInPeriod(soldInPeriod);

            // 2. Tồn kho bình quân = (Tồn đầu kỳ + Tồn cuối kỳ) / 2 xấp xỉ bằng currentStock + (soldInPeriod / 2)
            BigDecimal avgStock = currentStock.add(soldInPeriod.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP));
            if (avgStock.compareTo(BigDecimal.ZERO) <= 0) {
                avgStock = currentStock.compareTo(BigDecimal.ZERO) > 0 ? currentStock : BigDecimal.ONE;
            }
            dto.setAverageStock(avgStock);

            // 3. Hệ số vòng quay tồn kho (Turnover Ratio) = totalSold / averageStock
            double turnover = 0.0;
            if (avgStock.compareTo(BigDecimal.ZERO) > 0) {
                turnover = soldInPeriod.doubleValue() / avgStock.doubleValue();
            }
            dto.setTurnoverRatio(Math.round(turnover * 100.0) / 100.0);

            // 4. Vốn ứ đọng = currentStock * costPrice
            BigDecimal tiedUp = currentStock.multiply(costPrice);
            dto.setTiedUpCapital(tiedUp);

            // 5. Cảnh báo tồn kho quay vòng chậm (ứ đọng vốn)
            // Nếu có tồn kho > 0 mà turnover < 0.5 (hoặc 0 bán được gì trong kỳ)
            boolean isSlow = currentStock.compareTo(BigDecimal.ZERO) > 0 && (turnover < 0.5 || soldInPeriod.compareTo(BigDecimal.ZERO) == 0);
            dto.setSlowMoving(isSlow);

            if (isSlow) {
                dto.setTurnoverStatus("SLOW_MOVING");
            } else if (turnover >= 2.0) {
                dto.setTurnoverStatus("FAST_MOVING");
            } else {
                dto.setTurnoverStatus("HEALTHY");
            }

            list.add(dto);
        }

        // Ưu tiên sắp xếp các mặt hàng quay vòng chậm (slowMoving) lên trước, sau đó theo vốn ứ đọng giảm dần
        list.sort((a, b) -> {
            if (a.isSlowMoving() != b.isSlowMoving()) {
                return a.isSlowMoving() ? -1 : 1;
            }
            return b.getTiedUpCapital().compareTo(a.getTiedUpCapital());
        });

        return list;
    }

    // ==================== PROMPT 3: GỢI Ý SỐ LƯỢNG NHẬP HÀNG KHI CHẠM MIN_STOCK ====================
    @Override
    public List<ProductDTO> getLowStockWithSuggestions() {
        List<Product> lowStockProducts = em.createQuery("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.supplier WHERE (p.isDeleted = false OR p.isDeleted IS NULL) AND p.stockQuantity <= p.minStock ORDER BY p.stockQuantity ASC", Product.class)
                                           .getResultList();
        List<ProductDTO> result = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -30);
        Date thirtyDaysAgo = cal.getTime();

        for (Product p : lowStockProducts) {
            ProductDTO dto = convertToDTO(p);

            // Truy vấn tổng lượng bán 30 ngày gần nhất từ InventoryTransactions (type = 'SALE')
            BigDecimal soldLast30d = em.createQuery("SELECT SUM(tx.quantityChanged) FROM InventoryTransaction tx WHERE tx.product.id = :pId AND tx.transactionType = 'SALE' AND tx.createdAt >= :startDate", BigDecimal.class)
                                       .setParameter("pId", p.getId())
                                       .setParameter("startDate", thirtyDaysAgo)
                                       .getSingleResult();
            
            BigDecimal absSold = soldLast30d != null ? soldLast30d.abs() : BigDecimal.ZERO;
            
            // Tốc độ bán trung bình ngày
            BigDecimal dailyVelocity = absSold.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
            dto.setAverageDailySale30d(dailyVelocity);

            // Gợi ý số lượng nhập hàng:
            // Đảm bảo đủ hàng bán cho chu kỳ 30 ngày tiếp theo đưa tồn kho về mức an toàn:
            // Target = max(Lượng bán 30 ngày qua, minStock * 2)
            BigDecimal targetStock = absSold.max(p.getMinStock().multiply(BigDecimal.valueOf(2)));
            BigDecimal avail = p.getAvailableQuantity();
            BigDecimal suggested = targetStock.subtract(avail);

            if (suggested.compareTo(BigDecimal.ZERO) <= 0) {
                suggested = p.getMinStock(); // Tối thiểu nhập bằng minStock
            }

            // Làm tròn số lượng theo unitType
            if ("WEIGHT".equalsIgnoreCase(p.getUnitType())) {
                // Hàng cân: làm tròn lên 0.5 gần nhất
                double val = suggested.doubleValue();
                val = Math.ceil(val * 2.0) / 2.0;
                suggested = BigDecimal.valueOf(val).setScale(1, RoundingMode.HALF_UP);
            } else {
                // Hàng đếm: làm tròn lên số nguyên
                suggested = BigDecimal.valueOf(Math.ceil(suggested.doubleValue())).setScale(0, RoundingMode.HALF_UP);
            }

            dto.setSuggestedRestockQuantity(suggested);
            result.add(dto);
        }

        return result;
    }

    private ProductDTO convertToDTO(Product p) {
        if (p == null) return null;
        ProductDTO dto = new ProductDTO();
        dto.setId(p.getId());
        dto.setProductCode(p.getProductCode());
        dto.setName(p.getName());
        dto.setPrice(p.getPrice());
        dto.setStockQuantity(p.getStockQuantity());
        dto.setReservedQuantity(p.getReservedQuantity());
        dto.setMinStock(p.getMinStock());
        dto.setUnit(p.getUnit());
        dto.setUnitType(p.getUnitType());
        dto.setImageUrl(p.getImageUrl());
        if (p.getCategory() != null) {
            dto.setCategoryId(p.getCategory().getId());
            dto.setCategoryName(p.getCategory().getName());
        }
        if (p.getSupplier() != null) {
            dto.setSupplierId(p.getSupplier().getId());
            dto.setSupplierName(p.getSupplier().getName());
        }
        return dto;
    }
}
