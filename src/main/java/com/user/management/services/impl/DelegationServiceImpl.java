package com.user.management.services.impl;

import com.user.management.audit.annotation.AuditResource;
import com.user.management.audit.annotation.PublishAuditEvent;
import com.user.management.audit.enumeration.ActionType;
import com.user.management.audit.enumeration.ResourceType;
import com.user.management.dto.request.DelegationRequestDTO;
import com.user.management.dto.response.DelegationResponseDTO;
import com.user.management.model.entity.Delegation;
import com.user.management.repository.DelegationRepository;
import com.user.management.services.DelegationService;
import com.user.management.services.KeycloakService;
import com.user.management.services.OutboxService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@AuditResource(type = ResourceType.DELEGATION, idSpEL = "#id.toString()")
public class DelegationServiceImpl implements DelegationService {

    private final DelegationRepository delegationRepository;
    private final KeycloakService keycloakService;
    private final OutboxService outboxService;

    @Override
    @Transactional
    @PublishAuditEvent(
            actionType     = ActionType.DELEGATION_CREATE,
            resourceIdSpEL = "#result.id.toString()",
            metadataSpEL = "{'delegator': #request.delegatorId, 'delegatee': #request.delegateeId}"
    )
    public DelegationResponseDTO createDelegation(DelegationRequestDTO request){

        LocalDateTime now = LocalDateTime.now();

        if(request.getStartTime().isBefore(now)){
            throw new IllegalArgumentException("Start time cannot be in the past");
        }

        if(request.getEndTime().isBefore(request.getStartTime())
                ||request.getEndTime().isEqual(request.getStartTime())){

            throw new IllegalArgumentException("End time must be strictly after start time");

        }

        List<String> rolesToDelegate = new ArrayList<>();
        boolean rolesFetched = false;

        try {
            rolesToDelegate = keycloakService.getUserRoles(request.getDelegatorId());
            rolesFetched = true;
        } catch (Exception e) {
            log.warn("Keycloak down. Roles for delegator {} will be fetched by Outbox later.", request.getDelegatorId());
        }

        String status = request.getStartTime().isAfter(now.plusSeconds(30))? "SCHEDULED" : "ACTIVE";

        Delegation delegation = Delegation.builder()
                .delegatorId(request.getDelegatorId())
                .delegateeId(request.getDelegateeId())
                .delegatedRoles(rolesToDelegate)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(status)
                .build();

        Delegation saved = delegationRepository.save(delegation);

        String outboxStatus = "PROCESSED";
        if (!rolesFetched) {
            outboxStatus = "FAILED";
            throw new RuntimeException("Keycloak is down try again later.");
        } else if ("ACTIVE".equals(status)) {

            try {
                keycloakService.assignRolesToUser(saved.getDelegateeId(), rolesToDelegate);
            } catch (Exception e) {
                outboxStatus = "PENDING";
            }
        }


        outboxService.saveEvent(saved.getId(),"DELEGATION","DELEGATION_CREATED",saved,outboxStatus);

        return DelegationResponseDTO.builder()
                .id(saved.getId())
                .delegatorId(saved.getDelegatorId())
                .delegateeId(saved.getDelegateeId())
                .delegatedRoles(saved.getDelegatedRoles())
                .startTime(saved.getStartTime())
                .endTime(saved.getEndTime())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    @PublishAuditEvent(actionType = ActionType.DELEGATION_REVOKE)
    public DelegationResponseDTO revokeDelegation(UUID id){

        Delegation delegation = delegationRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("Delegation not found wit id: "+id));

        if("REVOKED".equals(delegation.getStatus()) || "EXPIRED".equals(delegation.getStatus()))
        {
           throw new IllegalArgumentException("Delegation is already "+ delegation.getStatus());
        }

        String previousStatus = delegation.getStatus();
        delegation.setStatus("REVOKED");
        Delegation saved = delegationRepository.save(delegation);

        String outboxStatus = "PROCESSED";

        if ("ACTIVE".equals(previousStatus)) {
            try {
                keycloakService.removeRolesFromUser(saved.getDelegateeId(), saved.getDelegatedRoles());
            } catch (Exception e) {
                log.error("Failed to remove roles from Keycloak during revocation. Outbox will retry.");
                outboxStatus = "PENDING";
            }
        }
        outboxService.saveEvent(saved.getId(), "DELEGATION", "DELEGATION_REVOKED", saved, outboxStatus);

        return DelegationResponseDTO.builder()
                .id(saved.getId())
                .delegatorId(saved.getDelegatorId())
                .delegateeId(saved.getDelegateeId())
                .delegatedRoles(saved.getDelegatedRoles())
                .startTime(saved.getStartTime())
                .endTime(saved.getEndTime())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .build();


    }



}
