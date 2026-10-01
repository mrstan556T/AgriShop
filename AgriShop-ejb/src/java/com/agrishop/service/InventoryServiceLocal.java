package com.agrishop.service;

import com.agrishop.dto.InventoryTransactionDTO;
import com.agrishop.dto.ProductDTO;
import jakarta.ejb.Local;
import java.math.BigDecimal;
import java.util.List;

@Local
public interface InventoryServiceLocal {
    void importStock(Long productId, Long supplierId, BigDecimal quantity, BigDecimal costPrice, String reason, Long adminUserId) throws Exception;
    void importStock(Long productId, Long supplierId, int quantity, BigDecimal costPrice, String reason, Long adminUserId) throws Exception;

    void adjustStock(Long productId, String adjustmentType, BigDecimal quantity, String reason, Long adminUserId) throws Exception;
    void adjustStock(Long productId, String adjustmentType, int quantity, String reason, Long adminUserId) throws Exception;

    List<ProductDTO> getInventoryProfitReport();
    List<InventoryTransactionDTO> getTransactionHistory(int maxResults);
    long getTotalInventoryQuantity();
    BigDecimal getTotalInventoryValue();
    int getLowStockCount();
    int getOutOfStockCount();
}
