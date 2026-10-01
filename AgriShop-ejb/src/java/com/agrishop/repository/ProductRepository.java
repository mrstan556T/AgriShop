package com.agrishop.repository;

import com.agrishop.entity.Product;
import com.agrishop.dto.PageRequestDTO;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.List;

@Stateless
public class ProductRepository implements ProductRepositoryLocal {

    @PersistenceContext(unitName = "AgriShopPU")
    private EntityManager em;

    @Override
    public Product findById(Long id) {
        return em.find(Product.class, id);
    }

    @Override
    public List<Product> findAll() {
        return em.createQuery("SELECT p FROM Product p", Product.class).getResultList();
    }

    @Override
    public List<Product> findActiveProducts() {
        return em.createQuery("SELECT DISTINCT p FROM Product p JOIN FETCH p.category LEFT JOIN FETCH p.images WHERE p.isDeleted = false", Product.class).getResultList();
    }

    @Override
    public void create(Product product) {
        em.persist(product);
    }

    @Override
    public void update(Product product) {
        em.merge(product);
    }

    @Override
    public void delete(Product product) {
        if (product != null) {
            em.remove(em.contains(product) ? product : em.merge(product));
        }
    }

    @Override
    public List<Product> findWithPagination(PageRequestDTO request, Long categoryIdFilter) {
        StringBuilder jpql = new StringBuilder("SELECT DISTINCT p FROM Product p JOIN FETCH p.category LEFT JOIN FETCH p.images WHERE p.isDeleted = false ");
        
        appendFilters(jpql, request, categoryIdFilter);
        
        // Sorting logic
        if (request.getSortOption() != null && !request.getSortOption().trim().isEmpty()) {
            switch (request.getSortOption()) {
                case "PRICE_ASC":
                    jpql.append(" ORDER BY p.price ASC");
                    break;
                case "PRICE_DESC":
                    jpql.append(" ORDER BY p.price DESC");
                    break;
                case "BEST_SELLER":
                    jpql.append(" ORDER BY p.totalSold DESC, p.id DESC");
                    break;
                case "NAME_ASC":
                    jpql.append(" ORDER BY p.name ASC");
                    break;
                case "NEWEST":
                default:
                    jpql.append(" ORDER BY p.id DESC");
                    break;
            }
        } else if (request.getSortField() != null && !request.getSortField().trim().isEmpty()) {
            jpql.append(" ORDER BY p.").append(request.getSortField()).append(" ").append(request.getSortOrder());
        } else {
            jpql.append(" ORDER BY p.id DESC");
        }

        TypedQuery<Product> query = em.createQuery(jpql.toString(), Product.class);
        setFilterParameters(query, request, categoryIdFilter);
        
        query.setFirstResult(request.getPageIndex() * request.getPageSize());
        query.setMaxResults(request.getPageSize());
        
        return query.getResultList();
    }

    @Override
    public long countWithPagination(PageRequestDTO request, Long categoryIdFilter) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(DISTINCT p) FROM Product p WHERE p.isDeleted = false ");
        
        appendFilters(jpql, request, categoryIdFilter);

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
        setFilterParameters(query, request, categoryIdFilter);
        
        return query.getSingleResult();
    }

    private void appendFilters(StringBuilder jpql, PageRequestDTO request, Long categoryIdFilter) {
        if (request.getSearchKeyword() != null && !request.getSearchKeyword().trim().isEmpty()) {
            jpql.append(" AND (LOWER(p.name) LIKE :keyword OR LOWER(p.productCode) LIKE :keyword) ");
        }
        
        if (categoryIdFilter != null) {
            jpql.append(" AND p.category.id = :categoryId ");
        }

        if (request.getMinPrice() != null && request.getMinPrice().compareTo(BigDecimal.ZERO) > 0) {
            jpql.append(" AND p.price >= :minPrice ");
        }

        if (request.getMaxPrice() != null && request.getMaxPrice().compareTo(BigDecimal.ZERO) > 0) {
            jpql.append(" AND p.price <= :maxPrice ");
        }

        if (request.getInStockOnly() != null && request.getInStockOnly()) {
            jpql.append(" AND (p.stockQuantity - p.reservedQuantity) > 0 ");
        }
    }

    private void setFilterParameters(TypedQuery<?> query, PageRequestDTO request, Long categoryIdFilter) {
        if (request.getSearchKeyword() != null && !request.getSearchKeyword().trim().isEmpty()) {
            query.setParameter("keyword", "%" + request.getSearchKeyword().trim().toLowerCase() + "%");
        }
        
        if (categoryIdFilter != null) {
            query.setParameter("categoryId", categoryIdFilter);
        }

        if (request.getMinPrice() != null && request.getMinPrice().compareTo(BigDecimal.ZERO) > 0) {
            query.setParameter("minPrice", request.getMinPrice());
        }

        if (request.getMaxPrice() != null && request.getMaxPrice().compareTo(BigDecimal.ZERO) > 0) {
            query.setParameter("maxPrice", request.getMaxPrice());
        }
    }

    @Override
    public long countByCategoryId(Long categoryId) {
        return em.createQuery("SELECT COUNT(p) FROM Product p WHERE p.isDeleted = false AND p.category.id = :catId", Long.class)
                 .setParameter("catId", categoryId)
                 .getSingleResult();
    }

    @Override
    public long countBySupplierId(Long supplierId) {
        return em.createQuery("SELECT COUNT(p) FROM Product p WHERE p.isDeleted = false AND p.supplier.id = :supId", Long.class)
                 .setParameter("supId", supplierId)
                 .getSingleResult();
    }
}
