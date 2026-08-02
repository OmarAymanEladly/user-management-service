package com.user.management.services.impl;

import com.user.management.dto.request.AdminUserRequestDTO;
import com.user.management.dto.request.UserApprovalStatusRequestDTO;
import com.user.management.dto.response.AdminUserResponseDTO;
import com.user.management.model.entity.FieldDefinition;
import com.user.management.model.entity.ManagedUser;
import com.user.management.model.entity.UserType;
import com.user.management.repository.ManagedUserRepository;
import com.user.management.repository.UserTypeRepository;
import com.user.management.services.AdminUserService;
import com.user.management.services.KeycloakService;
import lombok.RequiredArgsConstructor;
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

    @Override
    public AdminUserResponseDTO createUser(AdminUserRequestDTO request) {

        if (managedUserRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("User already exists in local database");
        }
        UserType userType = getUserType(request.getUserTypeId());

        if (userType.getStatus() == null || !"ACTIVE".equalsIgnoreCase(userType.getStatus())) {
            throw new RuntimeException("Cannot create user: User Type '" + userType.getType() + "' is currently " + userType.getStatus());
        }

        validateAttributes(userType, request.getAttributes());

        UUID localId = UUID.randomUUID();
        ManagedUser user = ManagedUser.builder().id(localId)
        .isNewUser(true)
        .build();
        applyRequest(user, request, userType);

        try{
            String confirmedId = keycloakService.createKeycloakUser(localId, request, userType);
            user.setId(UUID.fromString(confirmedId));
        } catch (Exception e) {
            System.err.println("Keycloak timeout/error: " + e.getMessage());
        }

        return toResponse(managedUserRepository.save(user));

    }

    @Override
    public List<AdminUserResponseDTO> getAllUsers(String signupApprovalStatus) {
        List<ManagedUser> users = signupApprovalStatus == null || signupApprovalStatus.isBlank()
                ? managedUserRepository.findAll()
                : managedUserRepository.findBySignupApprovalStatusIgnoreCase(signupApprovalStatus);

        return users.stream()
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
    public AdminUserResponseDTO approveSignup(UUID id) {
        return updateApprovalStatus(id, new UserApprovalStatusRequestDTO("APPROVED", null));
    }

    @Override
    public AdminUserResponseDTO rejectSignup(UUID id) {
        return updateApprovalStatus(id, new UserApprovalStatusRequestDTO("REJECTED", null));
    }

    @Override
    public AdminUserResponseDTO updateApprovalStatus(UUID id, UserApprovalStatusRequestDTO request) {
        ManagedUser user = getUser(id);
        String requestedStatus = normalizeApprovalStatus(request.getStatus());

        if ("ACTIVE".equals(requestedStatus)) {
            approvePendingUser(user);
        } else if ("REJECTED".equals(requestedStatus)) {
            rejectPendingUser(user, request.getRejectReason());
        } else {
            throw new IllegalArgumentException("Unsupported status: " + request.getStatus());
        }

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
        user.setSignupApprovalStatus("ACTIVE");
        user.setRejectionReason(null);
        user.setAttributes(request.getAttributes());
    }

    private void approvePendingUser(ManagedUser user) {
        if (!"PENDING_APPROVAL".equalsIgnoreCase(user.getSignupApprovalStatus())) {
            throw new IllegalArgumentException("Only users with PENDING_APPROVAL status can be approved");
        }

        AdminUserRequestDTO request = toRequest(user);
        String confirmedId = keycloakService.createKeycloakUser(user.getId(), request, user.getUserType());
        if (confirmedId != null && !confirmedId.isBlank() && !user.getId().toString().equals(confirmedId)) {
            user.setId(UUID.fromString(confirmedId));
        }

        user.setEnabled(true);
        user.setSignupApprovalStatus("ACTIVE");
        user.setRejectionReason(null);
    }

    private void rejectPendingUser(ManagedUser user, String rejectReason) {
        if (!"PENDING_APPROVAL".equalsIgnoreCase(user.getSignupApprovalStatus())) {
            throw new IllegalArgumentException("Only users with PENDING_APPROVAL status can be rejected");
        }

        user.setEnabled(false);
        user.setSignupApprovalStatus("REJECTED");
        user.setRejectionReason(rejectReason);
        System.out.println("Signup rejected for " + user.getEmail() + ". Reason: " + rejectReason);
    }

    private String normalizeApprovalStatus(String status) {
        String normalized = status == null ? "" : status.trim().toUpperCase();
        if ("APPROVED".equals(normalized)) {
            return "ACTIVE";
        }
        return normalized;
    }

    private AdminUserRequestDTO toRequest(ManagedUser user) {
        return new AdminUserRequestDTO(
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getUserType().getId(),
                true,
                user.getAttributes()
        );
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
                user.getUserType().getRoleName(),
                user.getEnabled(),
                user.getSignupApprovalStatus(),
                user.getRejectionReason(),
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
