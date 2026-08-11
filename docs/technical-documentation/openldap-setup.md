# Connecting to OpenLDAP

There are two options to connect to the locally hosted OpenLDAP server.

1. Using phpLDAPadmin
2. Using Apache Directory Studio (Recommended)

## 1. phpLDAPadmin

### Prerequisites

- OpenLDAP running.
- phpLDAPadmin running.

Open:

```
http://localhost:8082
```

Log in using:

| Setting | Value |
|---------|-------|
| Login DN | `cn=admin,dc=linkdev,dc=local` |
| Password | `admin123` |

---

## 2. Apache Directory Studio

### Prerequisites

- [Apache Directory Studio](https://directory.apache.org/studio/downloads.html) installed.

------------------------------------------------------------------------

### 1. Create a New LDAP Connection

1. Open **Apache Directory Studio**.
2. Select **File → New → LDAP Connection**.
3. Fill in:

    ```text
    Connection Name: openldap-linkdev
    Hostname: localhost
    Port: 389
    Encryption Method: No encryption
    ```

4. Click **Check Network Parameter**.
5. Verify the connection succeeds.
6. Click **Next**.

------------------------------------------------------------------------

### 2. Authenticate

Choose:

```text
Authentication Method: Simple Authentication
```

Enter:

```text
Bind DN: cn=admin,dc=linkdev,dc=local
Password: admin123
```

Click **Check Authentication**.

If authentication succeeds, click **Next**.

------------------------------------------------------------------------

### 3. Browser Options

Apache Directory Studio should automatically discover:

```text
dc=linkdev,dc=local
```

If it does not, manually specify:

```text
dc=linkdev,dc=local
```

Leave the remaining options at their default values.

Click **Finish**.

------------------------------------------------------------------------