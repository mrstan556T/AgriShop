package com.agrishop.service;

import com.agrishop.dto.SystemSettingDTO;
import jakarta.ejb.Local;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Local
public interface SystemSettingServiceLocal {
    List<SystemSettingDTO> getAllSettings();
    String getSettingValue(String key, String defaultValue);
    BigDecimal getBigDecimalSetting(String key, BigDecimal defaultValue);
    int getIntSetting(String key, int defaultValue);
    boolean getBooleanSetting(String key, boolean defaultValue);
    void updateSetting(String key, String value) throws Exception;
    void updateSettings(Map<String, String> settingsMap) throws Exception;
}
