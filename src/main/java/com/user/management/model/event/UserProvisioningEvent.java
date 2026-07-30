package com.user.management.model.event;

import com.user.management.model.enumeration.EventSource;
import com.user.management.model.enumeration.ProvisioningEventType;

import java.time.Instant;
import java.util.Map;

public record UserProvisioningEvent(
        ProvisioningEventType eventType,
        EventSource source,
        String keycloakId,
        String username,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        Map<String, String> attributes,
        Instant timestamp
) {}