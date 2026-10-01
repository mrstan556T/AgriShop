package com.agrishop.service;

import com.agrishop.dto.CouponDTO;
import com.agrishop.entity.Coupon;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Stateless
public class CouponService implements CouponServiceLocal {

    @PersistenceContext(unitName = "AgriShopPU")
    private EntityManager em;

    @Override
    public List<CouponDTO> getCouponsLazy(int first, int pageSize, String sortField, String sortOrder, Map<String, Object> filters) {
        StringBuilder jpql = new StringBuilder("SELECT c FROM Coupon c WHERE 1=1");
        applyFilters(jpql, filters);

        if (sortField != null && !sortField.isEmpty()) {
            String dir = "DESC".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";
            jpql.append(" ORDER BY c.").append(sortField).append(" ").append(dir);
        } else {
            jpql.append(" ORDER BY c.id DESC");
        }

        TypedQuery<Coupon> query = em.createQuery(jpql.toString(), Coupon.class);
        setFilterParameters(query, filters);
        query.setFirstResult(first);
        query.setMaxResults(pageSize);

        List<Coupon> list = query.getResultList();
        List<CouponDTO> dtoList = new ArrayList<>();
        for (Coupon c : list) {
            dtoList.add(toDTO(c));
        }
        return dtoList;
    }

    @Override
    public int countCoupons(Map<String, Object> filters) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(c) FROM Coupon c WHERE 1=1");
        applyFilters(jpql, filters);

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
        setFilterParameters(query, filters);
        Long count = query.getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public void createCoupon(CouponDTO dto) throws Exception {
        validateCoupon(dto, true);

        Coupon coupon = new Coupon();
        coupon.setCode(dto.getCode().trim().toUpperCase());
        coupon.setDiscountType(dto.getDiscountType());
        coupon.setDiscountValue(dto.getDiscountValue());
        coupon.setMinOrderValue(dto.getMinOrderValue() != null ? dto.getMinOrderValue() : BigDecimal.ZERO);
        coupon.setStartDate(dto.getStartDate());
        coupon.setEndDate(dto.getEndDate());
        coupon.setUsageLimit(dto.getUsageLimit() != null ? dto.getUsageLimit() : 100);
        coupon.setStatus("ACTIVE");

        em.persist(coupon);
    }

    @Override
    public void updateCoupon(CouponDTO dto) throws Exception {
        validateCoupon(dto, false);

        Coupon coupon = em.find(Coupon.class, dto.getId());
        if (coupon == null) throw new Exception("Không tìm thấy mã giảm giá.");

        coupon.setDiscountType(dto.getDiscountType());
        coupon.setDiscountValue(dto.getDiscountValue());
        coupon.setMinOrderValue(dto.getMinOrderValue() != null ? dto.getMinOrderValue() : BigDecimal.ZERO);
        coupon.setStartDate(dto.getStartDate());
        coupon.setEndDate(dto.getEndDate());
        coupon.setUsageLimit(dto.getUsageLimit());
        if (dto.getStatus() != null) {
            coupon.setStatus(dto.getStatus());
        }
        coupon.setUpdatedAt(new Date());

        em.merge(coupon);
    }

    @Override
    public void toggleCouponStatus(Long couponId) throws Exception {
        Coupon c = em.find(Coupon.class, couponId);
        if (c == null) throw new Exception("Không tìm thấy mã giảm giá.");
        if ("ACTIVE".equalsIgnoreCase(c.getStatus())) {
            c.setStatus("INACTIVE");
        } else {
            c.setStatus("ACTIVE");
        }
        c.setUpdatedAt(new Date());
        em.merge(c);
    }

    @Override
    public CouponDTO getCouponById(Long id) {
        Coupon c = em.find(Coupon.class, id);
        return c != null ? toDTO(c) : null;
    }

    @Override
    public CouponDTO getCouponByCode(String code) {
        if (code == null || code.trim().isEmpty()) return null;
        try {
            List<Coupon> list = em.createQuery("SELECT c FROM Coupon c WHERE UPPER(c.code) = :code", Coupon.class)
                                  .setParameter("code", code.trim().toUpperCase())
                                  .getResultList();
            return list.isEmpty() ? null : toDTO(list.get(0));
        } catch (Exception e) {
            return null;
        }
    }

    private void validateCoupon(CouponDTO dto, boolean isNew) throws Exception {
        if (dto.getCode() == null || dto.getCode().trim().isEmpty()) {
            throw new Exception("Mã khuyến mãi không được để trống.");
        }
        String cleanCode = dto.getCode().trim().toUpperCase();
        if (isNew) {
            Long exists = em.createQuery("SELECT COUNT(c) FROM Coupon c WHERE UPPER(c.code) = :code", Long.class)
                            .setParameter("code", cleanCode)
                            .getSingleResult();
            if (exists != null && exists > 0) {
                throw new Exception("Mã khuyến mãi '" + cleanCode + "' đã tồn tại trong hệ thống.");
            }
        }
        if (dto.getDiscountValue() == null || dto.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Giá trị giảm giá phải lớn hơn 0.");
        }
        if ("PERCENTAGE".equalsIgnoreCase(dto.getDiscountType())) {
            if (dto.getDiscountValue().compareTo(new BigDecimal(100)) > 0) {
                throw new Exception("Mức giảm theo phần trăm không được vượt quá 100%.");
            }
        }
        if (dto.getStartDate() == null || dto.getEndDate() == null) {
            throw new Exception("Vui lòng chọn ngày bắt đầu và ngày kết thúc.");
        }
        if (dto.getEndDate().before(dto.getStartDate())) {
            throw new Exception("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu.");
        }
    }

    private CouponDTO toDTO(Coupon c) {
        CouponDTO dto = new CouponDTO();
        dto.setId(c.getId());
        dto.setCode(c.getCode());
        dto.setDiscountType(c.getDiscountType());
        dto.setDiscountValue(c.getDiscountValue());
        dto.setMinOrderValue(c.getMinOrderValue());
        dto.setStartDate(c.getStartDate());
        dto.setEndDate(c.getEndDate());
        dto.setUsageLimit(c.getUsageLimit());
        dto.setUsedCount(c.getUsedCount());
        dto.setStatus(c.getStatus());
        dto.setCreatedAt(c.getCreatedAt());

        // Check if expired
        Date now = new Date();
        if ("ACTIVE".equalsIgnoreCase(c.getStatus()) && c.getEndDate() != null && c.getEndDate().before(now)) {
            dto.setStatus("EXPIRED");
        }
        return dto;
    }

    private void applyFilters(StringBuilder jpql, Map<String, Object> filters) {
        if (filters != null) {
            if (filters.containsKey("globalFilter") && filters.get("globalFilter") != null) {
                jpql.append(" AND UPPER(c.code) LIKE :globalFilter");
            }
            if (filters.containsKey("status") && filters.get("status") != null && !filters.get("status").toString().isEmpty()) {
                jpql.append(" AND c.status = :status");
            }
        }
    }

    private void setFilterParameters(TypedQuery<?> query, Map<String, Object> filters) {
        if (filters != null) {
            if (filters.containsKey("globalFilter") && filters.get("globalFilter") != null) {
                query.setParameter("globalFilter", "%" + filters.get("globalFilter").toString().trim().toUpperCase() + "%");
            }
            if (filters.containsKey("status") && filters.get("status") != null && !filters.get("status").toString().isEmpty()) {
                query.setParameter("status", filters.get("status").toString());
            }
        }
    }
}
