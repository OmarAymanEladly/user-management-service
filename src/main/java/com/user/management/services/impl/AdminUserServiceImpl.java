package com.user.management.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.management.dto.request.AdminUserRequestDTO;
import com.user.management.dto.response.AdminUserResponseDTO;
import com.user.management.entity.FieldDefinition;
import com.user.management.entity.ManagedUser;
import com.user.management.entity.OutboxEvent;
import com.user.management.entity.UserType;
import com.user.management.repository.ManagedUserRepository;
import com.user.management.repository.OutboxEventRepository;
import com.user.management.repository.UserTypeRepository;
import com.user.management.services.AdminUserService;
import com.user.management.services.KeycloakService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    private final ManagedUserRepository managedUserRepository;
    private final UserTypeRepository userTypeRepository;
    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final KeycloakService keycloakService;

    @Override
    @Transactional
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
        UUID finalUserId = localId;
        String outboxStatus = "PENDING";
        try {

            String confirmedIdStr = keycloakService.createKeycloakUser(localId, request, userType);
            finalUserId = UUID.fromString(confirmedIdStr);
            outboxStatus = "PROCESSED";
        } catch (Exception e) {
            log.warn("Keycloak down. Using temporary ID for {}. Worker will sync later.", request.getUsername());
        }
        ManagedUser user = ManagedUser.builder().id(finalUserId)
        .isNewUser(true)
        .build();
        applyRequest(user, request, userType);

        createOutboxEvent(finalUserId, "USER_CREATED", request, outboxStatus);

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
    @Transactional
    public AdminUserResponseDTO updateUser(UUID id, AdminUserRequestDTO request) {
        ManagedUser user = getUser(id);
        UserType userType = getUserType(request.getUserTypeId());
        validateAttributes(userType, request.getAttributes());

        String status = "PENDING";
        try {
            keycloakService.updateKeycloakUser(id, request);
            status = "PROCESSED";
        } catch (Exception e) {
            log.warn("Keycloak down. Fallback to Outbox for update: {}", id);
        }

        applyRequest(user, request, userType);
        createOutboxEvent(id, "USER_UPDATED", request,status);
        return toResponse(managedUserRepository.save(user));
    }

    @Override
    @Transactional
    public AdminUserResponseDTO activateUser(UUID id) {
        ManagedUser user = getUser(id);
        String status = "PENDING";
        try {

            keycloakService.updateKeycloakStatus(id, true);
            status = "PROCESSED";
        } catch (Exception e) {
            log.warn("Keycloak down. Fallback to Outbox for activation: {}", id);
        }
        user.setEnabled(true);
        createOutboxEvent(id, "USER_ACTIVATED", null, status);
        return toResponse(managedUserRepository.save(user));
    }

    @Override
    @Transactional
    public AdminUserResponseDTO deactivateUser(UUID id) {
        ManagedUser user = getUser(id);
        String status = "PENDING";
        try {
            keycloakService.updateKeycloakStatus(id, false);
            status = "PROCESSED";
        } catch (Exception e) {
            log.warn("Keycloak down. Fallback to Outbox for deactivation: {}", id);
        }
        user.setEnabled(false);
        createOutboxEvent(id, "USER_DEACTIVATED", null,status);
        return toResponse(managedUserRepository.save(user));
    }



    @Override
    public void deleteUser(UUID id) {
        ManagedUser user = managedUserRepository.findById(id).orElse(null);

        String status = "PENDING";

        try {
            keycloakService.deleteKeycloakUser(id);
            status = "PROCESSED";
        } catch (Exception e) {
            log.warn("Keycloak down. Fallback to Outbox for deletion: {}", id);
        }


        if (user!=null) {
            AdminUserRequestDTO deletePayload = new AdminUserRequestDTO();
            deletePayload.setUsername(user.getUsername());
            createOutboxEvent(id, "USER_DELETED", deletePayload,status);
            managedUserRepository.deleteById(id);
        } else {
            createOutboxEvent(id, "USER_DELETED", null,"PENDING");
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
                user.getUserType().getRoleName(),
                user.getEnabled(),
                user.getAttributes()
        );
    }


    private void createOutboxEvent(UUID aggregateId, String eventType, Object payloadObj,String outboxStatus) {
        try {
            String payload = payloadObj != null ? objectMapper.writeValueAsString(payloadObj) : "{}";
            OutboxEvent event = OutboxEvent.builder()
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(payload)
                    .createdAt(LocalDateTime.now())
                    .status(outboxStatus)
                    .retryCount(0)
                    .build();
            outboxRepository.save(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize outbox payload", e);
        }
    }


}
