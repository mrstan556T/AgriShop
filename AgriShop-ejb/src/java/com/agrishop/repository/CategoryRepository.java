package com.agrishop.repository;

import com.agrishop.entity.Category;
import com.agrishop.dto.PageRequestDTO;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

@Stateless
public class CategoryRepository implements CategoryRepositoryLocal {

    @PersistenceContext(unitName = "AgriShopPU")
    private EntityManager em;

    @Override
    public Category findById(Long id) {
        return em.find(Category.class, id);
    }

    @Override
    public List<Category> findAll() {
        return em.createQuery("SELECT c FROM Category c", Category.class).getResultList();
    }

    @Override
    public List<Category> findActiveCategories() {
        return em.createQuery("SELECT c FROM Category c WHERE c.isDeleted = false", Category.class).getResultList();
    }

    @Override
    public void create(Category category) {
        em.persist(category);
    }

    @Override
    public void update(Category category) {
        em.merge(category);
    }

    @Override
    public void delete(Category category) {
        em.merge(category);
    }

    @Override
    public List<Category> findWithPagination(PageRequestDTO request) {
        StringBuilder jpql = new StringBuilder("SELECT c FROM Category c WHERE c.isDeleted = false ");
        
        if (request.getSearchKeyword() != null && !request.getSearchKeyword().trim().isEmpty()) {
            jpql.append(" AND (LOWER(c.name) LIKE :keyword OR LOWER(c.code) LIKE :keyword) ");
        }
        
        if (request.getSortField() != null && !request.getSortField().trim().isEmpty()) {
            jpql.append(" ORDER BY c.").append(request.getSortField()).append(" ").append(request.getSortOrder());
        } else {
            jpql.append(" ORDER BY c.id DESC");
        }

        TypedQuery<Category> query = em.createQuery(jpql.toString(), Category.class);
        
        if (request.getSearchKeyword() != null && !request.getSearchKeyword().trim().isEmpty()) {
            query.setParameter("keyword", "%" + request.getSearchKeyword().trim().toLowerCase() + "%");
        }
        
        query.setFirstResult(request.getPageIndex() * request.getPageSize());
        query.setMaxResults(request.getPageSize());
        
        return query.getResultList();
    }

    @Override
    public long countWithPagination(PageRequestDTO request) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(c) FROM Category c WHERE c.isDeleted = false ");
        
        if (request.getSearchKeyword() != null && !request.getSearchKeyword().trim().isEmpty()) {
            jpql.append(" AND (LOWER(c.name) LIKE :keyword OR LOWER(c.code) LIKE :keyword) ");
        }

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
        
        if (request.getSearchKeyword() != null && !request.getSearchKeyword().trim().isEmpty()) {
            query.setParameter("keyword", "%" + request.getSearchKeyword().trim().toLowerCase() + "%");
        }
        
        return query.getSingleResult();
    }
}
