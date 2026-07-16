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

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void retrySync() {
        List<ManagedUser> pendingUsers = repository.findBySyncStatus("PENDING_SYNC");

        for (ManagedUser user : pendingUsers) {
            try {
                // Convert Entity back to DTO
                AdminUserRequestDTO request = mapEntityToRequest(user);

                keycloakService.createKeycloakUser(user.getId(), request, user.getUserType());

                user.setSyncStatus("SYNCED");
                repository.save(user);
                System.out.println("Worker: Successfully synced " + user.getUsername());

            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("409")) {
                    String realIdStr = keycloakService.findIdByUsername(user.getUsername());

                    if (realIdStr != null) {
                        UUID confirmedUUID = UUID.fromString(realIdStr);

                        keycloakService.sendWelcomeEmail(confirmedUUID);

                        if (!user.getId().equals(confirmedUUID)) {
                            // 1. Remove the old record with the wrong ID
                            repository.delete(user);
                            repository.flush();

                            // 2. Create the new record with the correct ID
                            ManagedUser newUser = ManagedUser.builder()
                                    .id(confirmedUUID)
                                    .username(user.getUsername())
                                    .email(user.getEmail())
                                    .firstName(user.getFirstName())
                                    .lastName(user.getLastName())
                                    .enabled(true)
                                    .userType(user.getUserType())
                                    .attributes(user.getAttributes())
                                    .syncStatus("SYNCED")
                                    .isNewUser(true)
                                    .build();

                            repository.save(newUser);
                            System.out.println("Worker: ID Mismatch fixed via recreation for " + user.getUsername());
                        } else {
                            user.setSyncStatus("SYNCED");
                            repository.save(user);
                        }
                    }
                } else {
                    System.err.println("Worker: Retry failed for " + user.getUsername() + ": " + e.getMessage());
                }
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