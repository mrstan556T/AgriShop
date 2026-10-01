package com.agrishop.service;

import com.agrishop.dto.CouponDTO;
import jakarta.ejb.Local;
import java.util.List;
import java.util.Map;

@Local
public interface CouponServiceLocal {
    List<CouponDTO> getCouponsLazy(int first, int pageSize, String sortField, String sortOrder, Map<String, Object> filters);
    int countCoupons(Map<String, Object> filters);
    void createCoupon(CouponDTO dto) throws Exception;
    void updateCoupon(CouponDTO dto) throws Exception;
    void toggleCouponStatus(Long couponId) throws Exception;
    CouponDTO getCouponById(Long id);
    CouponDTO getCouponByCode(String code);
}
