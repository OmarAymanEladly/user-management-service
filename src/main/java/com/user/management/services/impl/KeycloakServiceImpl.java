package com.user.management.services.impl;

import com.user.management.dto.request.AdminUserRequestDTO;
import com.user.management.entity.FieldDefinition;
import com.user.management.entity.UserType;
import com.user.management.repository.UserTypeRepository;
import com.user.management.services.KeycloakService;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.ClientScopeRepresentation;
import org.keycloak.representations.userprofile.config.UPAttribute;
import org.keycloak.representations.userprofile.config.UPAttributePermissions;
import org.keycloak.representations.userprofile.config.UPConfig;
import org.keycloak.representations.userprofile.config.UPGroup;
import org.springframework.beans.factory.annotation.Value;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import org.keycloak.representations.userprofile.config.UPAttributeSelector;

/*
import org.keycloak.representations.idm.userprofile.UPConfig;
import org.keycloak.representations.idm.userprofile.UPAttribute;
import org.keycloak.representations.idm.userprofile.UPGroup;
*/

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakServiceImpl implements KeycloakService {


    private final Keycloak keycloak;
    private final UserTypeRepository userTypeRepository;

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

        user.setAttributes(mapToKeycloakAttributes(request.getAttributes(),userType));

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

        UserType userType = userTypeRepository.findById(request.getUserTypeId())
                .orElseThrow(() -> new RuntimeException("UserType not found"));

        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        user.setAttributes(mapToKeycloakAttributes(request.getAttributes(),userType));

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

    @Override
    public void syncUserTypeAttributes(UserType userType) {
        UPConfig config = keycloak.realm(realm).users().userProfile().getConfiguration();
        List<UPAttribute> attributesList = config.getAttributes() == null ? new ArrayList<>() : new ArrayList<>(config.getAttributes());
        List<UPGroup> groupsList = config.getGroups() == null ? new ArrayList<>() : new ArrayList<>(config.getGroups());

        String typeName = userType.getType().toUpperCase().trim();
        String groupName = userType.getType().toLowerCase() + "-group";


        attributesList.removeIf(attr -> groupName.equals(attr.getGroup()));


        groupsList.removeIf(g -> g.getName().equals(groupName));
        UPGroup newGroup = new UPGroup();
        newGroup.setName(groupName);
        newGroup.setDisplayHeader(typeName + " Details");
        groupsList.add(newGroup);
        config.setGroups(groupsList);


        for (var field : userType.getFields()) {
            if (!field.isSyncToKeycloak()) {
                log.debug("Skipping field {} as showInKeycloak is false", field.getFieldName());
                continue;
            }

            UPAttribute attribute = new UPAttribute();
            String attrKey = field.getFieldName().toLowerCase().trim();
            attribute.setName(attrKey);
            attribute.setDisplayName(field.getDisplayName());
            attribute.setGroup(groupName);

            UPAttributePermissions permissions = new UPAttributePermissions();
            permissions.setView(Set.of("admin", "user"));
            permissions.setEdit(Set.of("admin"));
            attribute.setPermissions(permissions);

            attributesList.add(attribute);
        }

        config.setAttributes(attributesList);
        keycloak.realm(realm).users().userProfile().update(config);
    }




    private Map<String, List<String>> mapToKeycloakAttributes(Map<String, Object> attributes, UserType userType) {
        Map<String, List<String>> kcAttributes = new HashMap<>();
        if (userType == null) return kcAttributes;

        kcAttributes.put("user_type", List.of(userType.getType().toUpperCase()));

        if (attributes == null) return kcAttributes;


        Set<String> allowedKeycloakFields = userType.getFields().stream()
                .filter(FieldDefinition::isSyncToKeycloak)
                .map(f -> f.getFieldName().toLowerCase().trim())
                .collect(Collectors.toSet());

        attributes.forEach((key, value) -> {
            String cleanKey = key.toLowerCase().trim();
            if (allowedKeycloakFields.contains(cleanKey) && value != null && !value.toString().isBlank()) {
                kcAttributes.put(cleanKey, List.of(value.toString()));
            }
        });
        return kcAttributes;
    }


    @Override
    public void cleanupUserTypeAttributes(String typeName) {
        // FIX: Check if the type was recreated before performing cleanup
        if (userTypeRepository.findByType(typeName.toUpperCase()).isPresent() ||
                userTypeRepository.findByType(typeName.toLowerCase()).isPresent()) {
            log.warn("Cleanup skipped for type '{}' because it currently exists in the database (re-creation detected).", typeName);
            return;
        }

        UPConfig config = keycloak.realm(realm).users().userProfile().getConfiguration();
        String cleanTypeName = typeName.toLowerCase().trim();
        String groupName = cleanTypeName + "-group";

        // Remove attributes and group
        if (config.getAttributes() != null) {
            config.getAttributes().removeIf(attr -> groupName.equals(attr.getGroup()));
        }
        if (config.getGroups() != null) {
            config.getGroups().removeIf(group -> groupName.equals(group.getName()));
        }

        try {
            keycloak.realm(realm).users().userProfile().update(config);
            log.info("Successfully removed attributes and group '{}' from Keycloak schema", groupName);
        } catch (Exception e) {
            log.error("Failed to update User Profile schema during cleanup: {}", e.getMessage());
        }
    }

    @Override
    public List<String> getUserRoles(UUID userId){
        return keycloak.realm(realm).users().get(userId.toString())
                .roles()
                .realmLevel()
                .listAll()
                .stream()
                .map(RoleRepresentation::getName)
                .filter(name -> !name.startsWith("default-roles")&&!name.startsWith("offline_access")
                &&!name.startsWith("uma_authorization"))
                .collect(Collectors.toList());
    }

    @Override
    public void assignRolesToUser(UUID userId,List<String> roleNames){
        if(roleNames==null || roleNames.isEmpty()) return;


        List<RoleRepresentation> rolesToAdd = roleNames.stream()
                .map(name-> keycloak.realm(realm).roles().get(name).toRepresentation())
                .collect(Collectors.toList());

        keycloak.realm(realm).users().get(userId.toString())
                .roles()
                .realmLevel()
                .add(rolesToAdd);
    }



}
