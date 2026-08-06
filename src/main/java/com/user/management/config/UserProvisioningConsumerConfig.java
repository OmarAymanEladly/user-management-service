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
import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class UserProvisioningConsumerConfig {

    private final UserProvisioningService provisioningService;
    private final ObjectMapper objectMapper;

    @Bean
    public Consumer<UserProvisioningEvent> userProvisioningConsumer() {
        return event -> {
            switch (event.eventType()) {
                case USER_CREATED -> provisioningService.handleUserCreated(event);
                case USER_UPDATED -> provisioningService.handleUserUpdated(event);
                case USER_DELETED -> provisioningService.handleUserDeleted(event);
            }
            System.out.println("Processed event: " + event);
        };
    }

    @Bean
    public Consumer<Message<String>> userStatusConsumer() {
        return message -> {
            String payload = message.getPayload();
            log.info(">>>> KAFKA [Status]: RAW MESSAGE RECEIVED: {}", payload);

            try {

                KeycloakEvent event = objectMapper.readValue(payload, KeycloakEvent.class);
                log.info(">>>> KAFKA [Status]: Successfully parsed JSON into KeycloakEvent. Type: {}", event.getEventType());


                provisioningService.handleKeycloakEvent(event);

            } catch (Exception e) {
                log.error("!!!! KAFKA [Status]: FAILED TO PARSE JSON. Error: {}", e.getMessage());
                log.error("!!!! KAFKA [Status]: Raw payload was: {}", payload);
            }
        };
    }

}