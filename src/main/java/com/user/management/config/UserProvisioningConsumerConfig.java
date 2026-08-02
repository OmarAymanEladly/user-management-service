package com.user.management.config;

import com.user.management.model.event.UserProvisioningEvent;
import com.user.management.services.UserProvisioningService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
public class UserProvisioningConsumerConfig {

    private final UserProvisioningService provisioningService;

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
}