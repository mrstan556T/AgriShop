package com.agrishop.service;

import com.agrishop.dto.ReviewDTO;
import com.agrishop.entity.Product;
import com.agrishop.entity.Review;
import com.agrishop.entity.User;
import com.agrishop.exception.BusinessException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Stateless
public class ReviewService implements ReviewServiceLocal {

    @PersistenceContext(unitName = "AgriShopPU")
    private EntityManager em;

    @EJB
    private AuditLogServiceLocal auditLogService;

    @Override
    public List<ReviewDTO> getReviewsLazy(int first, int pageSize, String sortField, String sortOrder, Map<String, Object> filters) {
        StringBuilder jpql = new StringBuilder("SELECT r FROM Review r JOIN FETCH r.product p JOIN FETCH r.user u WHERE 1=1");
        applyFilters(jpql, filters);

        if (sortField != null && !sortField.isEmpty()) {
            String dir = "DESC".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";
            jpql.append(" ORDER BY r.").append(sortField).append(" ").append(dir);
        } else {
            jpql.append(" ORDER BY r.id DESC");
        }

        TypedQuery<Review> query = em.createQuery(jpql.toString(), Review.class);
        setFilterParameters(query, filters);
        query.setFirstResult(first);
        query.setMaxResults(pageSize);

        List<Review> list = query.getResultList();
        List<ReviewDTO> dtoList = new ArrayList<>();
        for (Review r : list) {
            dtoList.add(toDTO(r));
        }
        return dtoList;
    }

    @Override
    public int countReviews(Map<String, Object> filters) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(r) FROM Review r WHERE 1=1");
        applyFilters(jpql, filters);

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
        setFilterParameters(query, filters);
        Long count = query.getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public void toggleReviewStatus(Long reviewId) throws Exception {
        Review r = em.find(Review.class, reviewId);
        if (r == null) throw new Exception("Không tìm thấy đánh giá.");
        if ("HIDDEN".equalsIgnoreCase(r.getStatus())) {
            r.setStatus("APPROVED");
        } else {
            r.setStatus("HIDDEN");
        }
        r.setUpdatedAt(new Date());
        em.merge(r);
    }

    @Override
    public void approveReview(Long reviewId) throws Exception {
        Review r = em.find(Review.class, reviewId);
        if (r == null) throw new Exception("Không tìm thấy đánh giá.");
        r.setStatus("APPROVED");
        r.setUpdatedAt(new Date());
        em.merge(r);
    }

    @Override
    public void hideReview(Long reviewId) throws Exception {
        Review r = em.find(Review.class, reviewId);
        if (r == null) throw new Exception("Không tìm thấy đánh giá.");
        r.setStatus("HIDDEN");
        r.setUpdatedAt(new Date());
        em.merge(r);
    }

    @Override
    public void replyReview(Long reviewId, String reply, Long adminId) throws Exception {
        Review r = em.find(Review.class, reviewId);
        if (r == null) throw new Exception("Không tìm thấy đánh giá.");
        String oldReply = r.getAdminReply();
        r.setAdminReply(reply != null ? reply.trim() : null);
        r.setAdminReplyAt(new Date());
        r.setUpdatedAt(new Date());
        em.merge(r);

        if (auditLogService != null) {
            auditLogService.log(adminId, "REPLY_REVIEW", "Reviews", reviewId, 
                oldReply != null ? "reply=" + oldReply : "none", 
                "reply=" + (reply != null ? reply.trim() : ""));
        }
    }

    @Override
    public List<ReviewDTO> getApprovedReviewsByProduct(Long productId) {
        List<Review> list = em.createQuery("SELECT r FROM Review r JOIN FETCH r.user u WHERE r.product.id = :pId AND (r.status = 'APPROVED' OR r.status = 'VISIBLE' OR r.status IS NULL) ORDER BY r.createdAt DESC", Review.class)
                              .setParameter("pId", productId)
                              .getResultList();
        List<ReviewDTO> dtoList = new ArrayList<>();
        for (Review r : list) {
            dtoList.add(toDTO(r));
        }
        return dtoList;
    }

    @Override
    public ReviewDTO createReview(Long userId, Long productId, int rating, String comment) throws Exception {
        if (userId == null) {
            throw new BusinessException("Vui lòng đăng nhập để thực hiện đánh giá sản phẩm.");
        }
        User user = em.find(User.class, userId);
        if (user == null) {
            throw new BusinessException("Tài khoản người dùng không hợp lệ.");
        }
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new BusinessException("Tài khoản Quản trị viên không được phép viết đánh giá sản phẩm.");
        }

        if (productId == null) {
            throw new BusinessException("Sản phẩm không hợp lệ.");
        }
        Product product = em.find(Product.class, productId);
        if (product == null) {
            throw new BusinessException("Sản phẩm không tồn tại.");
        }
        if (Boolean.TRUE.equals(product.getIsDeleted())) {
            throw new BusinessException("Sản phẩm này hiện đã ngừng kinh doanh.");
        }

        if (rating < 1 || rating > 5) {
            throw new BusinessException("Điểm đánh giá phải từ 1 đến 5 sao.");
        }

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(rating);
        review.setComment(comment != null ? comment.trim() : "");
        review.setStatus("VISIBLE");
        review.setCreatedAt(new Date());
        review.setUpdatedAt(new Date());

        em.persist(review);
        em.flush();

        return toDTO(review);
    }

    @Override
    public ReviewDTO addReview(Long userId, Long productId, int rating, String comment) throws Exception {
        return createReview(userId, productId, rating, comment);
    }

    @Override
    public ReviewDTO createReview(ReviewDTO dto) throws Exception {
        if (dto == null) {
            throw new BusinessException("Dữ liệu đánh giá không hợp lệ.");
        }
        return createReview(dto.getUserId(), dto.getProductId(), dto.getRating() != null ? dto.getRating() : 5, dto.getComment());
    }

    @Override
    public ReviewDTO updateReview(Long userId, Long reviewId, int rating, String comment) throws Exception {
        if (userId == null) {
            throw new BusinessException("Vui lòng đăng nhập để thao tác.");
        }
        User user = em.find(User.class, userId);
        if (user == null) {
            throw new BusinessException("Tài khoản người dùng không hợp lệ.");
        }
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new BusinessException("Tài khoản Quản trị viên không được phép viết đánh giá sản phẩm.");
        }

        if (reviewId == null) {
            throw new BusinessException("Mã đánh giá không hợp lệ.");
        }
        Review review = em.find(Review.class, reviewId);
        if (review == null) {
            throw new BusinessException("Đánh giá không tồn tại.");
        }

        if (review.getUser() == null || !review.getUser().getId().equals(userId)) {
            throw new BusinessException("Bạn không có quyền chỉnh sửa đánh giá này.");
        }

        if (rating < 1 || rating > 5) {
            throw new BusinessException("Điểm đánh giá phải từ 1 đến 5 sao.");
        }

        review.setRating(rating);
        review.setComment(comment != null ? comment.trim() : "");
        review.setUpdatedAt(new Date());
        em.merge(review);
        em.flush();

        return toDTO(review);
    }

    @Override
    public ReviewDTO updateReview(ReviewDTO dto) throws Exception {
        if (dto == null || dto.getId() == null) {
            throw new BusinessException("Dữ liệu cập nhật không hợp lệ.");
        }
        return updateReview(dto.getUserId(), dto.getId(), dto.getRating() != null ? dto.getRating() : 5, dto.getComment());
    }

    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    private ReviewDTO toDTO(Review r) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(r.getId());
        if (r.getProduct() != null) {
            dto.setProductId(r.getProduct().getId());
            dto.setProductCode(r.getProduct().getProductCode());
            dto.setProductName(r.getProduct().getName());
            dto.setProductImageUrl(r.getProduct().getImageUrl());
        }
        if (r.getUser() != null) {
            dto.setUserId(r.getUser().getId());
            dto.setUserName(r.getUser().getFullName() != null ? r.getUser().getFullName() : r.getUser().getUsername());
            dto.setUserEmail(r.getUser().getEmail());
        }
        dto.setRating(r.getRating());
        dto.setComment(r.getComment());
        dto.setStatus(r.getStatus() != null ? r.getStatus() : "APPROVED");
        dto.setAdminReply(r.getAdminReply());
        dto.setAdminReplyAt(r.getAdminReplyAt());
        dto.setCreatedAt(r.getCreatedAt());
        return dto;
    }

    private void applyFilters(StringBuilder jpql, Map<String, Object> filters) {
        if (filters != null) {
            if (filters.containsKey("globalFilter") && filters.get("globalFilter") != null) {
                jpql.append(" AND (LOWER(r.product.name) LIKE :globalFilter OR LOWER(r.user.fullName) LIKE :globalFilter OR LOWER(r.comment) LIKE :globalFilter)");
            }
            if (filters.containsKey("status") && filters.get("status") != null && !filters.get("status").toString().isEmpty()) {
                jpql.append(" AND r.status = :status");
            }
            if (filters.containsKey("rating") && filters.get("rating") != null) {
                jpql.append(" AND r.rating = :rating");
            }
        }
    }

    private void setFilterParameters(TypedQuery<?> query, Map<String, Object> filters) {
        if (filters != null) {
            if (filters.containsKey("globalFilter") && filters.get("globalFilter") != null) {
                query.setParameter("globalFilter", "%" + filters.get("globalFilter").toString().trim().toLowerCase() + "%");
            }
            if (filters.containsKey("status") && filters.get("status") != null && !filters.get("status").toString().isEmpty()) {
                query.setParameter("status", filters.get("status").toString());
            }
            if (filters.containsKey("rating") && filters.get("rating") != null) {
                query.setParameter("rating", Integer.parseInt(filters.get("rating").toString()));
            }
        }
    }
}
