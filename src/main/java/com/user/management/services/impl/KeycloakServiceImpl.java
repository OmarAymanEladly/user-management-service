package com.user.management.services.impl;

import com.user.management.dto.request.AdminUserRequestDTO;
import com.user.management.services.KeycloakService;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.resource.UserResource;
import org.springframework.beans.factory.annotation.Value;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KeycloakServiceImpl implements KeycloakService {


    private final Keycloak keycloak;
    @Value("${keycloak.realm}")
    private String realm;


    @Override
    public String createKeycloakUser(UUID id, AdminUserRequestDTO request){

        UserRepresentation user = new UserRepresentation();
        user.setId(id.toString());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEnabled(true);
        user.setEmailVerified(false);

        Response response = keycloak.realm(realm).users().create(user);

        if(response.getStatus()!=201){
            throw new RuntimeException("Keycloak user creation failed with status: " + response.getStatus());
        }

        String path = response.getLocation().getPath();
        String keycloakId = path.substring(path.lastIndexOf('/') + 1);


        try {
            keycloak.realm(realm)
                    .users()
                    .get(keycloakId)
                    .executeActionsEmail(List.of("UPDATE_PASSWORD","VERIFY_EMAIL"));
        } catch (Exception e) {

            System.err.println("Keycloak is UP, but SMTP is not configured: " + e.getMessage());
        }

        return keycloakId;

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
}
