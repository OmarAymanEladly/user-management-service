package com.user.management.audit.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AuditEvent {
    private String eventId;
    private String eventType;
    private Instant occurredAt;
    private String sourceService;
    private AuditEventData data;
}