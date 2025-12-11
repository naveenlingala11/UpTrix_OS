package com.uptrix.uptrix_backend.service;

import com.uptrix.uptrix_backend.dto.audit.AuditLogDto;
import com.uptrix.uptrix_backend.entity.audit.AuditLog;
import com.uptrix.uptrix_backend.repository.AuditLogRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Main explicit API for logging a super admin action.
     *
     * @param action     short code like "TENANT_PAUSED"
     * @param targetType logical target type like "COMPANY"
     * @param targetId   target ID
     * @param targetName target display name
     * @param details    free text details
     */
    public void logSuperAdminAction(
            String action,
            String targetType,
            Long targetId,
            String targetName,
            String details
    ) {
        AuditLog log = new AuditLog();

        // Actor (from security context)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            log.setActorUsername(auth.getName());

            String role = null;
            if (auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()) {
                GrantedAuthority first = auth.getAuthorities().iterator().next();
                role = first.getAuthority();
            }
            log.setActorRole(role);
        } else {
            log.setActorUsername("system");
            log.setActorRole(null);
        }

        log.setAction(action != null ? action : "UNKNOWN_ACTION");

        // entityType column is NOT NULL in DB → always set something
        String safeTargetType = (targetType != null && !targetType.isBlank())
                ? targetType
                : "UNKNOWN";

        log.setEntityType(safeTargetType);
        log.setTargetType(safeTargetType);

        log.setTargetId(targetId);
        log.setTargetName(targetName);
        log.setDetails(details);

        auditLogRepository.save(log);
    }

    /**
     * Generic/legacy logging entry point.
     *
     * This exists to keep older auditLogService.log(...) calls compiling.
     * It simply normalises arguments and delegates to logSuperAdminAction().
     *
     * Common patterns it can support:
     *  - log("TENANT_PAUSED", "COMPANY", 1L, "Acme", "Paused by admin");
     *  - log("TENANT_CREATED", "Created tenant Acme");
     */
    @Deprecated
    public void log(Object... args) {
        if (args == null || args.length == 0) {
            // Nothing useful – write an UNKNOWN entry so DB insert still works
            logSuperAdminAction("UNKNOWN", "UNKNOWN", null, null, null);
            return;
        }

        // Try to interpret common usages:
        // 1) action, targetType, targetId, targetName, details
        if (args.length >= 5) {
            String action = args[0] != null ? args[0].toString() : "UNKNOWN";
            String targetType = args[1] != null ? args[1].toString() : "UNKNOWN";

            Long targetId = null;
            if (args[2] instanceof Number num) {
                targetId = num.longValue();
            }

            String targetName = args[3] != null ? args[3].toString() : null;
            String details = args[4] != null ? args[4].toString() : null;

            logSuperAdminAction(action, targetType, targetId, targetName, details);
            return;
        }

        // 2) action, details
        if (args.length == 2) {
            String action = args[0] != null ? args[0].toString() : "UNKNOWN";
            String details = args[1] != null ? args[1].toString() : null;
            logSuperAdminAction(action, "UNKNOWN", null, null, details);
            return;
        }

        // 3) action only
        if (args.length == 1) {
            String action = args[0] != null ? args[0].toString() : "UNKNOWN";
            logSuperAdminAction(action, "UNKNOWN", null, null, null);
            return;
        }

        // Fallback for any other weird pattern
        logSuperAdminAction("UNKNOWN", "UNKNOWN", null, null, args.toString());
    }

    public List<AuditLogDto> getRecentLogs(int limit) {
        List<AuditLog> logs = auditLogRepository.findTop50ByOrderByCreatedAtDesc();
        List<AuditLogDto> dtos = new ArrayList<>();

        int count = 0;
        for (AuditLog log : logs) {
            if (count >= limit) break;
            dtos.add(toDto(log));
            count++;
        }
        return dtos;
    }

    private AuditLogDto toDto(AuditLog log) {
        AuditLogDto dto = new AuditLogDto();
        dto.setId(log.getId());
        dto.setCreatedAt(log.getCreatedAt() != null ? log.getCreatedAt().toString() : null);
        dto.setActorUsername(log.getActorUsername());
        dto.setActorRole(log.getActorRole());
        dto.setAction(log.getAction());
        dto.setTargetType(log.getTargetType());
        dto.setTargetId(log.getTargetId());
        dto.setTargetName(log.getTargetName());
        dto.setDetails(log.getDetails());
        return dto;
    }
}
