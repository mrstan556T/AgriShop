package com.agrishop.repository;

import com.agrishop.entity.SystemSetting;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface SystemSettingRepositoryLocal {
    SystemSetting findByKey(String key);
    List<SystemSetting> findAll();
    void saveOrUpdate(SystemSetting setting);
}
