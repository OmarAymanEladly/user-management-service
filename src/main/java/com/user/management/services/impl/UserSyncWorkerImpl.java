package com.user.management.services.impl;

import com.user.management.dto.request.AdminUserRequestDTO;
import com.user.management.entity.ManagedUser;
import com.user.management.repository.ManagedUserRepository;
import com.user.management.services.KeycloakService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserSyncWorkerImpl {

    private final ManagedUserRepository repository;
    private final KeycloakService keycloakService;

    @Scheduled(fixedDelayString = "${user.sync.worker.delay}")
    @Transactional
    public void retrySync() {
        List<ManagedUser> pendingUsers = repository.findBySyncStatus("PENDING_SYNC");

        for (ManagedUser user : pendingUsers) {
            try {
                AdminUserRequestDTO request = mapEntityToRequest(user);

                // Try to create and get the REAL ID
                String confirmedId = keycloakService.createKeycloakUser(user.getId(), request, user.getUserType());

                // If success, sync the database
                handleIdSync(user, confirmedId);

            } catch (Exception e) {
                // Handle Conflict (409) - User already existed from a previous attempt
                if (e.getMessage() != null && e.getMessage().contains("409")) {
                    String realIdStr = keycloakService.findIdByUsername(user.getUsername());
                    if (realIdStr != null) {
                        keycloakService.sendWelcomeEmail(UUID.fromString(realIdStr));
                        handleIdSync(user, realIdStr);
                        System.out.println("Worker: Recovered 409 conflict for " + user.getUsername());
                    }
                } else {
                    System.err.println("Worker: Retry failed for " + user.getUsername() + ": " + e.getMessage());
                }
            }
        }
    }

    private void handleIdSync(ManagedUser user, String confirmedIdStr) {
        UUID confirmedUUID = UUID.fromString(confirmedIdStr);

        if (!user.getId().equals(confirmedUUID)) {
            // ID Mismatch: Delete old, create new
            String username = user.getUsername();
            repository.delete(user);
            repository.flush();

            ManagedUser newUser = buildNewUser(user, confirmedUUID);
            repository.save(newUser);

            System.out.println("Worker: ID Mismatch fixed via recreation for user: " + username);
        } else {
            // IDs match: Just update status
            user.setSyncStatus("SYNCED");
            repository.save(user);
            System.out.println("Worker: Successfully synced user: " + user.getUsername());
        }
    }

    private ManagedUser buildNewUser(ManagedUser oldUser, UUID newId) {
        return ManagedUser.builder()
                .id(newId)
                .username(oldUser.getUsername())
                .email(oldUser.getEmail())
                .firstName(oldUser.getFirstName())
                .lastName(oldUser.getLastName())
                .enabled(oldUser.getEnabled())
                .userType(oldUser.getUserType())
                .attributes(oldUser.getAttributes())
                .syncStatus("SYNCED")
                .isNewUser(true)
                .build();
    }

    private AdminUserRequestDTO mapEntityToRequest(ManagedUser user) {
        AdminUserRequestDTO request = new AdminUserRequestDTO();
        request.setUsername(user.getUsername());
        request.setEmail(user.getEmail());
        request.setFirstName(user.getFirstName());
        request.setLastName(user.getLastName());
        request.setUserTypeId(user.getUserType().getId());
        request.setAttributes(user.getAttributes());
        request.setEnabled(user.getEnabled());
        return request;
    }
}