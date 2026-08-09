
INSERT INTO keycloak_roles (id, name, sync_status)
VALUES (gen_random_uuid(), 'super-manager', 'PENDING')
ON CONFLICT (name) DO NOTHING;


INSERT INTO keycloak_permissions (id, name, client_Id, sync_status)
VALUES
    (gen_random_uuid(), 'user:unlock', 'user-management-client', 'PENDING'),
    (gen_random_uuid(), 'inventory:view', 'inventory-service', 'PENDING')
ON CONFLICT (name) DO NOTHING;



INSERT INTO keycloak_role_composites (role_id, permission_id, sync_status)
SELECT r.id, p.id, 'PENDING'
FROM keycloak_roles r, keycloak_permissions p
WHERE r.name = 'super-manager'
  AND p.name IN ('user:unlock', 'inventory:view')
ON CONFLICT (role_id, permission_id) DO NOTHING;