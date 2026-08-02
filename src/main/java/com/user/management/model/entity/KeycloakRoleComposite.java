package com.user.management.model.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "keycloak_role_composites")
@Data
public class KeycloakRoleComposite {

    @EmbeddedId
    private RoleCompositeId id;

    private String syncStatus;

    @ManyToOne
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private KeycloakRole role;

    @ManyToOne
    @MapsId("permissionId")
    @JoinColumn(name = "permission_id")
    private KeycloakPermission permission;
}

@Embeddable
@Data
class RoleCompositeId implements Serializable {
    private UUID roleId;
    private UUID permissionId;
}