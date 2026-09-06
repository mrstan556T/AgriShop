package com.agrishop.repository;

import com.agrishop.entity.Product;
import com.agrishop.dto.PageRequestDTO;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
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
        return em.createQuery("SELECT p FROM Product p JOIN FETCH p.category WHERE p.isDeleted = false", Product.class).getResultList();
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
        em.merge(product);
    }

    @Override
    public List<Product> findWithPagination(PageRequestDTO request, Long categoryIdFilter) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM Product p JOIN FETCH p.category WHERE p.isDeleted = false ");
        
        if (request.getSearchKeyword() != null && !request.getSearchKeyword().trim().isEmpty()) {
            jpql.append(" AND (LOWER(p.name) LIKE :keyword OR LOWER(p.productCode) LIKE :keyword) ");
        }
        
        if (categoryIdFilter != null) {
            jpql.append(" AND p.category.id = :categoryId ");
        }
        
        if (request.getSortField() != null && !request.getSortField().trim().isEmpty()) {
            jpql.append(" ORDER BY p.").append(request.getSortField()).append(" ").append(request.getSortOrder());
        } else {
            jpql.append(" ORDER BY p.id DESC");
        }

        TypedQuery<Product> query = em.createQuery(jpql.toString(), Product.class);
        
        if (request.getSearchKeyword() != null && !request.getSearchKeyword().trim().isEmpty()) {
            query.setParameter("keyword", "%" + request.getSearchKeyword().trim().toLowerCase() + "%");
        }
        
        if (categoryIdFilter != null) {
            query.setParameter("categoryId", categoryIdFilter);
        }
        
        query.setFirstResult(request.getPageIndex() * request.getPageSize());
        query.setMaxResults(request.getPageSize());
        
        return query.getResultList();
    }

    @Override
    public long countWithPagination(PageRequestDTO request, Long categoryIdFilter) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(p) FROM Product p WHERE p.isDeleted = false ");
        
        if (request.getSearchKeyword() != null && !request.getSearchKeyword().trim().isEmpty()) {
            jpql.append(" AND (LOWER(p.name) LIKE :keyword OR LOWER(p.productCode) LIKE :keyword) ");
        }
        
        if (categoryIdFilter != null) {
            jpql.append(" AND p.category.id = :categoryId ");
        }

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
        
        if (request.getSearchKeyword() != null && !request.getSearchKeyword().trim().isEmpty()) {
            query.setParameter("keyword", "%" + request.getSearchKeyword().trim().toLowerCase() + "%");
        }
        
        if (categoryIdFilter != null) {
            query.setParameter("categoryId", categoryIdFilter);
        }
        
        return query.getSingleResult();
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
