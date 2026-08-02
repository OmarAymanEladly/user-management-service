package com.user.management.model.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "keycloak_roles")
@Data
public class KeycloakRole {

    @Id
    private UUID id;
    private String name;
    private String syncStatus;
}
