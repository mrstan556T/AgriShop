package com.agrishop.service;

import com.agrishop.dto.CancellationRequestDTO;
import com.agrishop.dto.OrderDTO;
import com.agrishop.dto.OrderDetailDTO;
import com.agrishop.entity.CancellationRequest;
import com.agrishop.entity.Coupon;
import com.agrishop.entity.InventoryTransaction;
import com.agrishop.entity.OrderDetail;
import com.agrishop.entity.Orders;
import com.agrishop.entity.Product;
import com.agrishop.repository.OrderRepositoryLocal;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
public class OrderManagementService implements OrderManagementServiceLocal {

    @EJB
    private OrderRepositoryLocal orderRepository;

    @EJB
    private EmailServiceLocal emailService;

    @EJB
    private AuditLogServiceLocal auditLogService;

    @PersistenceContext(unitName = "AgriShopPU")
    private EntityManager em;

    @Override
    public List<OrderDTO> getOrdersLazy(int first, int pageSize, String sortField, String sortOrder, Map<String, Object> filters) {
        List<Orders> orders = orderRepository.getOrdersLazy(first, pageSize, sortField, sortOrder, filters);
        List<OrderDTO> dtoList = new ArrayList<>();
        if (orders == null || orders.isEmpty()) {
            return dtoList;
        }
        
        List<Long> orderIds = new ArrayList<>();
        for (Orders order : orders) {
            orderIds.add(order.getId());
        }
        
        // Batch-load order details with their products
        List<OrderDetail> allDetails = orderRepository.getOrderDetailsByOrderIds(orderIds);
        Map<Long, List<OrderDetailDTO>> detailsByOrderId = new HashMap<>();
        if (allDetails != null) {
            for (OrderDetail detail : allDetails) {
                Long oId = detail.getOrder() != null ? detail.getOrder().getId() : null;
                if (oId != null) {
                    detailsByOrderId.computeIfAbsent(oId, k -> new ArrayList<>());
                    detailsByOrderId.get(oId).add(convertToDetailDTO(detail));
                }
            }
        }
        
        for (Orders order : orders) {
            dtoList.add(convertToOrderDTO(order, detailsByOrderId.getOrDefault(order.getId(), new ArrayList<>())));
        }
        
        return dtoList;
    }

    @Override
    public int countOrders(Map<String, Object> filters) {
        return orderRepository.countOrders(filters);
    }

    @Override
    public OrderDTO getOrderDetails(Long orderId) {
        Orders order = orderRepository.findByIdWithDetails(orderId);
        if (order == null) return null;
        
        List<OrderDetailDTO> details = new ArrayList<>();
        if (order.getOrderDetails() != null) {
            for (OrderDetail detail : order.getOrderDetails()) {
                details.add(convertToDetailDTO(detail));
            }
        }
        return convertToOrderDTO(order, details);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void updateOrderStatus(Long orderId, String newStatus) throws Exception {
        Orders order = orderRepository.findByIdWithDetails(orderId);
        if (order == null) throw new Exception("Không tìm thấy đơn hàng.");
        
        String currentStatus = order.getStatus();
        if (currentStatus.equals(newStatus)) {
            return; // No change
        }

        if ("DELIVERED".equals(currentStatus) || "CANCELLED".equals(currentStatus) || "RETURNED".equals(currentStatus)) {
            throw new Exception("Không thể thay đổi trạng thái đơn hàng đã kết thúc (" + currentStatus + ").");
        }

        // State Machine validation
        if ("DELIVERED".equals(newStatus)) {
            // Fulfill: Deduct physical stock & release reserved stock, increment totalSold, record SALE transaction
            if (order.getOrderDetails() != null) {
                for (OrderDetail detail : order.getOrderDetails()) {
                    if (detail.getProduct() != null) {
                        Product p = em.find(Product.class, detail.getProduct().getId(), LockModeType.PESSIMISTIC_WRITE);
                        if (p != null) {
                            java.math.BigDecimal qty = detail.getQuantity() != null ? detail.getQuantity() : java.math.BigDecimal.ZERO;
                            java.math.BigDecimal currentStock = p.getStockQuantity() != null ? p.getStockQuantity() : java.math.BigDecimal.ZERO;
                            java.math.BigDecimal currentReserved = p.getReservedQuantity() != null ? p.getReservedQuantity() : java.math.BigDecimal.ZERO;
                            java.math.BigDecimal currentSold = p.getTotalSold() != null ? p.getTotalSold() : java.math.BigDecimal.ZERO;

                            java.math.BigDecimal newStock = currentStock.subtract(qty);
                            if (newStock.compareTo(java.math.BigDecimal.ZERO) < 0) newStock = java.math.BigDecimal.ZERO;
                            
                            java.math.BigDecimal newReserved = currentReserved.subtract(qty);
                            if (newReserved.compareTo(java.math.BigDecimal.ZERO) < 0) newReserved = java.math.BigDecimal.ZERO;

                            p.setStockQuantity(newStock);
                            p.setReservedQuantity(newReserved);
                            p.setTotalSold(currentSold.add(qty));
                            em.merge(p);

                            // Record SALE InventoryTransaction
                            InventoryTransaction tx = new InventoryTransaction();
                            tx.setProduct(p);
                            tx.setUser(order.getUser());
                            tx.setSupplier(p.getSupplier());
                            tx.setQuantityChanged(qty.negate());
                            tx.setUnitCost(detail.getCostPrice());
                            tx.setTransactionType("SALE");
                            tx.setReason("Xuất kho bán hàng - Giao thành công đơn " + order.getOrderCode());
                            tx.setCreatedAt(new Date());
                            em.persist(tx);
                        }
                    }
                }
            }
            order.setPaymentStatus("PAID");

        } else if ("CANCELLED".equals(newStatus)) {
            // Cancel: Release reserved stock (Physical stock was never deducted during PENDING/CONFIRMED/PROCESSING)
            if (order.getOrderDetails() != null) {
                for (OrderDetail detail : order.getOrderDetails()) {
                    if (detail.getProduct() != null) {
                        Product p = em.find(Product.class, detail.getProduct().getId(), LockModeType.PESSIMISTIC_WRITE);
                        if (p != null) {
                            java.math.BigDecimal qty = detail.getQuantity() != null ? detail.getQuantity() : java.math.BigDecimal.ZERO;
                            java.math.BigDecimal currentReserved = p.getReservedQuantity() != null ? p.getReservedQuantity() : java.math.BigDecimal.ZERO;
                            java.math.BigDecimal newReserved = currentReserved.subtract(qty);
                            if (newReserved.compareTo(java.math.BigDecimal.ZERO) < 0) newReserved = java.math.BigDecimal.ZERO;
                            p.setReservedQuantity(newReserved);
                            em.merge(p);
                        }
                    }
                }
            }
            // If a coupon was applied, restore 1 usage quota
            if (order.getCouponCode() != null && !order.getCouponCode().trim().isEmpty()) {
                List<Coupon> coupons = em.createQuery("SELECT c FROM Coupon c WHERE UPPER(c.code) = :code", Coupon.class)
                                         .setParameter("code", order.getCouponCode().trim().toUpperCase())
                                         .getResultList();
                if (!coupons.isEmpty()) {
                    Coupon c = coupons.get(0);
                    int used = c.getUsedCount() != null ? c.getUsedCount() : 0;
                    c.setUsedCount(Math.max(0, used - 1));
                    em.merge(c);
                }
            }
            if ("PAID".equals(order.getPaymentStatus())) {
                order.setPaymentStatus("REFUND_PENDING");
            }

        } else if ("CONFIRMED".equals(newStatus) || "PROCESSING".equals(newStatus) || "SHIPPED".equals(newStatus)) {
            // Moving along active fulfillment pipeline - stock remains safely in RESERVED state
        } else if ("RETURN_REQUESTED".equals(newStatus)) {
            if (!"DELIVERED".equals(currentStatus)) {
                throw new Exception("Chỉ có thể yêu cầu trả hàng cho đơn hàng đã giao thành công (DELIVERED).");
            }
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(new Date());
        em.merge(order);

        // Audit Log
        if (auditLogService != null) {
            auditLogService.log(null, "UPDATE_ORDER_STATUS", "Orders", order.getId(),
                "status=" + currentStatus, "status=" + newStatus);
        }

        // Notify customer via email asynchronously (Jakarta Mail)
        try {
            if (emailService != null) {
                emailService.sendOrderStatusEmail(order, newStatus);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void processReturn(Long orderId, boolean restockToInventory, String returnReason) throws Exception {
        Orders order = orderRepository.findByIdWithDetails(orderId);
        if (order == null) throw new Exception("Không tìm thấy đơn hàng.");

        String currentStatus = order.getStatus();
        if (!"DELIVERED".equals(currentStatus) && !"RETURN_REQUESTED".equals(currentStatus)) {
            throw new Exception("Đơn hàng phải ở trạng thái ĐÃ GIAO hoặc YÊU CẦU TRẢ HÀNG mới có thể xử lý trả hàng/hoàn tiền.");
        }

        if (order.getOrderDetails() != null) {
            for (OrderDetail detail : order.getOrderDetails()) {
                if (detail.getProduct() != null) {
                    Product p = em.find(Product.class, detail.getProduct().getId(), LockModeType.PESSIMISTIC_WRITE);
                    if (p != null) {
                        BigDecimal qty = detail.getQuantity() != null ? detail.getQuantity() : BigDecimal.ZERO;
                        BigDecimal currentSold = p.getTotalSold() != null ? p.getTotalSold() : BigDecimal.ZERO;
                        BigDecimal newSold = currentSold.subtract(qty);
                        if (newSold.compareTo(BigDecimal.ZERO) < 0) newSold = BigDecimal.ZERO;
                        p.setTotalSold(newSold);

                        if (restockToInventory) {
                            // Good condition agricultural products: restock to inventory
                            BigDecimal currentStock = p.getStockQuantity() != null ? p.getStockQuantity() : BigDecimal.ZERO;
                            p.setStockQuantity(currentStock.add(qty));

                            InventoryTransaction tx = new InventoryTransaction();
                            tx.setProduct(p);
                            tx.setUser(order.getUser());
                            tx.setSupplier(p.getSupplier());
                            tx.setQuantityChanged(qty);
                            tx.setUnitCost(detail.getCostPrice());
                            tx.setTransactionType("RETURN");
                            tx.setReason("Nhập lại kho hàng trả - " + (returnReason != null ? returnReason : "Khách trả hàng"));
                            tx.setCreatedAt(new Date());
                            em.persist(tx);
                        } else {
                            // Damaged / Perished goods: do NOT restock, record DAMAGE write-off
                            InventoryTransaction tx = new InventoryTransaction();
                            tx.setProduct(p);
                            tx.setUser(order.getUser());
                            tx.setSupplier(p.getSupplier());
                            tx.setQuantityChanged(BigDecimal.ZERO);
                            tx.setUnitCost(detail.getCostPrice());
                            tx.setTransactionType("DAMAGE");
                            tx.setReason("Hàng nông sản trả về bị hư hỏng/hủy bỏ - " + (returnReason != null ? returnReason : "Hàng dập/hỏng"));
                            tx.setCreatedAt(new Date());
                            em.persist(tx);
                        }
                        em.merge(p);
                    }
                }
            }
        }

        order.setStatus("RETURNED");
        order.setPaymentStatus("REFUNDED");
        order.setUpdatedAt(new Date());
        em.merge(order);
    }

    @Override
    public List<OrderDTO> getCustomerOrderHistory(Long userId) {
        List<Orders> orders = orderRepository.getOrdersByUserId(userId);
        List<OrderDTO> dtoList = new ArrayList<>();
        
        for (Orders order : orders) {
            List<OrderDetailDTO> details = new ArrayList<>();
            if (order.getOrderDetails() != null) {
                for (OrderDetail detail : order.getOrderDetails()) {
                    details.add(convertToDetailDTO(detail));
                }
            }
            dtoList.add(convertToOrderDTO(order, details));
        }
        
        return dtoList;
    }

    private OrderDetailDTO convertToDetailDTO(OrderDetail detail) {
        OrderDetailDTO d = new OrderDetailDTO();
        d.setId(detail.getId());
        if (detail.getProduct() != null) {
            d.setProductId(detail.getProduct().getId());
            d.setImageUrl(detail.getProduct().getImageUrl());
        }
        // Strict Snapshot Read: Prefer OrderDetail snapshot values over living Product state
        d.setProductCode(detail.getProductCode() != null ? detail.getProductCode() : 
            (detail.getProduct() != null ? detail.getProduct().getProductCode() : "N/A"));
        d.setProductName(detail.getProductName());
        d.setUnit(detail.getUnit() != null ? detail.getUnit() : 
            (detail.getProduct() != null ? detail.getProduct().getUnit() : "kg"));
        d.setUnitPrice(detail.getUnitPrice());
        d.setCostPrice(detail.getCostPrice());
        d.setQuantity(detail.getQuantity());
        return d;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void cancelOrderByCustomer(Long orderId, Long userId) throws Exception {
        if (orderId == null || userId == null) {
            throw new Exception("Dữ liệu không hợp lệ.");
        }
        Orders order = em.find(Orders.class, orderId);
        if (order == null) {
            throw new Exception("Không tìm thấy đơn hàng cần hủy.");
        }
        // Strict IDOR Check
        if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
            throw new Exception("Bạn không có quyền thao tác trên đơn hàng này.");
        }
        if (!"PENDING".equalsIgnoreCase(order.getStatus())) {
            throw new Exception("Chỉ có thể tự hủy đơn hàng khi đơn còn ở trạng thái Chờ xác nhận (PENDING). Với đơn đã xác nhận, vui lòng gửi yêu cầu hủy.");
        }
        // Reuse status update with full release of reserved stock & coupon restoration
        updateOrderStatus(orderId, "CANCELLED");

        if (auditLogService != null) {
            auditLogService.log(userId, "CUSTOMER_CANCEL_ORDER", "Orders", orderId,
                "status=PENDING", "status=CANCELLED");
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void requestOrderCancellation(Long orderId, Long userId, String reason) throws Exception {
        if (orderId == null || userId == null) {
            throw new Exception("Dữ liệu không hợp lệ.");
        }
        String cleanReason = reason != null ? reason.trim() : "";
        if (cleanReason.isEmpty()) {
            throw new Exception("Vui lòng cung cấp lý do hủy đơn hàng.");
        }
        Orders order = em.find(Orders.class, orderId);
        if (order == null) {
            throw new Exception("Không tìm thấy đơn hàng.");
        }
        // Strict IDOR Check
        if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
            throw new Exception("Bạn không có quyền thao tác trên đơn hàng này.");
        }
        String currentStatus = order.getStatus();
        if (!"CONFIRMED".equalsIgnoreCase(currentStatus) && !"PROCESSING".equalsIgnoreCase(currentStatus)) {
            throw new Exception("Chỉ có thể gửi yêu cầu hủy cho đơn hàng đang ở trạng thái ĐÃ XÁC NHẬN hoặc ĐANG XỬ LÝ.");
        }
        // Check if there is already a PENDING request
        List<CancellationRequest> pendingList = em.createQuery(
            "SELECT c FROM CancellationRequest c WHERE c.order.id = :orderId AND c.status = 'PENDING'", CancellationRequest.class)
            .setParameter("orderId", orderId)
            .getResultList();
        if (!pendingList.isEmpty()) {
            throw new Exception("Đơn hàng này đã có một yêu cầu hủy đang chờ Quản trị viên xét duyệt.");
        }

        CancellationRequest req = new CancellationRequest();
        req.setOrder(order);
        req.setUser(order.getUser());
        req.setReason(cleanReason);
        req.setStatus("PENDING");
        req.setCreatedAt(new Date());
        em.persist(req);

        if (auditLogService != null) {
            auditLogService.log(userId, "REQUEST_CANCEL_ORDER", "CancellationRequests", req.getId(),
                "none", "reason=" + cleanReason);
        }
    }

    @Override
    public List<CancellationRequestDTO> getPendingCancellationRequests() {
        List<CancellationRequest> list = em.createQuery(
            "SELECT c FROM CancellationRequest c JOIN FETCH c.order o JOIN FETCH c.user u WHERE c.status = 'PENDING' ORDER BY c.createdAt ASC", CancellationRequest.class)
            .getResultList();
        List<CancellationRequestDTO> dtoList = new ArrayList<>();
        if (list != null) {
            for (CancellationRequest r : list) {
                CancellationRequestDTO dto = new CancellationRequestDTO();
                dto.setId(r.getId());
                dto.setOrderId(r.getOrder().getId());
                dto.setOrderCode(r.getOrder().getOrderCode());
                dto.setUserId(r.getUser().getId());
                dto.setCustomerName(r.getUser().getFullName());
                dto.setPhone(r.getOrder().getPhone());
                dto.setTotalAmount(r.getOrder().getTotalAmount());
                dto.setReason(r.getReason());
                dto.setStatus(r.getStatus());
                dto.setAdminNote(r.getAdminNote());
                dto.setCreatedAt(r.getCreatedAt());
                dto.setProcessedAt(r.getProcessedAt());
                dtoList.add(dto);
            }
        }
        return dtoList;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void approveCancellationRequest(Long requestId, String adminNote) throws Exception {
        if (requestId == null) throw new Exception("Mã yêu cầu không hợp lệ.");
        CancellationRequest req = em.find(CancellationRequest.class, requestId);
        if (req == null) throw new Exception("Không tìm thấy yêu cầu hủy đơn.");
        if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
            throw new Exception("Yêu cầu này đã được xử lý trước đó.");
        }

        req.setStatus("APPROVED");
        req.setAdminNote(adminNote != null ? adminNote.trim() : "Quản trị viên đã chấp thuận yêu cầu hủy đơn.");
        req.setProcessedAt(new Date());
        em.merge(req);

        // Cancel the order (releases reserved stock, restores coupon, sets REFUND_PENDING if paid)
        updateOrderStatus(req.getOrder().getId(), "CANCELLED");

        if (auditLogService != null) {
            auditLogService.log(null, "APPROVE_CANCEL_REQUEST", "CancellationRequests", requestId,
                "status=PENDING", "status=APPROVED, note=" + adminNote);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void rejectCancellationRequest(Long requestId, String adminNote) throws Exception {
        if (requestId == null) throw new Exception("Mã yêu cầu không hợp lệ.");
        CancellationRequest req = em.find(CancellationRequest.class, requestId);
        if (req == null) throw new Exception("Không tìm thấy yêu cầu hủy đơn.");
        if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
            throw new Exception("Yêu cầu này đã được xử lý trước đó.");
        }

        req.setStatus("REJECTED");
        req.setAdminNote(adminNote != null ? adminNote.trim() : "Đơn hàng đang chuẩn bị hoặc đã đóng gói, không thể hủy.");
        req.setProcessedAt(new Date());
        em.merge(req);

        if (auditLogService != null) {
            auditLogService.log(null, "REJECT_CANCEL_REQUEST", "CancellationRequests", requestId,
                "status=PENDING", "status=REJECTED, note=" + adminNote);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void confirmRefund(Long orderId, String adminNote) throws Exception {
        if (orderId == null) throw new Exception("Mã đơn hàng không hợp lệ.");
        Orders order = em.find(Orders.class, orderId);
        if (order == null) throw new Exception("Không tìm thấy đơn hàng.");
        if (!"REFUND_PENDING".equalsIgnoreCase(order.getPaymentStatus())) {
            throw new Exception("Đơn hàng không ở trạng thái Chờ hoàn tiền (REFUND_PENDING).");
        }

        order.setPaymentStatus("REFUNDED");
        order.setUpdatedAt(new Date());
        em.merge(order);

        if (auditLogService != null) {
            auditLogService.log(null, "CONFIRM_REFUND", "Orders", orderId,
                "payment_status=REFUND_PENDING", "payment_status=REFUNDED, note=" + adminNote);
        }

        // Notify customer via email
        try {
            if (emailService != null) {
                emailService.sendOrderStatusEmail(order, "REFUNDED");
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public List<OrderDTO> getRefundOrders() {
        List<Orders> orders = em.createQuery(
            "SELECT o FROM Orders o WHERE o.paymentStatus IN ('REFUND_PENDING', 'REFUNDED') ORDER BY o.updatedAt DESC", Orders.class)
            .getResultList();
        List<OrderDTO> dtoList = new ArrayList<>();
        if (orders != null) {
            for (Orders o : orders) {
                dtoList.add(convertToOrderDTO(o, new ArrayList<>()));
            }
        }
        return dtoList;
    }

    private OrderDTO convertToOrderDTO(Orders order, List<OrderDetailDTO> details) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderCode(order.getOrderCode());
        dto.setSubtotal(order.getSubtotal());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setShippingFee(order.getShippingFee());
        dto.setCouponCode(order.getCouponCode());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setPhone(order.getPhone());
        dto.setCreatedAt(order.getCreatedAt());
        if (order.getUser() != null) {
            dto.setCustomerName(order.getUser().getFullName());
        } else {
            dto.setCustomerName("Khách vãng lai");
        }

        try {
            Long count = em.createQuery("SELECT COUNT(c) FROM CancellationRequest c WHERE c.order.id = :orderId AND c.status = 'PENDING'", Long.class)
                           .setParameter("orderId", order.getId())
                           .getSingleResult();
            dto.setCancellationRequested(count != null && count > 0);
        } catch (Exception e) {
            dto.setCancellationRequested(false);
        }

        dto.setOrderDetails(details);
        return dto;
    }

    /**
     * SePay webhook: Xác nhận thanh toán theo order_code.
     * Chỉ cập nhật nếu đơn tồn tại, chưa bị CANCELLED và payment_status != PAID.
     * @return true nếu cập nhật thành công, false nếu không tìm thấy hoặc đã PAID.
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public boolean confirmPaymentByOrderCode(String orderCode) throws Exception {
        if (orderCode == null || orderCode.isBlank()) return false;
        List<Orders> found = em.createQuery(
                "SELECT o FROM Orders o WHERE o.orderCode = :code", Orders.class)
                .setParameter("code", orderCode.trim())
                .getResultList();
        if (found.isEmpty()) return false;
        Orders order = found.get(0);
        if ("CANCELLED".equalsIgnoreCase(order.getStatus())) return false;
        if ("PAID".equalsIgnoreCase(order.getPaymentStatus())) return true; // idempotent
        order.setPaymentStatus("PAID");
        order.setUpdatedAt(new Date());
        em.merge(order);
        // Audit log
        try {
            auditLogService.log(null, "VIETQR_PAYMENT_CONFIRMED", "Orders", order.getId(),
                    "{\"payment_status\":\"UNPAID\"}", "{\"payment_status\":\"PAID\",\"via\":\"SePay\"}");
        } catch (Exception ex) { /* non-critical */ }
        return true;
    }

    /**
     * Checkout polling: client hỏi trạng thái thanh toán của đơn hàng đang chờ.
     * @return payment_status string ("UNPAID", "PAID", v.v.) hoặc null nếu không tìm thấy.
     */
    @Override
    public String getPaymentStatusByOrderCode(String orderCode) {
        if (orderCode == null || orderCode.isBlank()) return null;
        try {
            return em.createQuery(
                    "SELECT o.paymentStatus FROM Orders o WHERE o.orderCode = :code", String.class)
                    .setParameter("code", orderCode.trim())
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
}

