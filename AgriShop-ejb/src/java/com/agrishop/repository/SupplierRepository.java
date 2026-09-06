package com.agrishop.repository;

import com.agrishop.entity.Supplier;
import com.agrishop.dto.PageRequestDTO;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

@Stateless
public class SupplierRepository implements SupplierRepositoryLocal {

    @PersistenceContext(unitName = "AgriShopPU")
    private EntityManager em;

    @Override
    public Supplier findById(Long id) {
        return em.find(Supplier.class, id);
    }

    @Override
    public List<Supplier> findActiveSuppliers() {
        return em.createQuery("SELECT s FROM Supplier s WHERE s.isDeleted = false ORDER BY s.name ASC", Supplier.class).getResultList();
    }

    @Override
    public void create(Supplier supplier) {
        em.persist(supplier);
    }

    @Override
    public void update(Supplier supplier) {
        em.merge(supplier);
    }

    @Override
    public List<Supplier> findWithPagination(PageRequestDTO request) {
        StringBuilder jpql = new StringBuilder("SELECT s FROM Supplier s WHERE s.isDeleted = false ");
        if (request.getSearchKeyword() != null && !request.getSearchKeyword().trim().isEmpty()) {
            jpql.append(" AND (LOWER(s.name) LIKE :keyword OR LOWER(s.supplierCode) LIKE :keyword) ");
        }
        
        if (request.getSortField() != null && !request.getSortField().trim().isEmpty()) {
            jpql.append(" ORDER BY s.").append(request.getSortField()).append(" ").append(request.getSortOrder());
        } else {
            jpql.append(" ORDER BY s.id DESC");
        }

        TypedQuery<Supplier> query = em.createQuery(jpql.toString(), Supplier.class);
        if (request.getSearchKeyword() != null && !request.getSearchKeyword().trim().isEmpty()) {
            query.setParameter("keyword", "%" + request.getSearchKeyword().trim().toLowerCase() + "%");
        }
        
        query.setFirstResult(request.getPageIndex() * request.getPageSize());
        query.setMaxResults(request.getPageSize());
        
        return query.getResultList();
    }

    @Override
    public long countWithPagination(PageRequestDTO request) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(s) FROM Supplier s WHERE s.isDeleted = false ");
        if (request.getSearchKeyword() != null && !request.getSearchKeyword().trim().isEmpty()) {
            jpql.append(" AND (LOWER(s.name) LIKE :keyword OR LOWER(s.supplierCode) LIKE :keyword) ");
        }

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
        if (request.getSearchKeyword() != null && !request.getSearchKeyword().trim().isEmpty()) {
            query.setParameter("keyword", "%" + request.getSearchKeyword().trim().toLowerCase() + "%");
        }
        
        return query.getSingleResult();
    }
}
