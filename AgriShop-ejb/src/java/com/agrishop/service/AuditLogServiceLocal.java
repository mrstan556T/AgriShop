package com.agrishop.service;

import com.agrishop.dto.AuditLogDTO;
import jakarta.ejb.Local;
import java.util.List;
import java.util.Map;

@Local
public interface AuditLogServiceLocal {
    void log(Long userId, String action, String entityName, Long entityId, String oldValue, String newValue);
    List<AuditLogDTO> getAuditLogsLazy(int first, int pageSize, String sortField, String sortOrder, Map<String, Object> filters);
    int countAuditLogs(Map<String, Object> filters);
}
