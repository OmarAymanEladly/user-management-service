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
    private String syncStatus;
}

