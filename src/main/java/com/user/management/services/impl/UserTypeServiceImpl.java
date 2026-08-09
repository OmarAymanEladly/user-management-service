package com.user.management.services.impl;

import com.user.management.audit.annotation.AuditResource;
import com.user.management.audit.annotation.PublishAuditEvent;
import com.user.management.audit.enumeration.ActionType;
import com.user.management.audit.enumeration.ResourceType;
import com.user.management.dto.request.UserTypeRequestDTO;
import com.user.management.dto.response.UserTypeResponseDTO;
import com.user.management.repository.UserTypeRepository;
import com.user.management.services.KeycloakService;
import com.user.management.services.OutboxService;
import com.user.management.services.UserTypeService;
import com.user.management.model.entity.UserType;
import com.user.management.mapper.UserTypeMapper;
import jakarta.transaction.Transactional;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.*;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
@AuditResource(type = ResourceType.USER_TYPE, idSpEL = "#id.toString()")
public class UserTypeServiceImpl implements UserTypeService {

    private final UserTypeRepository repository;
    private final UserTypeMapper mapper;
    private final KeycloakService keycloakService;
    private final OutboxService outboxService;

    @Override
    @Transactional
    @PublishAuditEvent(
            actionType     = ActionType.USER_TYPE_CREATE,
            resourceIdSpEL = "#result.id.toString()",
            metadataSpEL = "{'typeName': #request.type, 'role': #request.roleName}"
    )
    public UserTypeResponseDTO createType(UserTypeRequestDTO request){

        String normalizedType = request.getType().trim().toUpperCase();

        if(repository.findByType(normalizedType).isPresent()){
            throw new RuntimeException("User Type: "+request.getType()+" already exist");
        }
        validateRole(request.getRoleName());

        UserType entity = mapper.toEntity(request);

        entity.setType(normalizedType);
        UserType saved = repository.save(entity);

        String status = "PENDING";
        try {
            keycloakService.syncUserTypeAttributes(saved);
            status = "PROCESSED";
        } catch (jakarta.ws.rs.BadRequestException e) {
            String errorBody = e.getResponse().readEntity(String.class);
            log.error("Keycloak rejected the request. Reason: {}", errorBody);
            throw new RuntimeException("Keycloak Error: " + errorBody);
        } catch (Exception e) {

            log.warn("Keycloak communication failed. Outbox will sync later.");
        }

        outboxService.saveEvent(saved.getId(),"USER_TYPE","USER_TYPE_CREATED",request,status);

        return mapper.toResponse(saved);
    }


    @Override
    public List<UserTypeResponseDTO> getAllTypes(){
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAvailableRoles() {

        return keycloakService.getRealmRoles();
    }

    @Override
    public UserTypeResponseDTO getTypeById(UUID id){
        UserType entity = repository.findById(id).
                orElseThrow(()->new RuntimeException("User Type not found with ID: "+ id));

        return mapper.toResponse(entity);
    }

    @Override
    @PublishAuditEvent(
            actionType = ActionType.USER_TYPE_UPDATE,
            metadataSpEL = "{'typeName': #request.type, 'status': #request.status}"
    )
    public UserTypeResponseDTO updateType(UUID id, UserTypeRequestDTO request){
        UserType existingEntity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User Type not found with id: " + id));
        validateRole(request.getRoleName());
        UserType updatedData = mapper.toEntity(request);

        existingEntity.setDescription(updatedData.getDescription());
        existingEntity.setType(updatedData.getType());
        existingEntity.setRoleName(updatedData.getRoleName());
        existingEntity.setFields(updatedData.getFields());
        existingEntity.setStatus(updatedData.getStatus());
        existingEntity.setAllowedToSelfSignup(updatedData.getAllowedToSelfSignup());
        existingEntity.setRequiresAdminApproval(updatedData.getRequiresAdminApproval());

        UserType saved = repository.save(existingEntity);

        String status = "PENDING";
        try {
            keycloakService.syncUserTypeAttributes(saved);
            status = "PROCESSED";
        } catch (Exception e) {
            log.warn("Keycloak down. Outbox will sync UserType update later: {}", saved.getType());
        }

        outboxService.saveEvent(saved.getId(), "USER_TYPE", "USER_TYPE_UPDATED", request, status);


        return  mapper.toResponse(saved);
    }

    @Override
    @PublishAuditEvent(actionType = ActionType.USER_TYPE_DEACTIVATE)
    public UserTypeResponseDTO deactivateType(UUID id) {
        UserType existingEntity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User Type not found with id: " + id));

        existingEntity.setStatus("INACTIVE");
        return mapper.toResponse(repository.save(existingEntity));
    }

    @Override
    @PublishAuditEvent(actionType = ActionType.USER_TYPE_DELETE)
    public void deleteType(UUID id){

        UserType userType = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User Type to delete doesn't exist"));

        repository.deleteById(id);
        String typeName = userType.getType();

        String status = "PENDING";
        try {

            keycloakService.cleanupUserTypeAttributes(typeName);
            status = "PROCESSED";
            log.info("Successfully cleaned up UserType '{}' from Keycloak immediately.", typeName);
        } catch (Exception e) {
            log.warn("Keycloak cleanup failed for '{}' (Keycloak might be down). Outbox will retry later.", typeName);
        }


        Map<String, String> payload = Map.of("typeName", typeName);
        outboxService.saveEvent(id, "USER_TYPE", "USER_TYPE_DELETED", payload, status);
    }

    private void validateRole(String roleName) {
        if (!keycloakService.realmRoleExists(roleName)) {
            throw new IllegalArgumentException("Keycloak role not found: " + roleName);
        }
    }

}
