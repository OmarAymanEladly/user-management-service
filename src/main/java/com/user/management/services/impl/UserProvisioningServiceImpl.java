package com.user.management.services.impl;

import com.user.management.mapper.UserProvisioningMapper;
import com.user.management.model.entity.ManagedUser;
import com.user.management.model.entity.UserType;
import com.user.management.model.event.UserProvisioningEvent;
import com.user.management.repository.ManagedUserRepository;
import com.user.management.repository.UserTypeRepository;
import com.user.management.services.UserProvisioningService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProvisioningServiceImpl implements UserProvisioningService {

    private final ManagedUserRepository managedUserRepository;
    private final UserTypeRepository userTypeRepository;
    private final UserProvisioningMapper mapper;

    @Override
    public void handleUserCreated(UserProvisioningEvent event) {
        upsert(event);
    }

    @Override
    public void handleUserUpdated(UserProvisioningEvent event) {
        upsert(event);
    }

    @Override
    public void handleUserDeleted(UserProvisioningEvent event) {
        managedUserRepository.deleteById(UUID.fromString(event.keycloakId()));
    }

    private void upsert(UserProvisioningEvent event) {
        UUID userId = UUID.fromString(event.keycloakId());

        ManagedUser user = managedUserRepository.findById(userId)
                .orElseGet(() -> {
                    ManagedUser newUser = mapper.toEntity(event);

                    UserType ldapType = userTypeRepository.findByType("Provisioned")
                            .orElseThrow(() -> new IllegalStateException("Provisioned UserType not found"));

                    newUser.setUserType(ldapType);
                    newUser.setEnabled(true);

                    return newUser;
                });

        mapper.update(user, event);
        managedUserRepository.save(user);
    }

}