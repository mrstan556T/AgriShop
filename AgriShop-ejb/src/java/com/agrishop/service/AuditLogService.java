package com.agrishop.service;

import com.agrishop.dto.AuditLogDTO;
import com.agrishop.entity.AuditLog;
import com.agrishop.entity.User;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Stateless
public class AuditLogService implements AuditLogServiceLocal {

    @PersistenceContext(unitName = "AgriShopPU")
    private EntityManager em;

    @Override
    public void log(Long userId, String action, String entityName, Long entityId, String oldValue, String newValue) {
        try {
            User user = null;
            if (userId != null) {
                user = em.find(User.class, userId);
            }
            if (user == null) {
                List<User> admins = em.createQuery("SELECT u FROM User u WHERE u.role = 'ADMIN'", User.class)
                                      .setMaxResults(1)
                                      .getResultList();
                if (!admins.isEmpty()) user = admins.get(0);
            }
            if (user == null) return; // Cannot log without a user

            AuditLog log = new AuditLog();
            log.setUser(user);
            log.setAction(action);
            log.setEntityName(entityName);
            log.setEntityId(entityId != null ? entityId : 0L);
            log.setOldValue(oldValue);
            log.setNewValue(newValue);
            log.setCreatedAt(new Date());

            em.persist(log);
        } catch (Exception ignored) {
            // Logging failure should never break business operations
        }
    }

    @Override
    public List<AuditLogDTO> getAuditLogsLazy(int first, int pageSize, String sortField, String sortOrder, Map<String, Object> filters) {
        StringBuilder jpql = new StringBuilder("SELECT a FROM AuditLog a JOIN FETCH a.user u WHERE 1=1");
        applyFilters(jpql, filters);

        if (sortField != null && !sortField.isEmpty()) {
            String dir = "DESC".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";
            jpql.append(" ORDER BY a.").append(sortField).append(" ").append(dir);
        } else {
            jpql.append(" ORDER BY a.id DESC");
        }

        TypedQuery<AuditLog> query = em.createQuery(jpql.toString(), AuditLog.class);
        setFilterParameters(query, filters);
        query.setFirstResult(first);
        query.setMaxResults(pageSize);

        List<AuditLog> list = query.getResultList();
        List<AuditLogDTO> dtoList = new ArrayList<>();
        for (AuditLog a : list) {
            AuditLogDTO dto = new AuditLogDTO();
            dto.setId(a.getId());
            if (a.getUser() != null) {
                dto.setUserId(a.getUser().getId());
                dto.setUserName(a.getUser().getFullName() != null ? a.getUser().getFullName() : a.getUser().getUsername());
                dto.setUserEmail(a.getUser().getEmail());
                dto.setUserRole(a.getUser().getRole());
            }
            dto.setAction(a.getAction());
            dto.setEntityName(a.getEntityName());
            dto.setEntityId(a.getEntityId());
            dto.setOldValue(a.getOldValue());
            dto.setNewValue(a.getNewValue());
            dto.setCreatedAt(a.getCreatedAt());
            dtoList.add(dto);
        }
        return dtoList;
    }

    @Override
    public int countAuditLogs(Map<String, Object> filters) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(a) FROM AuditLog a WHERE 1=1");
        applyFilters(jpql, filters);

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
        setFilterParameters(query, filters);
        Long count = query.getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    private void applyFilters(StringBuilder jpql, Map<String, Object> filters) {
        if (filters != null) {
            if (filters.containsKey("globalFilter") && filters.get("globalFilter") != null) {
                jpql.append(" AND (LOWER(a.action) LIKE :globalFilter OR LOWER(a.entityName) LIKE :globalFilter OR LOWER(a.user.fullName) LIKE :globalFilter OR LOWER(a.user.username) LIKE :globalFilter)");
            }
            if (filters.containsKey("action") && filters.get("action") != null && !filters.get("action").toString().isEmpty()) {
                jpql.append(" AND a.action = :action");
            }
            if (filters.containsKey("entityName") && filters.get("entityName") != null && !filters.get("entityName").toString().isEmpty()) {
                jpql.append(" AND a.entityName = :entityName");
            }
        }
    }

    private void setFilterParameters(TypedQuery<?> query, Map<String, Object> filters) {
        if (filters != null) {
            if (filters.containsKey("globalFilter") && filters.get("globalFilter") != null) {
                query.setParameter("globalFilter", "%" + filters.get("globalFilter").toString().trim().toLowerCase() + "%");
            }
            if (filters.containsKey("action") && filters.get("action") != null && !filters.get("action").toString().isEmpty()) {
                query.setParameter("action", filters.get("action").toString());
            }
            if (filters.containsKey("entityName") && filters.get("entityName") != null && !filters.get("entityName").toString().isEmpty()) {
                query.setParameter("entityName", filters.get("entityName").toString());
            }
        }
    }
}
