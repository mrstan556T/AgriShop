package com.agrishop.repository;

import com.agrishop.entity.SystemSetting;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;

@Stateless
public class SystemSettingRepository implements SystemSettingRepositoryLocal {

    @PersistenceContext(unitName = "AgriShopPU")
    private EntityManager em;

    @Override
    public SystemSetting findByKey(String key) {
        if (key == null || key.trim().isEmpty()) return null;
        List<SystemSetting> list = em.createQuery("SELECT s FROM SystemSetting s WHERE s.settingKey = :key", SystemSetting.class)
                                     .setParameter("key", key.trim())
                                     .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<SystemSetting> findAll() {
        return em.createQuery("SELECT s FROM SystemSetting s ORDER BY s.id ASC", SystemSetting.class)
                 .getResultList();
    }

    @Override
    public void saveOrUpdate(SystemSetting setting) {
        if (setting == null) return;
        setting.setUpdatedAt(new Date());
        if (setting.getId() == null) {
            em.persist(setting);
        } else {
            em.merge(setting);
        }
    }
}
