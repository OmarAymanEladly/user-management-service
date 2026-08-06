package com.user.management.model.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeycloakEvent {
    private String eventType;
    private String keycloakId;
    private String realmId;
    private String userId;
    private Map<String, String> details;
}