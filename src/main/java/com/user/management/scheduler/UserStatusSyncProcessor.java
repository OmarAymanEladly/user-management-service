package com.user.management.scheduler;


import com.user.management.model.entity.ManagedUser;
import com.user.management.repository.ManagedUserRepository;
import com.user.management.services.KeycloakService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserStatusSyncProcessor {

    private final ManagedUserRepository userRepository;
    private final KeycloakService keycloakService;

    @Scheduled(fixedDelayString = "${app.fixed-delay-ms}")
    @Transactional
    public void syncBlockedUsers(){
        log.debug("Checking for blocked users in keycloak...");

        List<ManagedUser> activeUsers = userRepository.findByEnabledTrue();

        for(ManagedUser user : activeUsers){


            if(keycloakService.isUserBlocked(user.getId())){
                log.warn("DETECTED BLOCK: User {} is locked in keycloak. Disabling locally.",user.getUsername());

                user.setEnabled(false);
                userRepository.save(user);
            }
        }

        List<ManagedUser> disabledUsers = userRepository.findByEnabledFalse();
        for(ManagedUser user : disabledUsers){

            boolean currentlyBlocked = keycloakService.isUserBlocked(user.getId());
            boolean enabledInKeycloak = keycloakService.isUserEnabledInKeycloak(user.getId());


            if(!currentlyBlocked && enabledInKeycloak){
                log.info("LOCKOUT EXPIRED: User {} is no longer blocked in Keycloak. Re-enabling locally.", user.getUsername());
                user.setEnabled(true);
                userRepository.save(user);

            }else if (!enabledInKeycloak) {

                log.debug("User {} is manually disabled by Admin. Skipping auto-activation.", user.getUsername());
            }
        }
    }
}
