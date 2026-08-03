
INSERT INTO keycloak_roles (id, name, sync_status)
VALUES (gen_random_uuid(), 'portal-admin', 'PENDING')
ON CONFLICT (name) DO NOTHING;

-- 2. Create the Client Roles (Permissions)
INSERT INTO keycloak_permissions (id, name, sync_status)
VALUES
    (gen_random_uuid(), 'user:create', 'PENDING'),
    (gen_random_uuid(), 'user:delete', 'PENDING')
ON CONFLICT (name) DO NOTHING;

-- 3. Create the Links (Composites)
-- Note: Use a subquery to find the IDs we just generated
INSERT INTO keycloak_role_composites (role_id, permission_id, sync_status)
SELECT r.id, p.id, 'PENDING'
FROM keycloak_roles r, keycloak_permissions p
WHERE r.name = 'portal-admin' AND p.name IN ('user:create', 'user:delete')
ON CONFLICT (role_id, permission_id) DO NOTHING;