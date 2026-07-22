package com.user.management.scheduler;

import com.user.management.entity.Delegation;
import com.user.management.repository.DelegationRepository;
import com.user.management.services.KeycloakService;
import com.user.management.services.OutboxService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DelegationProcessor {

    private final DelegationRepository delegationRepository;
    private final KeycloakService keycloakService;
    private final OutboxService outboxService;

    @Scheduled(fixedDelayString = "${app.delegation.check-ms}")
    @Transactional
    public void processDelegation(){
        log.debug("Starting delegation status check...");
        LocalDateTime now = LocalDateTime.now();

        // Flip SCHEDULED -> ACTIVE
        List<Delegation> toActivate = delegationRepository.findByStatusAndStartTimeBefore("SCHEDULED",now);
        for(Delegation delegation : toActivate){

            log.info("Job: Activating delegation {} for user {} ",delegation.getId(),delegation.getDelegateeId());
            delegation.setStatus("ACTIVE");
            delegationRepository.save(delegation);
            String outboxStatus = "PENDING";

            try{

                keycloakService.assignRolesToUser(delegation.getDelegateeId(),delegation.getDelegatedRoles());

               outboxStatus="PROCESSED";
            }catch (Exception e){
                log.warn("Keycloak down during delegation activation {}. Outbox will retry.", delegation.getId());

            }

            outboxService.saveEvent(delegation.getId(),"DELEGATION",
                    "DELEGATION_ACTIVATED",delegation,outboxStatus);

        }

        //Flip ACTIVE -> EXPIRED
        List<Delegation> toExpire = delegationRepository.findByStatusAndEndTimeBefore("ACTIVE",now);
        for(Delegation delegation: toExpire){

            log.info("Job: Expiring delegation {} for user {}", delegation.getId(), delegation.getDelegateeId());
            delegation.setStatus("EXPIRED");
            delegationRepository.save(delegation);

            String outboxStatus = "PENDING";
            try {

                keycloakService.removeRolesFromUser(delegation.getDelegateeId(),delegation.getDelegatedRoles());
                outboxStatus = "PROCESSED";

            }catch (Exception e){
                log.error("Job failed to expire delegation {}: {}", delegation.getId(), e.getMessage());
            }
            outboxService.saveEvent(delegation.getId(),"DELEGATION",
                    "DELEGATION_EXPIRED",delegation,outboxStatus);
        }
    }

}
