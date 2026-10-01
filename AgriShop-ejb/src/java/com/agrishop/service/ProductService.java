package com.agrishop.service;

import com.agrishop.dto.ProductDTO;
import com.agrishop.dto.ProductImageDTO;
import com.agrishop.dto.PageRequestDTO;
import com.agrishop.dto.PageResponseDTO;
import com.agrishop.entity.Category;
import com.agrishop.entity.InventoryTransaction;
import com.agrishop.entity.Product;
import com.agrishop.entity.ProductImage;
import com.agrishop.entity.User;
import com.agrishop.repository.CategoryRepositoryLocal;
import com.agrishop.repository.ProductRepositoryLocal;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class ProductService implements ProductServiceLocal {

    @PersistenceContext(unitName = "AgriShopPU")
    private EntityManager em;

    @EJB
    private ProductRepositoryLocal productRepository;

    @EJB
    private CategoryRepositoryLocal categoryRepository;

    @EJB
    private com.agrishop.repository.SupplierRepositoryLocal supplierRepository;

    @Override
    public List<ProductDTO> getAllActiveProducts() {
        List<Product> products = productRepository.findActiveProducts();
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public PageResponseDTO<ProductDTO> getProductsWithPagination(PageRequestDTO request, Long categoryIdFilter) {
        List<Product> products = productRepository.findWithPagination(request, categoryIdFilter);
        long totalRecords = productRepository.countWithPagination(request, categoryIdFilter);
        
        List<ProductDTO> dtoList = products.stream().map(this::convertToDTO).collect(Collectors.toList());
        return new PageResponseDTO<>(dtoList, totalRecords);
    }

    @Override
    public void createProduct(ProductDTO dto) {
        Product product = new Product();
        Category category = categoryRepository.findById(dto.getCategoryId());
        com.agrishop.entity.Supplier supplier = null;
        if (dto.getSupplierId() != null) {
            supplier = supplierRepository.findById(dto.getSupplierId());
        }
        
        product.setProductCode(dto.getProductCode());
        product.setCategory(category);
        product.setSupplier(supplier);
        product.setName(dto.getName());
        product.setUnit(dto.getUnit());
        product.setUnitType(dto.getUnitType() != null ? dto.getUnitType() : "COUNT");
        product.setPrice(dto.getPrice());
        product.setCostPrice(dto.getCostPrice() != null ? dto.getCostPrice() : BigDecimal.ZERO);
        product.setStockQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : BigDecimal.ZERO);
        product.setReservedQuantity(BigDecimal.ZERO);
        product.setMinStock(dto.getMinStock() != null ? dto.getMinStock() : BigDecimal.valueOf(10));
        product.setTotalImported(dto.getStockQuantity() != null ? dto.getStockQuantity() : BigDecimal.ZERO);
        product.setTotalSold(BigDecimal.ZERO);
        product.setDescription(dto.getDescription());
        product.setImageUrl(dto.getImageUrl());
        product.setIsDeleted(false);
        productRepository.create(product);
        dto.setId(product.getId());

        // If initial stock is provided, automatically log an IMPORT transaction for audit trail
        if (product.getStockQuantity() != null && product.getStockQuantity().compareTo(BigDecimal.ZERO) > 0) {
            InventoryTransaction tx = new InventoryTransaction();
            tx.setProduct(product);
            
            User user = null;
            List<User> admins = em.createQuery("SELECT u FROM User u WHERE u.role = 'ADMIN'", User.class)
                                  .setMaxResults(1)
                                  .getResultList();
            if (!admins.isEmpty()) {
                user = admins.get(0);
            } else {
                List<User> anyUser = em.createQuery("SELECT u FROM User u", User.class)
                                       .setMaxResults(1)
                                       .getResultList();
                if (!anyUser.isEmpty()) {
                    user = anyUser.get(0);
                }
            }
            tx.setUser(user);
            tx.setSupplier(supplier);
            tx.setQuantityChanged(product.getStockQuantity());
            tx.setUnitCost(product.getCostPrice());
            tx.setTransactionType("IMPORT");
            tx.setReason("Khởi tạo tồn kho ban đầu khi tạo sản phẩm");
            tx.setCreatedAt(new Date());
            em.persist(tx);
        }

        // Also add primary image to ProductImages if imageUrl is given
        if (product.getImageUrl() != null && !product.getImageUrl().trim().isEmpty()) {
            ProductImage pi = new ProductImage(product, product.getImageUrl().trim(), 0, true);
            em.persist(pi);
        }
    }

    @Override
    public void updateProduct(ProductDTO dto) {
        Product product = productRepository.findById(dto.getId());
        if (product != null && !product.getIsDeleted()) {
            Category category = categoryRepository.findById(dto.getCategoryId());
            com.agrishop.entity.Supplier supplier = null;
            if (dto.getSupplierId() != null) {
                supplier = supplierRepository.findById(dto.getSupplierId());
            }
            product.setCategory(category);
            product.setSupplier(supplier);
            product.setName(dto.getName());
            product.setUnit(dto.getUnit());
            if (dto.getUnitType() != null) {
                product.setUnitType(dto.getUnitType());
            }
            product.setPrice(dto.getPrice());
            if (dto.getCostPrice() != null) {
                product.setCostPrice(dto.getCostPrice());
            }
            if (dto.getMinStock() != null) {
                product.setMinStock(dto.getMinStock());
            }
            // NOT updating stockQuantity here - Stock can ONLY be changed via Inventory Transactions
            product.setDescription(dto.getDescription());
            product.setImageUrl(dto.getImageUrl());
            productRepository.update(product);
        }
    }

    @Override
    public boolean hasLinkedData(Long productId) {
        if (productId == null) return false;

        Long orderDetailCount = em.createQuery("SELECT COUNT(od) FROM OrderDetail od WHERE od.product.id = :pid", Long.class)
                                  .setParameter("pid", productId)
                                  .getSingleResult();
        if (orderDetailCount != null && orderDetailCount > 0) return true;

        Long invTransCount = em.createQuery("SELECT COUNT(it) FROM InventoryTransaction it WHERE it.product.id = :pid", Long.class)
                               .setParameter("pid", productId)
                               .getSingleResult();
        if (invTransCount != null && invTransCount > 0) return true;

        Long reviewCount = em.createQuery("SELECT COUNT(r) FROM Review r WHERE r.product.id = :pid", Long.class)
                             .setParameter("pid", productId)
                             .getSingleResult();
        return reviewCount != null && reviewCount > 0;
    }

    @Override
    @jakarta.ejb.TransactionAttribute(jakarta.ejb.TransactionAttributeType.REQUIRED)
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id);
        if (product == null) return;

        if (hasLinkedData(id)) {
            // SOFT-DELETE: Mark isDeleted = true to preserve history
            product.setIsDeleted(true);
            productRepository.update(product);

            // Clean up transient customer intent data (CartItems & Wishlists)
            em.createQuery("DELETE FROM CartItem ci WHERE ci.product.id = :pid")
              .setParameter("pid", id)
              .executeUpdate();
            em.createQuery("DELETE FROM Wishlist w WHERE w.product.id = :pid")
              .setParameter("pid", id)
              .executeUpdate();
        } else {
            // HARD-DELETE: No historical business links exist
            try {
                hardDeleteProduct(id);
            } catch (Exception e) {
                // Fallback to soft delete if unexpected constraint occurs
                product.setIsDeleted(true);
                productRepository.update(product);
            }
        }
    }

    @Override
    @jakarta.ejb.TransactionAttribute(jakarta.ejb.TransactionAttributeType.REQUIRED)
    public void hardDeleteProduct(Long id) throws Exception {
        Product product = productRepository.findById(id);
        if (product == null) return;

        if (hasLinkedData(id)) {
            throw new Exception("Sản phẩm đã có dữ liệu giao dịch/kho/đánh giá liên kết, không thể xóa vĩnh viễn.");
        }

        // Clean up transient references
        em.createQuery("DELETE FROM CartItem ci WHERE ci.product.id = :pid")
          .setParameter("pid", id)
          .executeUpdate();
        em.createQuery("DELETE FROM Wishlist w WHERE w.product.id = :pid")
          .setParameter("pid", id)
          .executeUpdate();

        // Clean up child product images
        em.createQuery("DELETE FROM ProductImage pi WHERE pi.product.id = :pid")
          .setParameter("pid", id)
          .executeUpdate();

        // Remove the product entity
        productRepository.delete(product);
        em.flush();
    }

    @Override
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id);
        if (product != null && (product.getIsDeleted() == null || !product.getIsDeleted())) {
            return convertToDTO(product);
        }
        return null;
    }

    // ==================== GALLERY IMAGE METHODS ====================

    @Override
    public void addProductImage(Long productId, String imageUrl, Integer displayOrder, boolean isPrimary) {
        Product product = em.find(Product.class, productId);
        if (product == null) return;

        if (isPrimary) {
            // Unset current primary images
            em.createQuery("UPDATE ProductImage pi SET pi.isPrimary = false WHERE pi.product.id = :pid")
              .setParameter("pid", productId)
              .executeUpdate();
            product.setImageUrl(imageUrl);
            em.merge(product);
        }

        ProductImage img = new ProductImage(product, imageUrl, displayOrder, isPrimary);
        em.persist(img);

        // If product has no primary image, make this primary
        if (product.getImageUrl() == null || product.getImageUrl().trim().isEmpty()) {
            product.setImageUrl(imageUrl);
            img.setIsPrimary(true);
            em.merge(product);
            em.merge(img);
        }
    }

    @Override
    public void deleteProductImage(Long imageId) {
        ProductImage img = em.find(ProductImage.class, imageId);
        if (img != null) {
            Product p = img.getProduct();
            boolean wasPrimary = img.getIsPrimary() != null && img.getIsPrimary();
            em.remove(img);

            if (wasPrimary && p != null) {
                // Find next available image and set as primary
                List<ProductImage> remaining = em.createQuery("SELECT pi FROM ProductImage pi WHERE pi.product.id = :pid ORDER BY pi.displayOrder ASC", ProductImage.class)
                                                 .setParameter("pid", p.getId())
                                                 .setMaxResults(1)
                                                 .getResultList();
                if (!remaining.isEmpty()) {
                    ProductImage next = remaining.get(0);
                    next.setIsPrimary(true);
                    p.setImageUrl(next.getImageUrl());
                    em.merge(next);
                } else {
                    p.setImageUrl(null);
                }
                em.merge(p);
            }
        }
    }

    @Override
    public void setPrimaryProductImage(Long productId, Long imageId) {
        Product product = em.find(Product.class, productId);
        if (product == null) return;

        em.createQuery("UPDATE ProductImage pi SET pi.isPrimary = false WHERE pi.product.id = :pid")
          .setParameter("pid", productId)
          .executeUpdate();

        ProductImage img = em.find(ProductImage.class, imageId);
        if (img != null && img.getProduct().getId().equals(productId)) {
            img.setIsPrimary(true);
            product.setImageUrl(img.getImageUrl());
            em.merge(img);
            em.merge(product);
        }
    }

    @Override
    public List<ProductImageDTO> getProductImages(Long productId) {
        List<ProductImage> list = em.createQuery("SELECT pi FROM ProductImage pi WHERE pi.product.id = :pid ORDER BY pi.displayOrder ASC, pi.id ASC", ProductImage.class)
                                    .setParameter("pid", productId)
                                    .getResultList();
        List<ProductImageDTO> dtos = new ArrayList<>();
        if (list != null) {
            for (ProductImage pi : list) {
                dtos.add(new ProductImageDTO(pi.getId(), productId, pi.getImageUrl(), pi.getDisplayOrder(), pi.getIsPrimary()));
            }
        }
        return dtos;
    }
    
    private ProductDTO convertToDTO(Product p) {
        ProductDTO dto = new ProductDTO();
        dto.setId(p.getId());
        dto.setProductCode(p.getProductCode());
        if (p.getCategory() != null) {
            dto.setCategoryId(p.getCategory().getId());
            dto.setCategoryName(p.getCategory().getName());
        }
        if (p.getSupplier() != null) {
            dto.setSupplierId(p.getSupplier().getId());
            dto.setSupplierName(p.getSupplier().getName());
        }
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
        dto.setDescription(p.getDescription());
        dto.setImageUrl(p.getImageUrl());
        dto.setIsDeleted(p.getIsDeleted());

        // Convert images
        if (p.getImages() != null && !p.getImages().isEmpty()) {
            List<ProductImageDTO> imgDTOs = new ArrayList<>();
            for (ProductImage pi : p.getImages()) {
                imgDTOs.add(new ProductImageDTO(pi.getId(), p.getId(), pi.getImageUrl(), pi.getDisplayOrder(), pi.getIsPrimary()));
            }
            dto.setImages(imgDTOs);
        } else if (p.getImageUrl() != null && !p.getImageUrl().trim().isEmpty()) {
            // Fallback to single primary image in gallery
            List<ProductImageDTO> imgDTOs = new ArrayList<>();
            imgDTOs.add(new ProductImageDTO(null, p.getId(), p.getImageUrl(), 0, true));
            dto.setImages(imgDTOs);
        }

        return dto;
    }
}