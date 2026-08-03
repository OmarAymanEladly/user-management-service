package com.user.management.repository;

import com.user.management.model.entity.KeycloakRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface KeycloakRoleRepository extends JpaRepository<KeycloakRole, UUID> {

    List<KeycloakRole> findBySyncStatus(String status);
}
