package com.user.management.repository;

import com.user.management.model.entity.KeycloakPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface KeycloakPermissionRepository extends JpaRepository<KeycloakPermission, UUID> {

    List<KeycloakPermission> findBySyncStatus(String status);
}
