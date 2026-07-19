package com.user.management.services.impl;

import com.user.management.dto.request.AdminUserRequestDTO;
import com.user.management.entity.ManagedUser;
import com.user.management.entity.UserType;
import com.user.management.repository.ManagedUserRepository;
import com.user.management.services.KeycloakService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserSyncWorkerImplTest {

    @Mock
    private ManagedUserRepository repository;

    @Mock
    private KeycloakService keycloakService;

    @InjectMocks
    private UserSyncWorkerImpl worker;

    @Test
    void retrySync_SuccessPath() {

        UUID localId = UUID.randomUUID();
        UserType type = UserType.builder().id(UUID.randomUUID()).build();
        ManagedUser user = ManagedUser.builder()
                .id(localId).username("bob").syncStatus("PENDING_SYNC").userType(type).build();

        when(repository.findBySyncStatus("PENDING_SYNC")).thenReturn(List.of(user));
        when(keycloakService.createKeycloakUser(eq(localId), any(), any())).thenReturn(localId.toString());


        worker.retrySync();


        verify(repository).save(argThat(u -> u.getSyncStatus().equals("SYNCED")));
        System.out.println("Test: Successful Sync Verified");
    }

    @Test
    void retrySync_ConflictAndHealPath() {
        // Arrange: Local DB has ID_A, but Keycloak has ID_B
        UUID idA = UUID.randomUUID();
        UUID idB = UUID.randomUUID();
        UserType type = UserType.builder().id(UUID.randomUUID()).build();
        ManagedUser userA = ManagedUser.builder()
                .id(idA).username("bob").syncStatus("PENDING_SYNC").userType(type).build();

        when(repository.findBySyncStatus("PENDING_SYNC")).thenReturn(List.of(userA));

        // 1. Keycloak returns 409 Conflict
        when(keycloakService.createKeycloakUser(any(), any(), any()))
                .thenThrow(new RuntimeException("HTTP 409 Conflict"));

        // 2. Worker asks for the real ID and gets ID_B
        when(keycloakService.findIdByUsername("bob")).thenReturn(idB.toString());

        // Act
        worker.retrySync();

        // Assert
        verify(keycloakService).sendWelcomeEmail(idB);
        verify(repository).delete(userA); // Must delete the record with ID_A
        verify(repository).save(argThat(u -> u.getId().equals(idB))); // Must save new record with ID_B
        System.out.println("Test: Self-Healing ID Mismatch Verified");
    }

    @Test
    void retrySync_StillDownPath() {
        UserType dummyType = UserType.builder().id(UUID.randomUUID()).build();

        ManagedUser user = ManagedUser.builder()
                .username("bob")
                .syncStatus("PENDING_SYNC")
                .userType(dummyType) // <--- ADD THIS
                .build();

        when(repository.findBySyncStatus("PENDING_SYNC")).thenReturn(List.of(user));


        when(keycloakService.createKeycloakUser(any(), any(), any()))
                .thenThrow(new RuntimeException("Connection Refused"));


        worker.retrySync();


        verify(repository, never()).save(argThat(u -> "SYNCED".equals(u.getSyncStatus())));
        System.out.println("Test: Resiliency (Still Down) Verified");
    }



}