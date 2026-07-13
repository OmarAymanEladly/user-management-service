package com.user.management.services.impl;

import com.user.management.dto.request.AdminUserRequestDTO;
import com.user.management.dto.response.AdminUserResponseDTO;
import com.user.management.entity.FieldDefinition;
import com.user.management.entity.ManagedUser;
import com.user.management.entity.UserType;
import com.user.management.repository.ManagedUserRepository;
import com.user.management.repository.UserTypeRepository;
import com.user.management.services.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import jakarta.ws.rs.core.Response;
import java.util.stream.Collectors;


import java.util.List;
import java.util.Map;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final ManagedUserRepository managedUserRepository;
    private final UserTypeRepository userTypeRepository;
    private final Keycloak keycloak;

    @Override
    public AdminUserResponseDTO createUser(AdminUserRequestDTO request) {

        if (managedUserRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("User already exists in local database");
        }
        UserType userType = getUserType(request.getUserTypeId());
        validateAttributes(userType, request.getAttributes());

        String keycloakId = createKeycloakUser(request);

        try{
            ManagedUser user = managedUserRepository.findByUsername(request.getUsername())
                    .orElseGet(ManagedUser::new);

            user.setId(UUID.fromString(keycloakId));

            applyRequest(user, request, userType);
            return toResponse(managedUserRepository.save(user));
        } catch (Exception e) {
            deleteKeycloakUser(UUID.fromString(keycloakId));
            throw new RuntimeException("Local database failed.Rolling back keycloak user.");

        }


    }

    @Override
    public List<AdminUserResponseDTO> getAllUsers() {
        return managedUserRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /*@Override
    public AdminUserResponseDTO getUserById(UUID id) {
        return toResponse(getUser(id));
    }*/

    @Override
    public AdminUserResponseDTO updateUser(UUID id, AdminUserRequestDTO request) {
        ManagedUser user = getUser(id);
        UserType userType = getUserType(request.getUserTypeId());
        validateAttributes(userType, request.getAttributes());

        updateKeycloakUser(id, request);
        applyRequest(user, request, userType);
        return toResponse(managedUserRepository.save(user));
    }

    @Override
    public AdminUserResponseDTO activateUser(UUID id) {
        updateKeycloakStatus(id, true);
        ManagedUser user = getUser(id);
        user.setEnabled(true);
        return toResponse(managedUserRepository.save(user));
    }

    @Override
    public AdminUserResponseDTO deactivateUser(UUID id) {
        updateKeycloakStatus(id, false);
        ManagedUser user = getUser(id);
        user.setEnabled(false);
        return toResponse(managedUserRepository.save(user));
    }

    @Override
    public void deleteUser(UUID id) {
        if (!managedUserRepository.existsById(id)) {
            throw new RuntimeException("User to delete doesn't exist");
        }
        deleteKeycloakUser(id);
        managedUserRepository.deleteById(id);
    }

    private ManagedUser getUser(UUID id) {
        return managedUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    private UserType getUserType(UUID id) {
        return userTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User Type not found with id: " + id));
    }

    private void validateAttributes(UserType userType, Map<String, Object> attributes) {
        List<FieldDefinition> fields = userType.getFields();
        if (fields == null || fields.isEmpty()) {
            return;
        }

        for (FieldDefinition field : fields) {
            if (field.isRequired() && (attributes == null || !attributes.containsKey(field.getFieldName())
                    || attributes.get(field.getFieldName()) == null)) {
                throw new RuntimeException("Required attribute is missing: " + field.getFieldName());
            }
        }
    }

    private void applyRequest(ManagedUser user, AdminUserRequestDTO request, UserType userType) {
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setUserType(userType);
        user.setEnabled(request.getEnabled() == null || request.getEnabled());
        user.setAttributes(request.getAttributes());
    }

    private AdminUserResponseDTO toResponse(ManagedUser user) {
        return new AdminUserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getUserType().getId(),
                user.getEnabled(),
                user.getAttributes()
        );
    }

    private String createKeycloakUser(AdminUserRequestDTO request){
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEnabled(true);
        user.setEmailVerified(false);

        Response response = keycloak.realm("user-management").users().create(user);

        if(response.getStatus()!=201){
            throw new RuntimeException("Keycloak user creation failed with status: " + response.getStatus());
        }

        String path = response.getLocation().getPath();
        String keycloakId = path.substring(path.lastIndexOf('/')+1);

        try {
            keycloak.realm("user-management")
                    .users()
                    .get(keycloakId)
                    .executeActionsEmail(List.of("UPDATE_PASSWORD","VERIFY_EMAIL"));
        } catch (Exception e) {

            System.err.println("Failed to send welcome email: " + e.getMessage());
        }

        return keycloakId;
    }

    private void updateKeycloakStatus(UUID id,boolean enabled){
        var userResource = keycloak.realm("user-management").users().get(id.toString());
        UserRepresentation user = userResource.toRepresentation();
        user.setEnabled(enabled);
        userResource.update(user);
    }

    private void deleteKeycloakUser(UUID id){
        keycloak.realm("user-management").users().get(id.toString()).remove();

    }

    private void updateKeycloakUser(UUID id,AdminUserRequestDTO request){
        UserRepresentation user = keycloak.realm("user-management").
                users().get(id.toString()).toRepresentation();

        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        keycloak.realm("user-management").
                users().get(id.toString()).update(user);
    }



    @Override
    public AdminUserResponseDTO getUserById(UUID id){
        ManagedUser user = managedUserRepository.findById(id)
                .orElseThrow(()->new RuntimeException("user not found: " + id));

        try{
            UserRepresentation kcUser = keycloak.realm("user-management")
                    .users().get(id.toString()).toRepresentation();
            user.setEnabled(kcUser.isEnabled());
        }catch (Exception e){

        }

        return toResponse(user);
    }
}
