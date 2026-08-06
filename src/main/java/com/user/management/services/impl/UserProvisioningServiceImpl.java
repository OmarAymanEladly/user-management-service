package com.user.management.services.impl;

import com.user.management.mapper.UserProvisioningMapper;
import com.user.management.model.entity.ManagedUser;
import com.user.management.model.entity.UserType;
<<<<<<< HEAD
import com.user.management.model.event.KeycloakEvent;
import com.user.management.model.event.UserProvisioningEvent;
import com.user.management.repository.ManagedUserRepository;
import com.user.management.repository.UserTypeRepository;
import com.user.management.services.KeycloakService;
=======
import com.user.management.model.enumeration.EventSource;
import com.user.management.model.event.UserProvisioningEvent;
import com.user.management.repository.ManagedUserRepository;
import com.user.management.services.OutboxService;
>>>>>>> 9082cad52326dd5df1a5ed89e2b8aec65a0d54c2
import com.user.management.services.UserProvisioningService;
import com.user.management.services.UserTypeMappingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserProvisioningServiceImpl implements UserProvisioningService {

    private final ManagedUserRepository managedUserRepository;
    private final UserTypeMappingService userTypeMappingService;
<<<<<<< HEAD
    private final UserProvisioningMapper mapper;
    private final KeycloakService keycloakService;
=======
    private final UserProvisioningMapper userProvisioningMapper;
    private final OutboxService outboxService;
>>>>>>> 9082cad52326dd5df1a5ed89e2b8aec65a0d54c2

    @Override
    public void handleUserCreated(UserProvisioningEvent event) {
        log.info("Handling USER_CREATED [keycloakId={}, username={}, source={}]",
                event.keycloakId(), event.username(), event.source());
        upsert(event);
    }

    @Override
    public void handleUserUpdated(UserProvisioningEvent event) {
        log.info("Handling USER_UPDATED [keycloakId={}, username={}, source={}]",
                event.keycloakId(), event.username(), event.source());
        upsert(event);
    }

    @Override
    public void handleUserDeleted(UserProvisioningEvent event) {
        log.info("Handling USER_DELETED [keycloakId={}, source={}]",
                event.keycloakId(), event.source());

        UUID id = UUID.fromString(event.keycloakId());
        managedUserRepository.findById(id).ifPresent(managedUserRepository::delete);
    }

    private void upsert(UserProvisioningEvent event) {
        UUID userId = UUID.fromString(event.keycloakId());

        UserType resolvedType = userTypeMappingService.mapUserType(event);

        ManagedUser user = managedUserRepository.findById(userId)
                .orElseGet(() -> {
<<<<<<< HEAD
                    ManagedUser newUser = mapper.toEntity(event);
=======
                    ManagedUser newUser = userProvisioningMapper.toEntity(event);
>>>>>>> 9082cad52326dd5df1a5ed89e2b8aec65a0d54c2
                    newUser.setEnabled(true);
                    return newUser;
                });

<<<<<<< HEAD
        mapper.update(user, event);
=======
        boolean userTypeChanged =
                user.getUserType() == null ||
                        !user.getUserType().getType().equals(resolvedType.getType());

        userProvisioningMapper.update(user, event);
>>>>>>> 9082cad52326dd5df1a5ed89e2b8aec65a0d54c2

        user.setSignupApprovalStatus("ACTIVE");
        user.setUserType(resolvedType);

        managedUserRepository.save(user);

        log.info("Upserted user [keycloakId={}, username={}, userType={}]",
                event.keycloakId(), event.username(), resolvedType.getType());
<<<<<<< HEAD
    }

    @Override
    public void handleKeycloakEvent(KeycloakEvent event) {
        log.info("==> SERVICE: Processing Keycloak Event [Type: {}, User: {}]", event.getEventType(), event.getUserId());

        if (event.getUserId() == null) {
            log.warn("==> SERVICE: userId is null in event. Skipping.");
            return;
        }

        UUID userId = UUID.fromString(event.getUserId());

        if ("LOGIN_ERROR".equals(event.getEventType()) || "USER_DISABLED_BY_TEMPORARY_LOCKOUT".equals(event.getEventType())) {
            log.info("==> SERVICE: Potential block detected. Checking Keycloak API for block status...");

            boolean isBlocked = keycloakService.isUserBlocked(userId);
            log.info("==> SERVICE: Keycloak API reports isUserBlocked = {}", isBlocked);

            if (isBlocked) {
                managedUserRepository.findById(userId).ifPresentOrElse(user -> {
                    if (user.getEnabled()) {
                        user.setEnabled(false);
                        managedUserRepository.save(user);
                        log.warn("==> SERVICE: USER {} IS NOW DISABLED IN LOCAL DB.", user.getUsername());
                    } else {
                        log.info("==> SERVICE: User {} is already disabled. No action needed.", user.getUsername());
                    }
                }, () -> log.error("==> SERVICE: Block event for user {} but user not found in local DB!", userId));
            }
        }
        else if ("LOGIN".equals(event.getEventType())) {
            log.info("==> SERVICE: Successful login. Re-enabling user if needed...");
            managedUserRepository.findById(userId).ifPresent(user -> {
                if (!user.getEnabled()) {
                    user.setEnabled(true);
                    managedUserRepository.save(user);
                    log.info("==> SERVICE: USER {} IS NOW RE-ENABLED IN LOCAL DB.", user.getUsername());
                }
            });
        } else {
            log.debug("==> SERVICE: Ignoring event type: {}", event.getEventType());
=======

        // Sync userType back to Keycloak
        if (userTypeChanged && event.source().equals(EventSource.LDAP)) {
            outboxService.saveEvent(user.getId(), "USER", "USER_UPDATED", userProvisioningMapper.toAdminRequest(user), "PENDING");
>>>>>>> 9082cad52326dd5df1a5ed89e2b8aec65a0d54c2
        }
    }
}