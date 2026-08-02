package com.user.management.repository;

import com.user.management.entity.KeycloakRoleComposite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface KeycloakRoleCompositeRepository extends JpaRepository<KeycloakRoleComposite, UUID> {

    List<KeycloakRoleComposite> findBySyncStatus(String status);
}
