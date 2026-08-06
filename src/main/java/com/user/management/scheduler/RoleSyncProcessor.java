package com.user.management.scheduler;


import com.user.management.repository.KeycloakPermissionRepository;
import com.user.management.repository.KeycloakRoleCompositeRepository;
import com.user.management.repository.KeycloakRoleRepository;
import com.user.management.services.KeycloakService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoleSyncProcessor {

    private final KeycloakService keycloakService;
    private final KeycloakRoleRepository roleRepo;
    private final KeycloakPermissionRepository permRepo;
    private final KeycloakRoleCompositeRepository compositeRepo;

    @Scheduled(fixedDelayString = "${app.fixed-delay-ms}")
    @Transactional
    public void processRoleSync(){
        log.info("Starting background role processing...");

        roleRepo.findBySyncStatus("PENDING").forEach(role->{
                try {
                    keycloakService.createRealmRole(role.getName());
                    role.setSyncStatus("SYNCED");
                    roleRepo.saveAndFlush(role);
                }catch (Exception e){
                    log.error("Failed role sync: {}",role.getName());
                }

        });


        permRepo.findBySyncStatus("PENDING").forEach(perm->{
            try {
                keycloakService.createClientRole(perm.getClientId(),perm.getName());
                perm.setSyncStatus("SYNCED");
                permRepo.saveAndFlush(perm);
            }catch (Exception e){
                log.error("Failed permission sync: {}",perm.getName());
            }
        });

        compositeRepo.findBySyncStatus("PENDING").stream()
                .filter(composite-> "SYNCED".equals(composite.getRole().getSyncStatus())
                && "SYNCED".equals(composite.getPermission().getSyncStatus()))
                .forEach(link->{
                    try {
                        keycloakService.addClientRoleToRealmRole(
                                link.getRole().getName(),
                                link.getPermission().getClientId(),
                                link.getPermission().getName()
                        );

                        link.setSyncStatus("SYNCED");
                        compositeRepo.saveAndFlush(link);
                    }catch (Exception e){
                        log.error("Failed to link {} to {}",link.getPermission().getName(),link.getRole().getName());
                    }
                });



    }
}
