package com.user.management.model.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "keycloak_permissions")
@Data
public class KeycloakPermission {

    @Id
    private UUID id;
    private String name;
    @Column(name = "client_id", nullable = false)
    private String clientId;
    private String syncStatus;
}

