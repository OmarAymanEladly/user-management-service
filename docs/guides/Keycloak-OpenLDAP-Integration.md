# Integrating Keycloak with OpenLDAP

> **Note:** This setup is intended for development and testing using a locally hosted OpenLDAP server running in Docker.

## Prerequisites

- An OpenLDAP server running and accessible.
- An administrator account.
- Keycloak running and accessible.

---

## 1. Create an LDAP Provider

Navigate to:

```text
Realm -> User Federation -> Add provider -> LDAP
```

### Connection Settings

| Setting | Value |
|---------|-------|
| Vendor | `Other` |
| Connection URL | `ldap://openldap:389` *(or `ldap://localhost:389` if Keycloak is running outside Docker)* |
| Bind Type | `simple` |
| Bind DN | `cn=admin,dc=linkdev,dc=local` |
| Bind Credentials | `admin123` |

Click:

- **Test connection**
- **Test authentication**

Both should succeed.

---

## 2. LDAP Searching and Updating

| Setting | Value |
|---------|-------|
| Edit Mode | `WRITABLE` *(or `READ_ONLY` if provisioning is not required)* |
| Users DN | `ou=People,dc=linkdev,dc=local` |
| Username LDAP Attribute | `uid` |
| RDN LDAP Attribute | `uid` |
| UUID LDAP Attribute | `entryUUID` |
| User Object Classes | `inetOrgPerson, organizationalPerson, person` |

- Import Users: Enable
- Sync Registrations: Disable

Save the provider.

---

## 3. Configure LDAP Synchronization

Periodic synchronization allows Keycloak to automatically detect new users and changes made directly in OpenLDAP.

Navigate to:

```text
User Federation → LDAP
```

Configure the synchronization settings:

| Setting | Value |
|---------|-------|
| Periodic Full Sync | **On** |
| Full Sync Period | *Choose an appropriate interval (e.g. 86400 seconds)* |
| Periodic Changed Users Sync | **On** |
| Changed Users Sync Period | *Choose an appropriate interval (e.g. 300 seconds)* |

Save the provider.

- **Periodic Full Sync** imports newly created LDAP users and performs a complete synchronization.
- **Periodic Changed Users Sync** synchronizes only users that have changed since the previous synchronization, reducing the load on the LDAP server.

> **Note:** Manual synchronization can still be triggered at any time using:
>
> ```text
> User Federation → LDAP → Action → Synchronize all users
> ```

---

## 4. Configure the LDAP Provisioning Mapper

The custom **ldap-provisioning-mapper** is responsible for publishing provisioning events whenever users are imported or synchronized from OpenLDAP.

Navigate to:

```text
User Federation → LDAP → Mappers → Add Mapper
```

Use the following settings:

| Setting | Value |
|---------|-------|
| Mapper Type | `ldap-provisioning-mapper` |

Save the mapper.

> **Note:** Without this mapper, users imported from OpenLDAP will still be synchronized into Keycloak, but no provisioning events will be published to the user-management-service.

---

## 5. Configure Group Mapper

This maps the groups provided by the LDAP server to Keycloak groups and allows assigning Realm or Client Roles to the entire group.

Navigate to:

```text
User Federation → LDAP → Mappers → Add Mapper
```

Use the following settings:

| Setting | Value |
|---------|-------|
| Mapper Type | `group-ldap-mapper` |
| LDAP Groups DN | `ou=Groups,dc=linkdev,dc=local` |
| Group Name LDAP Attribute | `cn` |
| Group Object Classes | `groupOfNames` |
| Membership LDAP Attribute | `member` |
| Membership Attribute Type | `DN` |
| Membership User LDAP Attribute | `uid` |
| Mode | `LDAP_ONLY` |
| User Groups Retrieve Strategy | `LOAD_GROUPS_BY_MEMBER_ATTRIBUTE` |
| Decode UUID Attribute to UUID Format | `off` |
| Groups Path | `/` |

Save the mapper.

### LDAP User Attribute Mappers

By default, the imported LDAP attribute mappers are configured as **Read Only**.

This means that any changes made to user attributes in Keycloak are **not written back to LDAP**. After saving, Keycloak immediately reloads the original values from the LDAP server.

**Configuration for writing data from Keycloak**

Navigate to:

```text
User Federation → LDAP → Mappers
```

For each writable user attribute mapper (e.g. **First Name**, **Last Name**, **Email**):

| Setting | Value |
|---------|-------|
| Read Only | **Off** |
| Always Read Value From LDAP | **On** |

> **Note:** This only has an effect if the LDAP provider's **Edit Mode** is set to `WRITABLE`.

---

## 7. Map Groups to Roles

1. Create realm roles.
2. Assign each role to its corresponding Keycloak group.

---

## 8. Test Login Through Keycloak

1. Open:

   ```
   http://localhost:8081/realms/<realm-name>/account
   ```

2. Log in using an LDAP user.

   Example:

   ```
   Username: momen
   Password: 123
   ```

3. If authentication succeeds, the user is successfully authenticated against the LDAP server through Keycloak.

4. If authentication fails:

    - Verify that the LDAP provider connection and authentication tests succeed.
    - Verify that the user exists under the configured **Users DN**.
    - Verify that the user has a valid `userPassword` attribute.
    - Verify that the user can authenticate directly against OpenLDAP.

---
