package com.user.management.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.management.dto.request.AdminUserRequestDTO;
import com.user.management.entity.ManagedUser;
import com.user.management.entity.OutboxEvent;
import com.user.management.repository.ManagedUserRepository;
import com.user.management.repository.OutboxEventRepository;
import com.user.management.services.KeycloakService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxProcessor {

    private final OutboxEventRepository outboxRepository;
    private final ManagedUserRepository userRepository;
    private final KeycloakService keycloakService;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${app.outbox.fixed-delay-ms}")
    @Transactional
    public void processPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxRepository.findByStatusOrderByCreatedAtAsc("PENDING");

        for (OutboxEvent event : pendingEvents) {
            try {
                syncToKeycloak(event);
                event.setStatus("PROCESSED");
                outboxRepository.save(event);
            } catch (Exception e) {
                log.error("Failed to sync event {} to Keycloak: {}", event.getId(), e.getMessage());
                event.setRetryCount(event.getRetryCount() + 1);
                event.setLastError(e.getMessage());

                if (event.getRetryCount() >= 5) {
                    event.setStatus("FAILED");
                }
                outboxRepository.save(event);
                break;
            }
        }
    }

    private void syncToKeycloak(OutboxEvent event) throws Exception {
        AdminUserRequestDTO request = null;
        if (!"{}".equals(event.getPayload()) && event.getPayload() != null) {
            request = objectMapper.readValue(event.getPayload(), AdminUserRequestDTO.class);
        }

        UUID aggregateId = event.getAggregateId();

        switch (event.getEventType()) {
            case "USER_CREATED":
                log.info("Syncing creation for user: {}", aggregateId);
                ManagedUser localUser = userRepository.findById(aggregateId)
                        .orElseThrow(() -> new RuntimeException("Local user not found"));


                String realIdStr = keycloakService.createKeycloakUser(aggregateId, request, localUser.getUserType());
                UUID realId = UUID.fromString(realIdStr);


                if (!aggregateId.equals(realId)) {
                    log.warn("Healing ID Mismatch. Deleting {} and creating {}.", aggregateId, realId);

                    userRepository.delete(localUser);
                    userRepository.flush(); // Clear the username immediately

                    ManagedUser fixedUser = ManagedUser.builder()
                            .id(realId)
                            .username(localUser.getUsername())
                            .email(localUser.getEmail())
                            .firstName(localUser.getFirstName())
                            .lastName(localUser.getLastName())
                            .userType(localUser.getUserType())
                            .attributes(localUser.getAttributes())
                            .enabled(localUser.getEnabled())
                            .isNewUser(true)
                            .build();

                    userRepository.save(fixedUser);
                }
                break;

            case "USER_UPDATED":
                if (request != null) {
                    String targetId = resolveKeycloakId(aggregateId, request.getUsername());
                    log.info("Syncing update for user: {}", targetId);

                    keycloakService.updateKeycloakUser(UUID.fromString(targetId), request);
                }
                break;

            case "USER_DEACTIVATED":
                String deactivateId = resolveKeycloakId(aggregateId, getUsernameFromPayload(request, aggregateId));
                log.info("Syncing deactivation for user: {}", deactivateId);

                keycloakService.updateKeycloakStatus(UUID.fromString(deactivateId), false);
                break;

            case "USER_ACTIVATED":
                String activateId = resolveKeycloakId(aggregateId, getUsernameFromPayload(request, aggregateId));
                log.info("Syncing activation for user: {}", activateId);

                keycloakService.updateKeycloakStatus(UUID.fromString(activateId), true);
                break;

            case "USER_DELETED":
                String deleteUsername = request != null ? request.getUsername() : null;
                String deleteId = resolveKeycloakId(aggregateId, deleteUsername);
                log.info("Syncing deletion for user: {}", deleteId);

                keycloakService.deleteKeycloakUser(UUID.fromString(deleteId));
                break;

            default:
                throw new IllegalArgumentException("Unknown event type: " + event.getEventType());
        }
    }


    private String resolveKeycloakId(UUID aggregateId, String username) {
        if (username != null && !username.isBlank()) {
            String keycloakId = keycloakService.findIdByUsername(username);
            if (keycloakId != null) {
                return keycloakId;
            }
        }

        return aggregateId.toString();
    }

    private String getUsernameFromPayload(AdminUserRequestDTO request, UUID aggregateId) {
        if (request != null && request.getUsername() != null) {
            return request.getUsername();
        }
        return userRepository.findById(aggregateId)
                .map(ManagedUser::getUsername)
                .orElse(null);
    }
}