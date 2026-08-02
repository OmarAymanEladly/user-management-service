package com.user.management.services.impl;

import com.user.management.dto.request.AdminUserRequestDTO;
import com.user.management.dto.request.SelfSignupRequestDTO;
import com.user.management.dto.response.AdminUserResponseDTO;
import com.user.management.model.entity.FieldDefinition;
import com.user.management.model.entity.ManagedUser;
import com.user.management.model.entity.UserType;
import com.user.management.repository.ManagedUserRepository;
import com.user.management.repository.UserTypeRepository;
import com.user.management.services.KeycloakService;
import com.user.management.services.SelfSignupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SelfSignupServiceImpl implements SelfSignupService {

    private final ManagedUserRepository managedUserRepository;
    private final UserTypeRepository userTypeRepository;
    private final KeycloakService keycloakService;

    @Override
    public AdminUserResponseDTO signup(SelfSignupRequestDTO request) {
        UserType userType = validateSignupRequest(request);
        if (Boolean.TRUE.equals(userType.getRequiresAdminApproval())) {
            throw new IllegalArgumentException("This user type requires admin approval. Use /api/signup/approval-required");
        }
        ManagedUser user = buildUser(request, userType, true, "ACTIVE");

        try {
            String confirmedId = keycloakService.createKeycloakUser(user.getId(), toAdminRequest(request, true), userType);
            user.setId(UUID.fromString(confirmedId));
        } catch (Exception e) {
            System.err.println("Keycloak timeout/error during self signup: " + e.getMessage());
        }

        return toResponse(managedUserRepository.save(user));
    }

    @Override
    public AdminUserResponseDTO signupWithApproval(SelfSignupRequestDTO request) {
        UserType userType = validateSignupRequest(request);
        if (!Boolean.TRUE.equals(userType.getRequiresAdminApproval())) {
            throw new IllegalArgumentException("This user type does not require admin approval. Use /api/signup/open");
        }
        ManagedUser user = buildUser(request, userType, false, "PENDING_APPROVAL");
        return toResponse(managedUserRepository.save(user));
    }

    private UserType validateSignupRequest(SelfSignupRequestDTO request) {
        if (managedUserRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("User already exists in local database");
        }

        UserType userType = userTypeRepository.findById(request.getUserTypeId())
                .orElseThrow(() -> new RuntimeException("User Type not found with id: " + request.getUserTypeId()));

        if (userType.getStatus() == null || !"ACTIVE".equalsIgnoreCase(userType.getStatus())) {
            throw new RuntimeException("Cannot create user: User Type '" + userType.getType() + "' is currently " + userType.getStatus());
        }

        if (!Boolean.TRUE.equals(userType.getAllowedToSelfSignup())) {
            throw new IllegalArgumentException("Self signup is not allowed for User Type '" + userType.getType() + "'");
        }

        validateAttributes(userType, request.getAttributes());
        return userType;
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

    private ManagedUser buildUser(SelfSignupRequestDTO request, UserType userType, boolean enabled, String approvalStatus) {
        return ManagedUser.builder()
                .id(UUID.randomUUID())
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .userType(userType)
                .enabled(enabled)
                .attributes(request.getAttributes())
                .signupApprovalStatus(approvalStatus)
                .isNewUser(true)
                .build();
    }

    private AdminUserRequestDTO toAdminRequest(SelfSignupRequestDTO request, boolean enabled) {
        return new AdminUserRequestDTO(
                request.getUsername(),
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getPhoneNumber(),
                request.getUserTypeId(),
                enabled,
                request.getAttributes()
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
}
