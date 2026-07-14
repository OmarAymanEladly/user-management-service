package com.user.management.services.impl;

import com.user.management.dto.request.AdminUserRequestDTO;
import com.user.management.dto.response.AdminUserResponseDTO;
import com.user.management.entity.FieldDefinition;
import com.user.management.entity.ManagedUser;
import com.user.management.entity.UserType;
import com.user.management.repository.ManagedUserRepository;
import com.user.management.repository.UserTypeRepository;
import com.user.management.services.AdminUserService;
import com.user.management.services.KeycloakService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;



import java.util.List;
import java.util.Map;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final ManagedUserRepository managedUserRepository;
    private final UserTypeRepository userTypeRepository;
    private final KeycloakService keycloakService;
    private final Keycloak keycloak;

    @Override
    public AdminUserResponseDTO createUser(AdminUserRequestDTO request) {

        if (managedUserRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("User already exists in local database");
        }
        UserType userType = getUserType(request.getUserTypeId());
        validateAttributes(userType, request.getAttributes());

        UUID localId = UUID.randomUUID();
        ManagedUser user = ManagedUser.builder().id(localId)
        .isNewUser(true)
        .syncStatus("PENDING_SYNC").build();
        applyRequest(user, request, userType);



        try{
            String confirmedId = keycloakService.createKeycloakUser(localId,request);
            user.setId(UUID.fromString(confirmedId));
            user.setSyncStatus("SYNCED");
        } catch (Exception e) {
            System.err.println("Keycloak unavailable. Sync will be handled by background worker.");
        }

        return toResponse(managedUserRepository.save(user));

    }

    @Override
    public List<AdminUserResponseDTO> getAllUsers() {
        return managedUserRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AdminUserResponseDTO getUserById(UUID id) {
        return toResponse(getUser(id));
    }

    @Override
    public AdminUserResponseDTO updateUser(UUID id, AdminUserRequestDTO request) {
        ManagedUser user = getUser(id);
        UserType userType = getUserType(request.getUserTypeId());
        validateAttributes(userType, request.getAttributes());

        keycloakService.updateKeycloakUser(id, request);
        applyRequest(user, request, userType);
        return toResponse(managedUserRepository.save(user));
    }

    @Override
    public AdminUserResponseDTO activateUser(UUID id) {
        keycloakService.updateKeycloakStatus(id, true);
        ManagedUser user = getUser(id);
        user.setEnabled(true);
        return toResponse(managedUserRepository.save(user));
    }

    @Override
    public AdminUserResponseDTO deactivateUser(UUID id) {
        keycloakService.updateKeycloakStatus(id, false);
        ManagedUser user = getUser(id);
        user.setEnabled(false);
        return toResponse(managedUserRepository.save(user));
    }



    @Override
    public void deleteUser(UUID id) {
        boolean existsInDb = managedUserRepository.existsById(id);

        try {
            keycloakService.deleteKeycloakUser(id);
        } catch (Exception e) {
            System.err.println("Note: User was already missing from Keycloak or Keycloak is down.");
        }
        if (existsInDb) {
            managedUserRepository.deleteById(id);
        } else {
            System.out.println("User was not in local DB, but Keycloak cleanup was attempted.");
        }
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



    /*@Override
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
    }*/
}
