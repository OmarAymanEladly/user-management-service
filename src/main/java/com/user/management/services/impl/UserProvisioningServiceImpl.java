package com.user.management.services.impl;

import com.user.management.mapper.UserProvisioningMapper;
import com.user.management.model.entity.ManagedUser;
import com.user.management.model.entity.UserType;
import com.user.management.model.event.UserProvisioningEvent;
import com.user.management.repository.ManagedUserRepository;
import com.user.management.repository.UserTypeRepository;
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
    private final UserProvisioningMapper mapper;

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
                    ManagedUser newUser = mapper.toEntity(event);
                    newUser.setEnabled(true);
                    return newUser;
                });

        mapper.update(user, event);

        user.setSignupApprovalStatus("ACTIVE");
        user.setUserType(resolvedType);

        managedUserRepository.save(user);

        log.info("Upserted user [keycloakId={}, username={}, userType={}]",
                event.keycloakId(), event.username(), resolvedType.getType());
    }
}