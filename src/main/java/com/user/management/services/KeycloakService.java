package com.user.management.services;

import com.user.management.dto.request.AdminUserRequestDTO;
import com.user.management.entity.UserType;

import java.util.List;
import java.util.UUID;

public interface KeycloakService {

    String createKeycloakUser(UUID id, AdminUserRequestDTO request, UserType userType);
    void updateKeycloakStatus(UUID id,boolean enabled);
    void updateKeycloakUser(UUID id,AdminUserRequestDTO request);
    void deleteKeycloakUser(UUID id);
    List<String> getRealmRoles();
    boolean realmRoleExists(String roleName);
    void sendWelcomeEmail(UUID id);
    String findIdByUsername(String username);
    void syncUserTypeAttributes(UserType userType);
    void cleanupUserTypeAttributes(String typeName);
    List<String> getUserRoles(UUID userId);
    void assignRolesToUser(UUID userId, List<String> roleNames);


}
