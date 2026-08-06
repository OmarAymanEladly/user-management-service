# Integrating Keycloak with OpenDirectory

> **Note:** This setup is intended for development and testing. OpenDirectory is used temporarily and can later be replaced with OpenLDAP with minimal changes.

## Prerequisites

- A provisioned OpenDirectory instance.
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
| Connection URL | `ldap://linkdev.opendirectory.net:389` |
| Bind Type | `simple` |
| Bind DN | `cn=linkdev,ou=admins,dc=opendirectory,dc=net` |
| Bind Credentials | `<Admin Password>` |

Click:

- **Test connection**
- **Test authentication**

Both should succeed.

---

## 2. LDAP Searching and Updating

| Setting | Value |
|---------|-------|
| Edit Mode | `WRITABLE` *(or `READ_ONLY` if provisioning is not required)* |
| Users DN | `ou=People,dc=linkdev,dc=opendirectory,dc=net` |
| Username LDAP Attribute | `uid` |
| RDN LDAP Attribute | `uid` |
| UUID LDAP Attribute | `entryUUID` |
| User Object Classes | `inetOrgPerson, organizationalPerson` |

Enable:

- Import Users
- Sync Registrations (only if using `WRITABLE`)

Save the provider.

---

## 3. Import Users

Run:

```text
User Federation -> LDAP -> Action -> Synchronize all users
```

Verify that the LDAP users appear in the users tab by typing `*` in the search.

---

## 4. Configure Group Mapper

This maps the groups provided by the LDAP server to Keycloak groups and we can assign Realm or Client Roles to the entire group

Navigate to:

```text
User Federation → LDAP → Mappers → Add Mapper
```

Use the following settings:

| Setting | Value |
|---------|-------|
| Mapper Type | `group-ldap-mapper` |
| LDAP Groups DN | `ou=Groups,dc=linkdev,dc=opendirectory,dc=net` |
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

**Configuration for writing data from keycloak**

Navigate to:

```text
User Federation → LDAP → Mappers
```

For each writable user attribute mapper (e.g. **First Name**, **Last Name**):

| Setting | Value |
|---------|-------|
| Read Only | **Off** |
| Always Read Value From LDAP | **On** |

> **Note:** This only has an effect if the LDAP provider's **Edit Mode** is set to `WRITABLE`.

---

## 5. Map Groups to Roles

1. Create realm roles:

   - `ADMIN`
   - `DEVELOPER`
   - `USER`

2. Assign each role to its corresponding Keycloak group:

| Group | Role |
|-------|------|
| admins | ADMIN |
| developers | DEVELOPER |
| users | USER |

---
## 6. Test Login Through Keycloak

1. Open:
   ```
   http://localhost:8081/realms/<realm-name>/account
   ```
2. Log in using an LDAP user, the password for all current users is `123`

   Example:

   ```
   Username: momen
   Password: 123
   ```

3. If authentication succeeds, the user is successfully authenticated against the LDAP server through Keycloak.

4. If failed, check the Opendirectory Limitations section.


# OpenDirectory Limitations

The following limitations were discovered while integrating OpenDirectory with Keycloak.

- Passwords can only be assigned **when the LDAP user is created**.
- Passwords **cannot be modified** after the user has been created.
- Users created in **Keycloak** are synchronized to LDAP, but **their passwords cannot be initialized or managed**, since OpenDirectory does not allow password modification through LDAP.
- OpenDirectory only authenticated users whose passwords were created using the **Plaintext** option. Passwords created with other hash methods (e.g. SHA-512, SSHA-512) could not be used for authentication.
- The `userPassword` attribute is not readable and cannot be modified through Apache Directory Studio or the OpenDirectory web interface.

---

# Future Plan

OpenDirectory is used only as a temporary hosted LDAP server for development.

The preferred long-term solution is to migrate to **OpenLDAP**, which provides:

- Simple migration by exporting data into an LDIF file and importing it to OpenLDAP.
- Full control over permissions and LDAP behavior.
- Easy deployment using Docker Compose.

---