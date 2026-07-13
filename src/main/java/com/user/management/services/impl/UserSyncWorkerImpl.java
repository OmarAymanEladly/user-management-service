package com.user.management.services.impl;

import com.user.management.dto.request.AdminUserRequestDTO;
import com.user.management.entity.ManagedUser;
import com.user.management.repository.ManagedUserRepository;
import com.user.management.services.KeycloakService;
import com.user.management.services.UserSyncWorker;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
@RequiredArgsConstructor
public class UserSyncWorkerImpl implements UserSyncWorker {

    private final ManagedUserRepository repository;
    private final KeycloakService keycloakService;

    @Scheduled(fixedDelay = 30000)
    public void retrySync() {
        List<ManagedUser> pendingUsers = repository.findBySyncStatus("PENDING_SYNC");

        for (ManagedUser user : pendingUsers) {
            try {
                // Convert Entity back to DTO briefly to use the shared function
                AdminUserRequestDTO request = mapEntityToRequest(user);

                // Use the SAME tool!
                keycloakService.createKeycloakUser(user.getId(), request);

                user.setSyncStatus("SYNCED");
                repository.save(user);
            } catch (Exception e) {
                System.err.println("Retry failed for " + user.getUsername());
            }
        }
    }

    private AdminUserRequestDTO mapEntityToRequest(ManagedUser user) {
        AdminUserRequestDTO request = new AdminUserRequestDTO();


        request.setUsername(user.getUsername());
        request.setEmail(user.getEmail());
        request.setFirstName(user.getFirstName());
        request.setLastName(user.getLastName());
        request.setPhoneNumber(user.getPhoneNumber());


        request.setEnabled(user.getEnabled());
        request.setUserTypeId(user.getUserType().getId());


        request.setAttributes(user.getAttributes());

        return request;
    }
}




