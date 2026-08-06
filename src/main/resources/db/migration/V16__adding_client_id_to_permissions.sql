ALTER TABLE keycloak_permissions
    ADD COLUMN client_id VARCHAR(100) NOT NULL DEFAULT 'user-management-client';
