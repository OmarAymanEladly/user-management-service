package com.user.management.audit.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AuditEventData {
    private String actionType;
    private Actor actor;
    private Resource resource;
    private String outcome;    // "SUCCESS" or "FAILURE"
    private String reason;     // null on success, exception message on failure
    private String correlationId;
    private Map<String, Object> metadata;
}
