CREATE TABLE keycloak_roles (
                                id UUID PRIMARY KEY,
                                name VARCHAR(100) NOT NULL UNIQUE,
                                sync_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE keycloak_permissions (
                                      id UUID PRIMARY KEY,
                                      name VARCHAR(100) NOT NULL UNIQUE,
                                      sync_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE keycloak_role_composites (
                                          role_id UUID REFERENCES keycloak_roles(id),
                                          permission_id UUID REFERENCES keycloak_permissions(id),
                                          sync_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                          PRIMARY KEY (role_id, permission_id)
);