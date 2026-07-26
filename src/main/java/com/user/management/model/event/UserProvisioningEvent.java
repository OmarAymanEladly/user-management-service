package com.user.management.model.event;

import com.user.management.model.enumeration.EventSource;
import com.user.management.model.enumeration.ProvisioningEventType;

import java.time.Instant;

public record UserProvisioningEvent(
        ProvisioningEventType eventType,
        EventSource source,
        String keycloakId,
        String username,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        Instant timestamp
) {}