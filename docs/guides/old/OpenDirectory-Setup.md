# Connecting to OpenDirectory

There are two options to connect to the LDAP server hosted on OpenDirectory

1. Using the Browser 
2. Using Apache Directory Studio (Better)

## 1. The Browser
1. Go to [opendirectory.net](https://opendirectory.net/)
2. Click Login
3. Fill in `dc=linkdev`  
4. You will get redirected to a login form, fill in the `The administrator password`

## 2. Apache Directory Studio

### Prerequisites

-   [Apache Directory Studio](https://directory.apache.org/studio/downloads.html) installed

------------------------------------------------------------------------

### LDAP Connection Information

| Setting | Value |
|---------|-------|
| Host | `linkdev.opendirectory.net` |
| Port | `389` |
| Encryption | `No encryption` |
| Authentication | `Simple Authentication` |
| Bind DN | `cn=linkdev,ou=admins,dc=opendirectory,dc=net` |
| Password | *The administrator password* |
| Base DN | `dc=linkdev,dc=opendirectory,dc=net` |

------------------------------------------------------------------------

### 1. Create a New LDAP Connection

1.  Open **Apache Directory Studio**.
2.  Select **File -> New -> LDAP Connection**.
3.  Fill in:
    ```
        Connection Name: link-dev
        Hostname: linkdev.opendirectory.net
        Port: 389
        Encryption Method: No encryption
    ```
4.  Click **Check Network Parameter**.
5.  Verify the connection succeeds.
6.  Click **Next**.

------------------------------------------------------------------------

### 2. Authenticate

Choose:

    Authentication Method: Simple Authentication

Enter:

    Bind DN: cn=linkdev,ou=admins,dc=opendirectory,dc=net
    Password: <your admin password>

Click **Check Authentication**.

If authentication succeeds, click **Next**.

------------------------------------------------------------------------

### 3. Browser Options

Initially, Apache Directory Studio may discover:

    dc=opendirectory,dc=net

Replace it with:

    dc=linkdev,dc=opendirectory,dc=net

Leave the remaining options at their default values.

Click **Finish**.

------------------------------------------------------------------------
