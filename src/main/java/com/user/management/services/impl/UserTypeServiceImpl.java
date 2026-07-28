package com.user.management.services.impl;

import com.user.management.dto.request.UserTypeRequestDTO;
import com.user.management.dto.response.UserTypeResponseDTO;
import com.user.management.repository.UserTypeRepository;
import com.user.management.services.KeycloakService;
import com.user.management.services.OutboxService;
import com.user.management.services.UserTypeService;
import com.user.management.entity.UserType;
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
public class UserTypeServiceImpl implements UserTypeService {

    private final UserTypeRepository repository;
    private final UserTypeMapper mapper;
    private final KeycloakService keycloakService;
    private final OutboxService outboxService;

    @Override
    @Transactional
    public UserTypeResponseDTO createType(UserTypeRequestDTO request){

        if(repository.findByType(request.getType().toLowerCase()).isPresent()){
            throw new RuntimeException("User Type: "+request.getType()+" already exist");
        }
        validateRole(request.getRoleName());

        UserType entity = mapper.toEntity(request);
        UserType saved = repository.save(entity);

        String status = "PENDING";
        try {
            keycloakService.syncUserTypeAttributes(saved);
            status = "PROCESSED";
        } catch (Exception e) {
            log.warn("Keycloak down. Outbox will sync UserType profile later.");
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
    public UserTypeResponseDTO deactivateType(UUID id) {
        UserType existingEntity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User Type not found with id: " + id));

        existingEntity.setStatus("INACTIVE");
        return mapper.toResponse(repository.save(existingEntity));
    }

    @Override
    public void deleteType(UUID id){

        UserType userType = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User Type to delete doesn't exist"));

        repository.deleteById(id);
        String typeName = userType.getType();

        Map<String, String> payload = Map.of("typeName", typeName);
        outboxService.saveEvent(id, "USER_TYPE", "USER_TYPE_DELETED", payload, "PENDING");
    }

    private void validateRole(String roleName) {
        if (!keycloakService.realmRoleExists(roleName)) {
            throw new RuntimeException("Keycloak role not found: " + roleName);
        }
    }

}
