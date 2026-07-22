package com.user.management.services;

import java.util.UUID;

public interface OutboxService {
    void saveEvent(UUID aggregateId, String aggregateType, String eventType, Object payload, String status);
}