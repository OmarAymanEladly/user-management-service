package com.user.management.services.impl;

import com.user.management.dto.request.DelegationRequestDTO;
import com.user.management.dto.response.DelegationResponseDTO;
import com.user.management.entity.Delegation;
import com.user.management.repository.DelegationRepository;
import com.user.management.services.DelegationService;
import com.user.management.services.KeycloakService;
import com.user.management.services.OutboxService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DelegationServiceImpl implements DelegationService {

    private final DelegationRepository delegationRepository;
    private final KeycloakService keycloakService;
    private final OutboxService outboxService;

    @Override
    @Transactional
    public DelegationResponseDTO createDelegation(DelegationRequestDTO request){

        LocalDateTime now = LocalDateTime.now();

        if(request.getStartTime().isBefore(now.minusSeconds(5))){
            throw new IllegalArgumentException("Start time cannot be in the past");
        }

        if(request.getEndTime().isBefore(request.getStartTime())
                ||request.getEndTime().isEqual(request.getStartTime())){

            throw new IllegalArgumentException("End time must be strictly after start time");

        }

        List<String> rolesToDelegate = keycloakService.getUserRoles(request.getDelegatorId());

        if(rolesToDelegate.isEmpty()){
            throw new IllegalArgumentException("Delegator has no roles to delegate");
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

        String outboxStatus = "PENDING";
        if("ACTIVE".equals(status)){
            try{
                keycloakService.assignRolesToUser(request.getDelegateeId(),rolesToDelegate);
                outboxStatus = "PROCESSED";

            }catch (Exception e){
                log.error("Failed to assign roles to User B during immediate delegation: {}",e.getMessage());
            }

        }

        outboxService.saveEvent(saved.getId(),"DELEGATION","DELEGATION_CREATED",request,outboxStatus);

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
