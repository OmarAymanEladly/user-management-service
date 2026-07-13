package com.user.management.services;

import com.user.management.dto.request.AdminUserRequestDTO;

import java.util.UUID;

public interface KeycloakService {

    String createKeycloakUser(UUID id, AdminUserRequestDTO request);
    void updateKeycloakStatus(UUID id,boolean enabled);
    void updateKeycloakUser(UUID id,AdminUserRequestDTO request);
    void deleteKeycloakUser(UUID id);


}
