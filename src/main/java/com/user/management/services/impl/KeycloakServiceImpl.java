package com.user.management.services.impl;

import com.user.management.dto.request.AdminUserRequestDTO;
import com.user.management.entity.UserType;
import com.user.management.services.KeycloakService;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.UserResource;
import org.springframework.beans.factory.annotation.Value;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakServiceImpl implements KeycloakService {


    private final Keycloak keycloak;
    @Value("${keycloak.realm}")
    private String realm;


    @Override
    public String createKeycloakUser(UUID id, AdminUserRequestDTO request, UserType userType){


        UserRepresentation user = new UserRepresentation();
        user.setId(id.toString());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEnabled(true);
        user.setEmailVerified(false);

        Response response = keycloak.realm(realm).users().create(user);


        if (response.getStatus() == 409) {
            String existingId = findIdByUsername(request.getUsername());
            sendWelcomeEmail(UUID.fromString(existingId));
            return id.toString();
        }

        if(response.getStatus()!=201){
            throw new RuntimeException("Keycloak user creation failed with status: " + response.getStatus());
        }

        String path = response.getLocation().getPath();
        String actualKeycloakId = path.substring(path.lastIndexOf('/') + 1);

        assignRealmRole(actualKeycloakId, userType.getRoleName());
        sendWelcomeEmail(UUID.fromString(actualKeycloakId));

        return actualKeycloakId;

    }

    private void assignRealmRole(String keycloakId, String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return;
        }

        RoleRepresentation role = keycloak.realm(realm)
                .roles()
                .get(roleName)
                .toRepresentation();

        keycloak.realm(realm)
                .users()
                .get(keycloakId)
                .roles()
                .realmLevel()
                .add(List.of(role));
    }
    @Override
    public void updateKeycloakStatus(UUID id,boolean enabled){
        UserResource userResource = keycloak.realm(realm).users().get(id.toString());
        UserRepresentation user = userResource.toRepresentation();
        user.setEnabled(enabled);
        userResource.update(user);
    }

    @Override
    public void deleteKeycloakUser(UUID id){
        try {
            keycloak.realm(realm).users().get(id.toString()).remove();
        } catch(NotFoundException e) {

            System.out.println("User already gone from Keycloak: " + id);
        }
    }

    @Override
    public void updateKeycloakUser(UUID id,AdminUserRequestDTO request){
        UserResource userResource = keycloak.realm(realm).
                users().get(id.toString());

        UserRepresentation user = userResource.toRepresentation();

        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        userResource.update(user);
    }

    @Override
    public void sendWelcomeEmail(UUID id) {
        try {
            keycloak.realm(realm)
                    .users()
                    .get(id.toString())
                    .executeActionsEmail(List.of("UPDATE_PASSWORD", "VERIFY_EMAIL"));
            System.out.println("Email sent successfully to: " + id);
        } catch (Exception e) {
            System.err.println("Failed to send email during recovery: " + e.getMessage());
        }
    }

    @Override
    public List<String> getRealmRoles() {
        return keycloak.realm(realm)
                .roles()
                .list()
                .stream()
                .map(RoleRepresentation::getName)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public boolean realmRoleExists(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return false;
        }

        try {
            keycloak.realm(realm)
                    .roles()
                    .get(roleName)
                    .toRepresentation();
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    @Override
    public String findIdByUsername(String username) {
        List<UserRepresentation> users = keycloak.realm(realm)
                .users()
                .search(username, true);

        if (users == null || users.isEmpty()) {
            return null;
        }

        // Return the ID that Keycloak officially uses
        return users.get(0).getId();
    }
}
