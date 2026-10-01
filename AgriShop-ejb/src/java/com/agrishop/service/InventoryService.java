package com.agrishop.service;

import com.agrishop.dto.InventoryTransactionDTO;
import com.agrishop.dto.ProductDTO;
import com.agrishop.entity.InventoryTransaction;
import com.agrishop.entity.Product;
import com.agrishop.entity.Supplier;
import com.agrishop.entity.User;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Stateless
public class InventoryService implements InventoryServiceLocal {

    @PersistenceContext(unitName = "AgriShopPU")
    private EntityManager em;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void importStock(Long productId, Long supplierId, int quantity, BigDecimal costPrice, String reason, Long adminUserId) throws Exception {
        importStock(productId, supplierId, BigDecimal.valueOf(quantity), costPrice, reason, adminUserId);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void importStock(Long productId, Long supplierId, BigDecimal quantity, BigDecimal costPrice, String reason, Long adminUserId) throws Exception {
        if (productId == null) {
            throw new Exception("Vui lòng chọn sản phẩm cần nhập hàng.");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Số lượng nhập kho phải lớn hơn 0.");
        }
        if (costPrice == null || costPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new Exception("Đơn giá vốn nhập hàng không hợp lệ.");
        }

        Product product = em.find(Product.class, productId);
        if (product == null || (product.getIsDeleted() != null && product.getIsDeleted())) {
            throw new Exception("Sản phẩm không tồn tại hoặc đã bị xóa.");
        }

        User user = null;
        if (adminUserId != null) {
            user = em.find(User.class, adminUserId);
        }
        if (user == null) {
            // Fallback to first active admin
            List<User> admins = em.createQuery("SELECT u FROM User u WHERE u.role = 'ADMIN'", User.class)
                                  .setMaxResults(1)
                                  .getResultList();
            if (!admins.isEmpty()) {
                user = admins.get(0);
            } else {
                throw new Exception("Không tìm thấy thông tin quản trị viên thực hiện nhập hàng.");
            }
        }

        Supplier supplier = null;
        if (supplierId != null) {
            supplier = em.find(Supplier.class, supplierId);
        }
        if (supplier == null && product.getSupplier() != null) {
            supplier = product.getSupplier();
        }

        // 1. Calculate Moving Average Cost Price
        BigDecimal currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : BigDecimal.ZERO;
        BigDecimal currentImported = product.getTotalImported() != null ? product.getTotalImported() : currentStock;

        // New Cost = (Old Stock * Old Cost + New Quantity * New Cost) / Total Stock
        BigDecimal oldCost = product.getCostPrice() != null ? product.getCostPrice() : BigDecimal.ZERO;
        BigDecimal totalStock = currentStock.add(quantity);

        BigDecimal movingAverageCost;
        if (totalStock.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal totalValue = (currentStock.multiply(oldCost)).add(quantity.multiply(costPrice));
            movingAverageCost = totalValue.divide(totalStock, 2, java.math.RoundingMode.HALF_UP);
        } else {
            movingAverageCost = costPrice;
        }

        product.setStockQuantity(currentStock.add(quantity));
        product.setTotalImported(currentImported.add(quantity));
        product.setCostPrice(movingAverageCost);
        if (supplier != null && product.getSupplier() == null) {
            product.setSupplier(supplier);
        }
        product.setUpdatedAt(new Date());
        em.merge(product);

        // 2. Record InventoryTransaction
        InventoryTransaction tx = new InventoryTransaction();
        tx.setProduct(product);
        tx.setUser(user);
        tx.setSupplier(supplier);
        tx.setQuantityChanged(quantity);
        tx.setUnitCost(costPrice);
        tx.setTransactionType("IMPORT");
        tx.setReason((reason != null && !reason.trim().isEmpty()) ? reason.trim() : "Nhập hàng từ nhà cung cấp");
        tx.setCreatedAt(new Date());

        em.persist(tx);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void adjustStock(Long productId, String adjustmentType, int quantity, String reason, Long adminUserId) throws Exception {
        adjustStock(productId, adjustmentType, BigDecimal.valueOf(quantity), reason, adminUserId);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void adjustStock(Long productId, String adjustmentType, BigDecimal quantity, String reason, Long adminUserId) throws Exception {
        if (productId == null) {
            throw new Exception("Vui lòng chọn sản phẩm cần điều chỉnh tồn kho.");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new Exception("Vui lòng nhập lý do điều chỉnh tồn kho.");
        }
        if (adjustmentType == null) {
            throw new Exception("Vui lòng chọn loại điều chỉnh tồn kho.");
        }

        Product product = em.find(Product.class, productId);
        if (product == null || (product.getIsDeleted() != null && product.getIsDeleted())) {
            throw new Exception("Sản phẩm không tồn tại hoặc đã bị xóa.");
        }

        User user = null;
        if (adminUserId != null) {
            user = em.find(User.class, adminUserId);
        }
        if (user == null) {
            List<User> admins = em.createQuery("SELECT u FROM User u WHERE u.role = 'ADMIN'", User.class)
                                  .setMaxResults(1)
                                  .getResultList();
            if (!admins.isEmpty()) {
                user = admins.get(0);
            }
        }

        BigDecimal currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : BigDecimal.ZERO;
        BigDecimal delta = BigDecimal.ZERO;

        if ("ADD".equalsIgnoreCase(adjustmentType)) {
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) throw new Exception("Số lượng điều chỉnh tăng phải lớn hơn 0.");
            delta = quantity;
            product.setStockQuantity(currentStock.add(delta));
        } else if ("REMOVE".equalsIgnoreCase(adjustmentType)) {
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) throw new Exception("Số lượng điều chỉnh giảm phải lớn hơn 0.");
            if (currentStock.compareTo(quantity) < 0) {
                throw new Exception("Số lượng xuất giảm vượt quá tồn kho hiện tại (" + currentStock + ").");
            }
            delta = quantity.negate();
            product.setStockQuantity(currentStock.subtract(quantity));
        } else if ("CORRECTION".equalsIgnoreCase(adjustmentType)) {
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) < 0) throw new Exception("Số lượng tồn kho thực tế không được âm.");
            delta = quantity.subtract(currentStock);
            product.setStockQuantity(quantity);
        } else {
            throw new Exception("Loại điều chỉnh không hợp lệ: " + adjustmentType);
        }

        product.setUpdatedAt(new Date());
        em.merge(product);

        InventoryTransaction tx = new InventoryTransaction();
        tx.setProduct(product);
        tx.setUser(user);
        tx.setSupplier(product.getSupplier());
        tx.setQuantityChanged(delta);
        tx.setUnitCost(product.getCostPrice());
        tx.setTransactionType("ADJUST_" + adjustmentType.toUpperCase());
        tx.setReason(reason.trim());
        tx.setCreatedAt(new Date());

        em.persist(tx);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<ProductDTO> getInventoryProfitReport() {
        List<Product> products = em.createQuery("SELECT p FROM Product p JOIN FETCH p.category WHERE (p.isDeleted = false OR p.isDeleted IS NULL) ORDER BY p.name ASC", Product.class)
                                   .getResultList();
        List<ProductDTO> dtos = new ArrayList<>();
        if (products == null) return dtos;

        for (Product p : products) {
            ProductDTO dto = new ProductDTO();
            dto.setId(p.getId());
            dto.setProductCode(p.getProductCode());
            dto.setName(p.getName());
            dto.setUnit(p.getUnit());
            dto.setUnitType(p.getUnitType());
            dto.setPrice(p.getPrice());
            dto.setCostPrice(p.getCostPrice());
            dto.setStockQuantity(p.getStockQuantity());
            dto.setReservedQuantity(p.getReservedQuantity());
            dto.setMinStock(p.getMinStock());
            dto.setTotalImported(p.getTotalImported());
            dto.setTotalSold(p.getTotalSold());
            if (p.getCategory() != null) {
                dto.setCategoryName(p.getCategory().getName());
            }
            if (p.getSupplier() != null) {
                dto.setSupplierName(p.getSupplier().getName());
            }
            dto.setImageUrl(p.getImageUrl());

            BigDecimal totalSoldBd = p.getTotalSold() != null ? p.getTotalSold() : BigDecimal.ZERO;
            BigDecimal price = p.getPrice() != null ? p.getPrice() : BigDecimal.ZERO;
            BigDecimal cost = p.getCostPrice() != null ? p.getCostPrice() : BigDecimal.ZERO;

            BigDecimal revenue = totalSoldBd.multiply(price);
            BigDecimal totalCost = totalSoldBd.multiply(cost);
            BigDecimal profit = revenue.subtract(totalCost);

            dto.setRevenue(revenue);
            dto.setProfit(profit);

            // Gợi ý số lượng nhập hàng khi sản phẩm chạm hoặc thấp hơn minStock
            if (p.getStockQuantity() != null && p.getStockQuantity().compareTo(p.getMinStock()) <= 0) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, -30);
                BigDecimal sold30d = em.createQuery("SELECT SUM(tx.quantityChanged) FROM InventoryTransaction tx WHERE tx.product.id = :pId AND tx.transactionType = 'SALE' AND tx.createdAt >= :startDate", BigDecimal.class)
                                       .setParameter("pId", p.getId())
                                       .setParameter("startDate", cal.getTime())
                                       .getSingleResult();
                BigDecimal absSold = sold30d != null ? sold30d.abs() : BigDecimal.ZERO;
                BigDecimal dailyVel = absSold.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
                dto.setAverageDailySale30d(dailyVel);

                BigDecimal targetStock = absSold.max(p.getMinStock().multiply(BigDecimal.valueOf(2)));
                BigDecimal avail = p.getAvailableQuantity();
                BigDecimal suggested = targetStock.subtract(avail);
                if (suggested.compareTo(BigDecimal.ZERO) <= 0) {
                    suggested = p.getMinStock();
                }
                if ("WEIGHT".equalsIgnoreCase(p.getUnitType())) {
                    double val = Math.ceil(suggested.doubleValue() * 2.0) / 2.0;
                    suggested = BigDecimal.valueOf(val).setScale(1, RoundingMode.HALF_UP);
                } else {
                    suggested = BigDecimal.valueOf(Math.ceil(suggested.doubleValue())).setScale(0, RoundingMode.HALF_UP);
                }
                dto.setSuggestedRestockQuantity(suggested);
            }

            dtos.add(dto);
        }

        return dtos;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<InventoryTransactionDTO> getTransactionHistory(int maxResults) {
        int limit = maxResults > 0 ? maxResults : 100;
        List<InventoryTransaction> list = em.createQuery("SELECT tx FROM InventoryTransaction tx JOIN FETCH tx.product LEFT JOIN FETCH tx.supplier LEFT JOIN FETCH tx.user ORDER BY tx.createdAt DESC", InventoryTransaction.class)
                                            .setMaxResults(limit)
                                            .getResultList();
        List<InventoryTransactionDTO> dtoList = new ArrayList<>();
        if (list == null) return dtoList;

        for (InventoryTransaction tx : list) {
            InventoryTransactionDTO dto = new InventoryTransactionDTO();
            dto.setId(tx.getId());
            if (tx.getProduct() != null) {
                dto.setProductId(tx.getProduct().getId());
                dto.setProductCode(tx.getProduct().getProductCode());
                dto.setProductName(tx.getProduct().getName());
                dto.setUnit(tx.getProduct().getUnit());
            }
            if (tx.getUser() != null) {
                dto.setUserId(tx.getUser().getId());
                dto.setUserName(tx.getUser().getFullName() != null ? tx.getUser().getFullName() : tx.getUser().getUsername());
            }
            if (tx.getSupplier() != null) {
                dto.setSupplierId(tx.getSupplier().getId());
                dto.setSupplierName(tx.getSupplier().getName());
            }
            dto.setQuantityChanged(tx.getQuantityChanged());
            dto.setUnitCost(tx.getUnitCost());
            dto.setTransactionType(tx.getTransactionType());
            dto.setReason(tx.getReason());
            dto.setCreatedAt(tx.getCreatedAt());

            dtoList.add(dto);
        }
        return dtoList;
    }

    @Override
    public long getTotalInventoryQuantity() {
        BigDecimal sum = em.createQuery("SELECT SUM(p.stockQuantity) FROM Product p WHERE (p.isDeleted = false OR p.isDeleted IS NULL)", BigDecimal.class)
                           .getSingleResult();
        return sum != null ? sum.longValue() : 0L;
    }

    @Override
    public BigDecimal getTotalInventoryValue() {
        BigDecimal sum = em.createQuery("SELECT SUM(p.stockQuantity * p.costPrice) FROM Product p WHERE (p.isDeleted = false OR p.isDeleted IS NULL)", BigDecimal.class)
                           .getSingleResult();
        return sum != null ? sum : BigDecimal.ZERO;
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
}
