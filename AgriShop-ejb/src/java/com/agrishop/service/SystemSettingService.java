package com.agrishop.service;

import com.agrishop.dto.SystemSettingDTO;
import com.agrishop.entity.SystemSetting;
import com.agrishop.repository.SystemSettingRepositoryLocal;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Stateless
public class SystemSettingService implements SystemSettingServiceLocal {

    @EJB
    private SystemSettingRepositoryLocal settingRepository;

    @EJB
    private AuditLogServiceLocal auditLogService;

    @Override
    public List<SystemSettingDTO> getAllSettings() {
        List<SystemSetting> entities = settingRepository.findAll();
        List<SystemSettingDTO> dtoList = new ArrayList<>();
        if (entities != null) {
            for (SystemSetting s : entities) {
                dtoList.add(new SystemSettingDTO(s.getId(), s.getSettingKey(), s.getSettingValue(), s.getDescription(), s.getUpdatedAt()));
            }
        }
        return dtoList;
    }

    @Override
    public String getSettingValue(String key, String defaultValue) {
        SystemSetting s = settingRepository.findByKey(key);
        if (s != null && s.getSettingValue() != null && !s.getSettingValue().trim().isEmpty()) {
            return s.getSettingValue().trim();
        }
        return defaultValue;
    }

    @Override
    public BigDecimal getBigDecimalSetting(String key, BigDecimal defaultValue) {
        String val = getSettingValue(key, null);
        if (val != null) {
            try {
                return new BigDecimal(val.trim());
            } catch (Exception ignored) {
            }
        }
        return defaultValue;
    }

    @Override
    public int getIntSetting(String key, int defaultValue) {
        String val = getSettingValue(key, null);
        if (val != null) {
            try {
                return Integer.parseInt(val.trim());
            } catch (Exception ignored) {
            }
        }
        return defaultValue;
    }

    @Override
    public boolean getBooleanSetting(String key, boolean defaultValue) {
        String val = getSettingValue(key, null);
        if (val != null) {
            String clean = val.trim().toLowerCase();
            return "true".equals(clean) || "1".equals(clean) || "yes".equals(clean) || "on".equals(clean);
        }
        return defaultValue;
    }

    @Override
    public void updateSetting(String key, String value) throws Exception {
        if (key == null || key.trim().isEmpty()) {
            throw new Exception("Mã khóa cấu hình không được để trống.");
        }
        SystemSetting s = settingRepository.findByKey(key.trim());
        String oldValue = (s != null && s.getSettingValue() != null) ? s.getSettingValue() : "";
        if (s == null) {
            s = new SystemSetting(key.trim(), value, "");
        } else {
            s.setSettingValue(value != null ? value.trim() : "");
        }
        settingRepository.saveOrUpdate(s);

        if (auditLogService != null) {
            auditLogService.log(null, "UPDATE_SETTING", "SystemSettings", s.getId(),
                "key=" + key.trim() + ", val=" + oldValue,
                "key=" + key.trim() + ", val=" + (value != null ? value.trim() : ""));
        }
    }

    @Override
    public void updateSettings(Map<String, String> settingsMap) throws Exception {
        if (settingsMap == null || settingsMap.isEmpty()) return;
        for (Map.Entry<String, String> entry : settingsMap.entrySet()) {
            updateSetting(entry.getKey(), entry.getValue());
        }
    }
}
