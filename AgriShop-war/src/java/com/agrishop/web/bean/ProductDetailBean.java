package com.agrishop.web.bean;

import com.agrishop.dto.ProductDTO;
import com.agrishop.dto.ReviewDTO;
import com.agrishop.dto.UserDTO;
import com.agrishop.exception.BusinessException;
import com.agrishop.service.ProductServiceLocal;
import com.agrishop.service.ReviewServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Named("productDetailBean")
@ViewScoped
public class ProductDetailBean implements Serializable {

    @EJB
    private ProductServiceLocal productService;

    @EJB
    private ReviewServiceLocal reviewService;

    @Inject
    private CartBean cartBean;

    @Inject
    private WishlistBean wishlistBean;

    @Inject
    private LoginBean loginBean;

    private Long productId;
    private ProductDTO product;
    private BigDecimal quantity = BigDecimal.ONE;
    private String activeImageUrl;
    private List<ReviewDTO> approvedReviews = new ArrayList<>();
    private Double averageRating = 0.0;
    private Integer reviewCount = 0;
    private int reviewRating = 5;
    private String reviewComment;

    @PostConstruct
    public void init() {
        // Initialization
    }

    public void loadProduct() {
        if (productId != null) {
            this.product = productService.getProductById(productId);
            if (this.product != null) {
                if (this.product.getImageUrl() != null) {
                    this.activeImageUrl = this.product.getImageUrl();
                } else if (this.product.getImages() != null && !this.product.getImages().isEmpty()) {
                    this.activeImageUrl = this.product.getImages().get(0).getImageUrl();
                }
                if ("WEIGHT".equalsIgnoreCase(this.product.getUnitType())) {
                    this.quantity = new BigDecimal("1.0");
                } else {
                    this.quantity = BigDecimal.ONE;
                }
            }
            try {
                this.approvedReviews = reviewService.getApprovedReviewsByProduct(productId);
                if (this.approvedReviews != null && !this.approvedReviews.isEmpty()) {
                    this.reviewCount = this.approvedReviews.size();
                    double sum = 0.0;
                    for (ReviewDTO r : this.approvedReviews) {
                        if (r.getRating() != null) {
                            sum += r.getRating();
                        }
                    }
                    this.averageRating = Math.round((sum / this.reviewCount) * 10.0) / 10.0;
                } else {
                    this.reviewCount = 0;
                    this.averageRating = 0.0;
                }
            } catch (Exception e) {
                this.approvedReviews = new ArrayList<>();
                this.reviewCount = 0;
                this.averageRating = 0.0;
            }
        }
    }

    public void changeActiveImage(String url) {
        if (url != null && !url.trim().isEmpty()) {
            this.activeImageUrl = url;
        }
    }

    public BigDecimal getStep() {
        if (product != null && "WEIGHT".equalsIgnoreCase(product.getUnitType())) {
            return new BigDecimal("0.5");
        }
        return BigDecimal.ONE;
    }

    public void increaseQuantity() {
        BigDecimal step = getStep();
        if (product != null && product.getStockQuantity() != null) {
            if (quantity.add(step).compareTo(product.getStockQuantity()) <= 0) {
                quantity = quantity.add(step);
            }
        } else {
            quantity = quantity.add(step);
        }
    }

    public void decreaseQuantity() {
        BigDecimal step = getStep();
        if (quantity.compareTo(step) > 0) {
            quantity = quantity.subtract(step);
        }
    }

    public void addToCart() {
        if (product != null) {
            cartBean.addToCart(product.getId(), quantity);
        }
    }

    public void toggleWishlist() {
        if (product != null) {
            wishlistBean.toggleWishlist(product.getId());
        }
    }

    public boolean isWishlisted() {
        if (product != null) {
            return wishlistBean.isWishlisted(product.getId());
        }
        return false;
    }

    // Getters and Setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public ProductDTO getProduct() { return product; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getActiveImageUrl() { return activeImageUrl; }
    public void setActiveImageUrl(String activeImageUrl) { this.activeImageUrl = activeImageUrl; }
    public List<ReviewDTO> getApprovedReviews() { return approvedReviews; }
    public void setApprovedReviews(List<ReviewDTO> approvedReviews) { this.approvedReviews = approvedReviews; }
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }

    public int getReviewRating() { return reviewRating; }
    public void setReviewRating(int reviewRating) { this.reviewRating = reviewRating; }

    public String getReviewComment() { return reviewComment; }
    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }

    public void submitReview() {
        UserDTO currentUser = loginBean != null ? loginBean.getCurrentUser() : null;
        if (currentUser == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Yêu cầu đăng nhập", "Vui lòng đăng nhập để viết đánh giá cho sản phẩm này!"));
            return;
        }

        try {
            reviewService.createReview(currentUser.getId(), productId, reviewRating, reviewComment);
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", "Đánh giá của bạn đã được gửi thành công!"));
            this.reviewComment = "";
            this.reviewRating = 5;
            loadProduct();
        } catch (BusinessException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Không được phép", e.getMessage()));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", e.getMessage()));
        }
    }

    public String getStructuredDataJson() {
        if (product == null) {
            return "{}";
        }
        
        jakarta.faces.context.FacesContext fc = jakarta.faces.context.FacesContext.getCurrentInstance();
        jakarta.servlet.http.HttpServletRequest req = (jakarta.servlet.http.HttpServletRequest) fc.getExternalContext().getRequest();
        
        String scheme = req.getScheme();
        String serverName = req.getServerName();
        int port = req.getServerPort();
        String contextPath = req.getContextPath();
        String baseUrl = ((scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443))
                ? (scheme + "://" + serverName + contextPath)
                : (scheme + "://" + serverName + ":" + port + contextPath);

        String fullProductUrl = baseUrl + "/product-detail.xhtml?id=" + product.getId();
        
        String fullImageUrl = "";
        if (activeImageUrl != null && !activeImageUrl.trim().isEmpty()) {
            if (activeImageUrl.startsWith("http://") || activeImageUrl.startsWith("https://")) {
                fullImageUrl = activeImageUrl;
            } else {
                fullImageUrl = baseUrl + (activeImageUrl.startsWith("/") ? activeImageUrl : "/" + activeImageUrl);
            }
        }

        String safeName = escapeJson(product.getName());
        String safeDesc = escapeJson(product.getDescription() != null && !product.getDescription().trim().isEmpty() 
                ? product.getDescription() 
                : product.getName() + " đạt chuẩn VietGAP tươi sạch tại AgriShop");
        String safeSku = escapeJson(product.getProductCode() != null ? product.getProductCode() : "AGRI-" + product.getId());
        String availability = (product.getStockQuantity() != null && product.getStockQuantity().compareTo(BigDecimal.ZERO) > 0)
                ? "https://schema.org/InStock"
                : "https://schema.org/OutOfStock";

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"@context\": \"https://schema.org/\",\n");
        sb.append("  \"@type\": \"Product\",\n");
        sb.append("  \"name\": \"").append(safeName).append("\",\n");
        if (!fullImageUrl.isEmpty()) {
            sb.append("  \"image\": [\"").append(fullImageUrl).append("\"],\n");
        }
        sb.append("  \"description\": \"").append(safeDesc).append("\",\n");
        sb.append("  \"sku\": \"").append(safeSku).append("\",\n");
        sb.append("  \"brand\": {\n");
        sb.append("    \"@type\": \"Brand\",\n");
        sb.append("    \"name\": \"AgriShop\"\n");
        sb.append("  },\n");
        sb.append("  \"offers\": {\n");
        sb.append("    \"@type\": \"Offer\",\n");
        sb.append("    \"url\": \"").append(fullProductUrl).append("\",\n");
        sb.append("    \"priceCurrency\": \"VND\",\n");
        sb.append("    \"price\": \"").append(product.getPrice() != null ? product.getPrice().toPlainString() : "0").append("\",\n");
        sb.append("    \"availability\": \"").append(availability).append("\",\n");
        sb.append("    \"itemCondition\": \"https://schema.org/NewCondition\"\n");
        sb.append("  }");

        if (reviewCount != null && reviewCount > 0 && averageRating != null && averageRating > 0) {
            sb.append(",\n");
            sb.append("  \"aggregateRating\": {\n");
            sb.append("    \"@type\": \"AggregateRating\",\n");
            sb.append("    \"ratingValue\": \"").append(averageRating).append("\",\n");
            sb.append("    \"reviewCount\": \"").append(reviewCount).append("\",\n");
            sb.append("    \"bestRating\": \"5\",\n");
            sb.append("    \"worstRating\": \"1\"\n");
            sb.append("  }\n");
        } else {
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}
