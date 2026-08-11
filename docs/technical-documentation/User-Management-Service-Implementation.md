# User Management Service

## Implementation Documentation — Assigned Tasks

**Spring Boot • Keycloak 26.x • PostgreSQL • Flyway • Kafka • Outbox • Google OIDC**

## Implementation Documentation

## 1. Scope

This document covers the implementation items included in the assigned tasks and the corresponding technical configuration.

Reviewed task areas:

- UserType CRUD operations and Postman collection.

- User creation through the Keycloak Admin API.

- Welcome email with UPDATE_PASSWORD and VERIFY_EMAIL actions.

- Delegation creation: User A → User B, role retrieval from Keycloak, role assignment, and persistence.

- Delegation status validation and scheduled status transitions.

- Keycloak-down handling for user and delegation update/delete/activate/deactivate operations.

- UserType synchronization with Keycloak.

- UserType-specific Keycloak User Profile attributes/groups and custom field validation.

- Email template and password reset / update-password flow.

- Synchronization of Keycloak brute-force blocked/unblocked users with the local database.

- Google OIDC / Identity Provider integration.

- Delegation revoke API and delegated-role removal.

- Dynamic role synchronization using Flyway-seeded pending records and a background worker.

## 2. Implementation Status Summary

| Task | Status | Implementation evidence |
| --- | --- | --- |
| UserType CRUD + Postman | Implemented | UserTypeController + Postman collections |
| Admin user creation + Keycloak Admin API | Implemented | AdminUserServiceImpl + KeycloakServiceImpl |
| Welcome email | Implemented | executeActionsEmail(UPDATE_PASSWORD, VERIFY_EMAIL) |
| Delegation create + role copy | Implemented | DelegationServiceImpl + KeycloakServiceImpl |
| Delegated role persistence | Implemented | delegations.delegated_roles JSONB |
| ACTIVE / SCHEDULED validation | Implemented | DelegationServiceImpl |
| Scheduled activation / expiration | Implemented | DelegationProcessor |
| Keycloak-down retry | Implemented for persisted operations | OutboxProcessor |
| UserType → Keycloak synchronization | Implemented | UserTypeServiceImpl + KeycloakServiceImpl |
| UserType field visibility/validation | Implemented | Custom Validator extension |
| Welcome email template | Implemented | Keycloak theme executeActions.ftl |
| Forgot/reset password UI | Implemented | login-reset-password.ftl + Keycloak flow |
| Blocked/unblocked synchronization | Implemented | Kafka consumer + UserProvisioningServiceImpl |
| Google OIDC | Configured | Keycloak Identity Provider alias google |
| Delegation revoke | Implemented | PATCH /api/delegations/{id}/revoke |
| Dynamic role synchronization | Implemented | 3 DB tables + Flyway seed + RoleSyncProcessor |
| Dynamic migration script to create composite roles | Implemented | Flyway inserts PENDING records into the three synchronization tables;<br>RoleSyncProcessor processes them and creates/synchronizes composite roles in Keycloak |
## 3. UserType CRUD Operations

Controller: com.user.management.controller.UserTypeController

```text
POST   /api/userTypes
GET    /api/userTypes
GET    /api/userTypes/{id}
PUT    /api/userTypes/{id}
PATCH  /api/userTypes/{id}/deactivate
DELETE /api/userTypes/{id}
GET    /api/userTypes/roles
GET    /api/userTypes/public/user-types
```

The service validates the selected Keycloak realm role before creating/updating a UserType and synchronizes the UserType definition to Keycloak. If Keycloak cannot be reached during synchronization, the service records an Outbox event for later processing.

Postman collections:

- docs/postman/postman_collection.json — main authenticated API collection.

- docs/postman/postman_collection_noauth.json — CRUD/testing collection without API security.

- docs/postman/postman_collection_keycloak_data_check.json — Keycloak/API data checks.

- docs/postman/postman_self_signup_approval_flow.json — self-signup approval flow.

- docs/postman/keycloak-demo.postman_collection.json — Keycloak login/refresh/logout examples.

## 4. User Creation and Keycloak Admin API

Endpoint: POST /api/admin/users

```text
AdminUserController
    ↓
AdminUserServiceImpl.createUser()
    ↓
Save ManagedUser locally
    ↓
KeycloakServiceImpl.createKeycloakUser()
    ↓
Keycloak Admin API: realm.users().create(user)
    ↓
Assign UserType realm role
    ↓
Send UPDATE_PASSWORD + VERIFY_EMAIL email
```

The Keycloak integration uses the Keycloak Admin Client. The local user is created first and an Outbox event is used to keep the local database and Keycloak eventually consistent when Keycloak is unavailable. The normal successful path creates the Keycloak user, assigns the UserType role, and sends the required actions email.

Important implementation detail: the service requests the Keycloak-generated user ID and uses it as the authoritative Keycloak ID. The project also contains healing logic for a local-ID/Keycloak-ID mismatch in the Outbox processor.

## 5. Welcome Email and Password Setup

KeycloakServiceImpl.sendWelcomeEmail() calls:

```text
executeActionsEmail(
    List.of("UPDATE_PASSWORD", "VERIFY_EMAIL")
)
```

Therefore a newly created user is directed to complete the initial account setup instead of receiving a plain password. The custom Keycloak email theme contains executeActions.ftl, which renders the required actions in a branded email and provides a Complete Account Setup button.

The email messages map the technical actions to readable text:

- UPDATE_PASSWORD → Update Password

- VERIFY_EMAIL → Verify Email

- CONFIGURE_TOTP → Configure Authenticator

- UPDATE_PROFILE → Update Profile Information

The custom login theme also contains login-update-password.ftl with password strength feedback and a confirmation field.

## 6. Forgot / Reset Password

The custom theme implements the Keycloak reset-password page in login-reset-password.ftl.

- Username or email input.

- Send Reset Link action.

- Back to Login navigation.

- Custom card, icon, title, explanatory text, message area, and styled form.

- The actual reset operation remains handled by Keycloak's reset-password flow.

## 7. Delegation: User A → User B

Endpoints:

```text
POST  /api/delegations
PATCH /api/delegations/{id}/revoke
```

Create flow:

```text
1. Validate startTime is not in the past.
2. Validate endTime > startTime.
3. Call Keycloak to read User A's realm roles.
4. Store the delegated role names in the delegation row.
5. Determine status:
       near-immediate start → ACTIVE
       future start          → SCHEDULED
6. If ACTIVE, assign the copied roles to User B.
7. Persist a DELEGATION_CREATED Outbox event when needed.
```

Database representation:

```text
delegations
- id
- delegator_id
- delegatee_id
- delegated_roles (JSONB)
- start_time
- end_time
- status
- created_at
```

KeycloakServiceImpl.getUserRoles() filters technical/default roles such as default-roles, offline_access, and uma_authorization before storing the delegated role list.

## 8. Delegation Worker and Lifecycle

DelegationProcessor runs using the configured delegation check interval (app.delegation.check-ms).

```text
SCHEDULED + startTime <= now
        ↓
     ACTIVE
        ↓
Assign delegated roles to User B

ACTIVE + endTime <= now
        ↓
     EXPIRED
        ↓
Remove delegated roles from User B
```

The worker writes an Outbox event when the Keycloak operation succeeds or fails. A failed Keycloak operation remains pending so the Outbox worker can retry it.

## 9. Delegation Revoke Flow

```text
ADMIN
  │
  │ PATCH /api/delegations/{id}/revoke
  ▼
DelegationServiceImpl
  │
  ├─ Read delegation
  ├─ Reject if already REVOKED or EXPIRED
  ├─ Save status = REVOKED
  │
  └─ If previous status == ACTIVE
        │
        ▼
   Keycloak: remove delegated roles from User B
        │
        └─ failure → Outbox PENDING
```

If the delegation was still SCHEDULED, the revoke operation changes the database status to REVOKED without attempting to remove roles because they were never activated. If the delegation was ACTIVE, the delegated roles are removed from User B. A Keycloak failure produces a pending Outbox event.

## 10. Keycloak-Down Handling and Outbox

The project uses an Outbox pattern for operations that must eventually synchronize with Keycloak.

```text
Local database operation
        ↓
Create/Update/Delete/Activate/Deactivate
        ↓
Outbox event = PENDING
        ↓
OutboxProcessor (periodic)
        ↓
Attempt Keycloak operation
   ┌───────────────┴───────────────┐
 Success                         Failure
   ↓                                ↓
PROCESSED                    retry_count++
                             last_error saved
                             remains PENDING
```

Covered event categories include USER, USER_TYPE, and DELEGATION.

- USER_CREATED / UPDATED / DELETED / ACTIVATED / DEACTIVATED

- USER_TYPE_CREATED / UPDATED / DELETED

- DELEGATION_CREATED / ACTIVATED / EXPIRED / REVOKED

The implementation does not currently move events to FAILED after a retry threshold; the five-retry block is commented out. Therefore the current behavior is effectively retry-until-success while the event remains PENDING.

## 11. UserType Synchronization with Keycloak

UserTypeServiceImpl creates/updates/deletes UserTypes locally and invokes KeycloakServiceImpl to synchronize the Keycloak User Profile configuration.

- Realm attribute user_type_list is rebuilt from the current database UserTypes.

- A user_type attribute is maintained in the Keycloak User Profile.

- Each UserType gets a dedicated User Profile group, e.g. developer-group.

- Fields marked syncToKeycloak are added to the corresponding Keycloak group.

- Each synchronized field receives the user-type-field-dependency validator.

- The field's required_role validator configuration is set to the UserType name.

- Deleting a UserType removes its corresponding Keycloak User Profile group and fields, unless the type has been recreated.

The local request-to-Keycloak mapping only sends fields that belong to the selected UserType and are marked for Keycloak synchronization.

## 12. Custom Keycloak UserType Field Validator

The custom validator is implemented as a Keycloak Validator SPI.

```text
Validator ID:
user-type-field-dependency

Configuration:
required_role = <USER TYPE>
```

The validator checks the selected user_type from the current validation context. If it cannot find it there, it attempts to resolve it from the current user or the user ID in the request path. If the selected type does not match required_role, it adds a ValidationError and prevents the value from being accepted.

Provider registration:

```text
META-INF/services/org.keycloak.validate.ValidatorFactory
    → com.user.management.validator.UserTypeFieldValidatorFactory
```

This is the implementation behind the UserType-dependent field validation rather than relying on a CSS/selector-only solution.

## 13. Keycloak Blocked/Unblocked User Synchronization

The project uses a Kafka-based event path to synchronize Keycloak brute-force block/unblock state into the local ManagedUser record.

```text
Keycloak event
    ↓
Kafka: user-events
    ↓
UserProvisioningConsumerConfig
    ↓
UserProvisioningServiceImpl.handleKeycloakEvent()
    ↓
USER_BLOCKED
    → call Keycloak attackDetection.bruteForceUserStatus()
    → if disabled=true, local ManagedUser.enabled=false

USER_UNBLOCKED
    → local ManagedUser.enabled=true
```

Admin update operations also check isUserBlocked() and reject a user update when Keycloak reports that the account is blocked because of repeated failed login attempts.

## 14. Dynamic Role / Permission Migration and Composite Role Synchronization

The dynamic migration uses three dedicated database tables to store the role, permission, and composite-role definitions that must be synchronized with Keycloak:

```text
keycloak_roles
keycloak_permissions
keycloak_role_composites
```

Flyway migrations create the three tables and insert the required synchronization records with status = PENDING. These records act as the migration requests that the background worker processes.

```text
Flyway creates the synchronization requests:

Realm role:
    super-manager

Client permissions:
    user:unlock       → user-management-client
    inventory:view    → inventory-service

Composite role:
    super-manager
       ├── user:unlock
       └── inventory:view

The records are initially stored as PENDING and are processed by the background worker.
```

RoleSyncProcessor runs periodically and searches the database for PENDING synchronization requests. It processes the realm roles and client permissions first, then processes the composite-role records after their referenced roles/permissions are available in Keycloak.

```text
Flyway migration
      ↓
Insert PENDING records into:
  keycloak_roles
  keycloak_permissions
  keycloak_role_composites
      ↓
RoleSyncProcessor runs periodically
      ↓
Read PENDING records from database
      ↓
Create/synchronize realm roles
      ↓
Create/synchronize client permissions
      ↓
Create composite-role relationships
      ↓
Update synchronization status to SYNCED
```

This provides a dynamic migration mechanism for creating and synchronizing Keycloak role definitions, client permissions, and composite roles. The Flyway scripts populate the synchronization tables, while RoleSyncProcessor reads the PENDING records and applies the required changes to Keycloak, updating the records after successful synchronization.

## 15. Google OIDC / Gmail Identity Provider Integration

A Google Identity Provider is configured in the user-management realm. The provider alias is google and the provider is enabled.

- Identity Provider: Google.

- Alias: google.

- Keycloak redirect URI follows the Keycloak broker endpoint: /realms/user-management/broker/google/endpoint.

- Google Cloud OAuth client type: Web application.

- Authorized JavaScript origin: http://localhost:8081.

- Authorized redirect URI: http://localhost:8081/realms/user-management/broker/google/endpoint.

- Keycloak setting Trust Email is enabled.

- Sync Mode is Import.

- First Login Flow is first broker login.

- Post Login Flow is None.

- The provider is exposed on the custom login page as the Google sign-in option.

For security, the client secret is intentionally not reproduced in this document. The OAuth client is enabled and configured for the application.

![Configuration screenshot](User_Management_Task_Documentation_Dynamic_Migration_assets/image1.png)

Keycloak Google Identity Provider — General settings

![Configuration screenshot](User_Management_Task_Documentation_Dynamic_Migration_assets/image2.png)

Keycloak Google Identity Provider — Advanced settings

![Configuration screenshot](User_Management_Task_Documentation_Dynamic_Migration_assets/image3.png)

Google Cloud OAuth Web Application client configuration

## 16. Keycloak Email and Theme Configuration

Relevant theme files confirmed in the project:

- login/login-reset-password.ftl — custom forgot-password page.

- login/login-update-password.ftl — custom password update page.

- email/html/executeActions.ftl — custom welcome/required-actions email.

- email/messages/messages_en.properties — human-readable required-action labels.

- login/resources/css/custom-login.css — shared login/reset/update-password styling.

The Google Identity Provider configuration is separate from SMTP/email delivery. The executeActionsEmail call relies on Keycloak's configured email server to deliver the welcome/reset-related message.

## 17. End-to-End Flow for the Main Assigned Features

```text
A) USER CREATION

Admin
  ↓ POST /api/admin/users
User Management API
  ↓
PostgreSQL ManagedUser
  ↓
Keycloak Admin API
  ├─ create user
  ├─ assign UserType realm role
  └─ send UPDATE_PASSWORD + VERIFY_EMAIL
  ↓
User completes account setup


B) DELEGATION

Admin
  ↓ POST /api/delegations
User Management API
  ↓
Keycloak → get User A roles
  ↓
PostgreSQL → save delegation + delegatedRoles
  ↓
ACTIVE: assign roles to User B
SCHEDULED: DelegationProcessor waits for startTime
  ↓
ACTIVE → assign roles
  ↓
EXPIRED → remove roles


C) REVOKE

Admin
  ↓ PATCH /api/delegations/{id}/revoke
PostgreSQL → status = REVOKED
  ↓
If previous status = ACTIVE
  ↓
Keycloak → remove delegated roles from User B
  ↓
Failure → Outbox PENDING → retry


D) USER TYPE

Admin
  ↓
UserType CRUD
  ↓
PostgreSQL user_types
  ↓
Keycloak User Profile
  ├─ user_type
  ├─ <type>-group
  └─ type-specific fields + custom validator


E) KEYCLOAK FAILURE

Local transaction
  ↓
Outbox PENDING
  ↓
OutboxProcessor
  ↓
retry Keycloak call periodically
  ↓
PROCESSED when successful
```

## 18. Key Source Files

- controller/UserTypeController.java

- controller/AdminUserController.java

- controller/DelegationController.java

- services/impl/UserTypeServiceImpl.java

- services/impl/AdminUserServiceImpl.java

- services/impl/KeycloakServiceImpl.java

- services/impl/DelegationServiceImpl.java

- services/impl/OutboxServiceImpl.java

- scheduler/DelegationProcessor.java

- scheduler/OutboxProcessor.java

- scheduler/RoleSyncProcessor.java

- services/impl/UserProvisioningServiceImpl.java

- config/UserProvisioningConsumerConfig.java

- db/migration/V15__create_role_sync_tables.sql

- db/migration/V21__test_table_roles.sql

- themes/my-portal-theme/login/login-reset-password.ftl

- themes/my-portal-theme/login/login-update-password.ftl

- themes/my-portal-theme/email/html/executeActions.ftl

- themes/my-portal-theme/email/messages/messages_en.properties

- providers/keycloak-field-validator-1.0.0.jar

## 19. Notes / Technical Caveats

- Delegation creation currently requires the initial Keycloak role retrieval. Although the code catches the retrieval exception, it ultimately throws 'Keycloak is down try again later' instead of creating a role-less pending delegation. Therefore the Outbox retry mechanism is strongest for already-persisted operations, not for the initial role-fetch step.

- Outbox events remain PENDING after repeated failures because the retry-limit-to-FAILED logic is currently commented out.

- The dynamic role synchronization worker implements the migration flow for realm roles, client roles/permissions, and composite roles. Flyway inserts the required records into the three synchronization tables with PENDING status, and RoleSyncProcessor periodically reads those pending records and creates or updates the corresponding objects in Keycloak.

- Google OIDC is configured as an external Identity Provider. Google OAuth client credentials must remain outside source control; the client secret is not documented here.

- Compiled provider JARs are included under providers/, and the custom extension implements the user-type validator and SPI registration.

## 20. Conclusion

The implementation covers UserType management, Keycloak Admin API integration, welcome/password setup, delegation lifecycle and revoke behavior, Keycloak synchronization, blocked-user synchronization, custom UserType field validation, dynamic role/permission migration with composite-role creation, Google OIDC integration, and custom Keycloak UI/email theme work. The main implementation caveats are the initial Keycloak dependency during delegation creation and the absence of an active retry threshold.
