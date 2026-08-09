package com.user.management.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.management.model.event.KeycloakEvent;
import com.user.management.model.event.UserProvisioningEvent;
import com.user.management.repository.ManagedUserRepository;
import com.user.management.services.KeycloakService;
import com.user.management.services.UserProvisioningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.UUID;

import com.user.management.model.event.UserProvisioningEvent;
import com.user.management.services.UserProvisioningService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor

@Slf4j
public class UserProvisioningConsumerConfig {

    private final UserProvisioningService provisioningService;
    private final ObjectMapper objectMapper;


    @Bean
    public Consumer<Message<byte[]>> userProvisioningConsumer() {
        return message -> {
            try {
                String json = new String(message.getPayload());
                var root = objectMapper.readTree(json);

                if (!root.has("eventType")) return;
                String typeStr = root.get("eventType").asText();


                if (typeStr.contains("BLOCKED")) {
                    KeycloakEvent statusEvent = objectMapper.readValue(json, KeycloakEvent.class);
                    log.info(">>>> KAFKA: Routing to Status Handler: {}", typeStr);
                    provisioningService.handleKeycloakEvent(statusEvent);
                }

                else {
                    UserProvisioningEvent provEvent = objectMapper.readValue(json, UserProvisioningEvent.class);
                    log.info(">>>> KAFKA: Routing to Provisioning Handler: {}", provEvent.eventType());

                    switch (provEvent.eventType()) {
                        case USER_CREATED -> provisioningService.handleUserCreated(provEvent);
                        case USER_UPDATED -> provisioningService.handleUserUpdated(provEvent);
                        case USER_DELETED -> provisioningService.handleUserDeleted(provEvent);
                    }
                }
            } catch (Exception e) {
                log.error("!!!! KAFKA Error: {}", e.getMessage());
            }
        };
    }
}