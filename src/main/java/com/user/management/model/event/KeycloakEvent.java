package com.user.management.model.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeycloakEvent {
    private String eventType;

    private String realmId;
    @JsonProperty("keycloakId")
    private String userId;
    private Map<String, String> details;
}