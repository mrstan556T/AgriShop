package com.agrishop.service;

import com.agrishop.dto.OrderDTO;
import com.agrishop.entity.Cart;
import com.agrishop.entity.CartItem;
import com.agrishop.entity.Coupon;
import com.agrishop.entity.OrderDetail;
import com.agrishop.entity.Orders;
import com.agrishop.entity.Product;
import com.agrishop.entity.User;
import com.agrishop.repository.CartRepositoryLocal;
import com.agrishop.repository.OrderRepositoryLocal;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Stateless
public class CheckoutService implements CheckoutServiceLocal {

    @PersistenceContext(unitName = "AgriShopPU")
    private EntityManager em;

    @EJB
    private CartRepositoryLocal cartRepository;

    @EJB
    private OrderRepositoryLocal orderRepository;

    @EJB
    private SystemSettingServiceLocal systemSettingService;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public OrderDTO placeOrder(Long userId, String shippingAddress, String phone) throws Exception {
        return placeOrder(userId, shippingAddress, phone, null, "COD");
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public OrderDTO placeOrder(Long userId, String shippingAddress, String phone, String couponCode) throws Exception {
        return placeOrder(userId, shippingAddress, phone, couponCode, "COD");
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public OrderDTO placeOrder(Long userId, String shippingAddress, String phone, String couponCode, String paymentMethod) throws Exception {
        Cart cart = cartRepository.findByUserId(userId);
        
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new Exception("Giỏ hàng của bạn đang trống.");
        }

        User user = em.find(User.class, userId);
        if (user == null) {
            throw new Exception("Tài khoản người dùng không hợp lệ.");
        }
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new com.agrishop.exception.BusinessException("Bạn đang xem bằng tài khoản Quản trị viên — không thể thực hiện đặt hàng trên Storefront.");
        }

        Orders order = new Orders();
        order.setOrderCode(orderRepository.generateOrderCode());
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setPhone(phone);
        order.setStatus("PENDING");
        order.setPaymentStatus("UNPAID");
        order.setPaymentMethod(paymentMethod != null && !paymentMethod.trim().isEmpty() ? paymentMethod.trim().toUpperCase() : "COD");
        order.setOrderDetails(new ArrayList<>());
        
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            // Pessimistic write lock on Product ensures atomic stock verification & reservation
            Product product = em.find(Product.class, cartItem.getProduct().getId(), LockModeType.PESSIMISTIC_WRITE);
            
            if (product == null || (product.getIsDeleted() != null && product.getIsDeleted())) {
                throw new Exception("Sản phẩm '" + (product != null ? product.getName() : "ID: " + cartItem.getProduct().getId()) + "' hiện không còn kinh doanh.");
            }

            BigDecimal stock = product.getStockQuantity() != null ? product.getStockQuantity() : BigDecimal.ZERO;
            BigDecimal reserved = product.getReservedQuantity() != null ? product.getReservedQuantity() : BigDecimal.ZERO;
            BigDecimal available = stock.subtract(reserved);

            if (cartItem.getQuantity().compareTo(available) > 0) {
                throw new Exception("Sản phẩm '" + product.getName() + "' không đủ số lượng tồn khả dụng (Còn lại: " + (available.compareTo(BigDecimal.ZERO) > 0 ? available : BigDecimal.ZERO) + " " + product.getUnit() + ").");
            }
            
            // Tri-State Inventory Model: Reserve stock atomically (Physical stock is deducted upon fulfillment/delivery)
            product.setReservedQuantity(reserved.add(cartItem.getQuantity()));
            em.merge(product);
            
            // Full immutable OrderDetail snapshot
            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(product);
            detail.setProductCode(product.getProductCode()); // Snapshot SKU
            detail.setProductName(product.getName());        // Snapshot Name
            detail.setUnit(product.getUnit());               // Snapshot Unit
            detail.setUnitPrice(product.getPrice());         // Snapshot Unit Price
            detail.setCostPrice(product.getCostPrice() != null ? product.getCostPrice() : BigDecimal.ZERO); // Snapshot Cost Price
            detail.setQuantity(cartItem.getQuantity());
            
            order.getOrderDetails().add(detail);
            
            // Accumulate subtotal
            BigDecimal itemTotal = product.getPrice().multiply(cartItem.getQuantity());
            subtotal = subtotal.add(itemTotal);
        }
        
        // Calculate Discount from Coupon if provided
        BigDecimal discountAmount = BigDecimal.ZERO;
        String appliedCouponCode = null;

        if (couponCode != null && !couponCode.trim().isEmpty()) {
            String cleanCode = couponCode.trim().toUpperCase();
            List<Coupon> coupons = em.createQuery("SELECT c FROM Coupon c WHERE UPPER(c.code) = :code", Coupon.class)
                                     .setParameter("code", cleanCode)
                                     .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                                     .getResultList();
            if (coupons.isEmpty()) {
                throw new Exception("Mã giảm giá '" + cleanCode + "' không tồn tại.");
            }
            Coupon coupon = coupons.get(0);

            if (!"ACTIVE".equalsIgnoreCase(coupon.getStatus())) {
                throw new Exception("Mã giảm giá '" + cleanCode + "' hiện không còn hiệu lực.");
            }

            Date now = new Date();
            if (coupon.getStartDate() != null && now.before(coupon.getStartDate())) {
                throw new Exception("Mã giảm giá '" + cleanCode + "' chưa đến thời gian áp dụng.");
            }
            if (coupon.getEndDate() != null && now.after(coupon.getEndDate())) {
                throw new Exception("Mã giảm giá '" + cleanCode + "' đã hết hạn sử dụng.");
            }

            int usedCount = coupon.getUsedCount() != null ? coupon.getUsedCount() : 0;
            if (coupon.getUsageLimit() != null && usedCount >= coupon.getUsageLimit()) {
                throw new Exception("Mã giảm giá '" + cleanCode + "' đã hết lượt sử dụng.");
            }

            if (coupon.getMinOrderValue() != null && subtotal.compareTo(coupon.getMinOrderValue()) < 0) {
                throw new Exception("Đơn hàng chưa đạt giá trị tối thiểu " + coupon.getMinOrderValue() + " đ để áp dụng mã giảm giá.");
            }

            if ("PERCENTAGE".equalsIgnoreCase(coupon.getDiscountType())) {
                discountAmount = subtotal.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
            } else {
                discountAmount = coupon.getDiscountValue();
            }

            if (discountAmount.compareTo(subtotal) > 0) {
                discountAmount = subtotal;
            }

            // Atomically increment used count
            coupon.setUsedCount(usedCount + 1);
            em.merge(coupon);
            appliedCouponCode = coupon.getCode();
        }

        // Shipping fee calculation: Dynamic lookup from SystemSettings with sensible fallback defaults
        BigDecimal threshold = systemSettingService != null 
            ? systemSettingService.getBigDecimalSetting("FREE_SHIPPING_THRESHOLD", new BigDecimal("300000")) 
            : new BigDecimal("300000");
        BigDecimal standardFee = systemSettingService != null 
            ? systemSettingService.getBigDecimalSetting("SHIPPING_FEE", new BigDecimal("30000")) 
            : new BigDecimal("30000");

        BigDecimal shippingFee = subtotal.compareTo(threshold) >= 0 ? BigDecimal.ZERO : standardFee;

        // Grand total calculation: Subtotal - Discount + Shipping Fee
        BigDecimal totalAmount = subtotal.subtract(discountAmount).add(shippingFee);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        order.setSubtotal(subtotal);
        order.setDiscountAmount(discountAmount);
        order.setShippingFee(shippingFee);
        order.setCouponCode(appliedCouponCode);
        order.setTotalAmount(totalAmount);

        orderRepository.createOrder(order);
        
        // Clear Cart
        cartRepository.deleteCartItemsByCartId(cart.getId());
        
        // Return DTO
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
        
        return dto;
    }
}
