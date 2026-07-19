package com.user.management.services.impl;

import com.user.management.dto.request.AdminUserRequestDTO;
import com.user.management.entity.UserType;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeycloakServiceImplTest {

    @Mock
    private Keycloak keycloak;

    @Mock
    private RealmResource realmResource;
    @Mock
    private UsersResource usersResource;
    @Mock
    private UserResource userResource;
    @Mock
    private RolesResource rolesResource;
    @Mock
    private RoleResource roleResource;
    @Mock
    private RoleMappingResource roleMappingResource;
    @Mock
    private RoleScopeResource roleScopeResource;


    @InjectMocks
    private KeycloakServiceImpl keycloakService;

    private final String REALM_NAME = "test-realm";

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(keycloakService, "realm", REALM_NAME);
    }

    private void mockKeycloakChain() {
        when(keycloak.realm(REALM_NAME)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);

    }

    @Test
    void createKeycloakUser_Success() throws Exception {
        mockKeycloakChain();
        UUID id = UUID.randomUUID();
        AdminUserRequestDTO request = new AdminUserRequestDTO();
        request.setUsername("testuser");
        UserType userType = UserType.builder()
                .roleName("user-role").build();

        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatus()).thenReturn(201);
        when(mockResponse.getLocation())
                .thenReturn(new URI("http://localhost/users/"+id));
        when(usersResource.create(any(UserRepresentation.class)))
                .thenReturn(mockResponse);

        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get("user-role")).thenReturn(roleResource);
        when(roleResource.toRepresentation())
                .thenReturn(new RoleRepresentation());

        when(usersResource.get(id.toString())).thenReturn(userResource);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

        String resultId = keycloakService.createKeycloakUser(id, request, userType);

        assertEquals(id.toString(), resultId);
        verify(usersResource).create(any());
        verify(userResource).executeActionsEmail(any());
    }


    @Test
    void updateKeycloakStatus_Success() {
        mockKeycloakChain();
        UUID id = UUID.randomUUID();
        UserRepresentation userRep = new UserRepresentation();
        userRep.setEnabled(false);

        when(usersResource.get(id.toString())).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(userRep);


        keycloakService.updateKeycloakStatus(id, true);


        assertTrue(userRep.isEnabled());
        verify(userResource).update(userRep);
    }

    @Test
    void deleteKeycloakUser_Success() {
        mockKeycloakChain();
        UUID id = UUID.randomUUID();
        when(usersResource.get(id.toString())).thenReturn(userResource);

        keycloakService.deleteKeycloakUser(id);

        verify(userResource).remove();
    }

    @Test
    void deleteKeycloakUser_HandlesNotFound() {
        mockKeycloakChain();
        UUID id = UUID.randomUUID();
        when(usersResource.get(id.toString())).thenReturn(userResource);


        doThrow(new NotFoundException()).when(userResource).remove();


        assertDoesNotThrow(() -> keycloakService.deleteKeycloakUser(id));
    }


    @Test
    void updateKeycloakUser_Success() {
        mockKeycloakChain();
        UUID id = UUID.randomUUID();
        AdminUserRequestDTO request = new AdminUserRequestDTO();
        request.setEmail("new@test.com");
        request.setFirstName("NewFirst");
        request.setLastName("NewLast");

        UserRepresentation userRep = new UserRepresentation();

        when(usersResource.get(id.toString())).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(userRep);


        keycloakService.updateKeycloakUser(id, request);


        assertEquals("new@test.com", userRep.getEmail());
        assertEquals("NewFirst", userRep.getFirstName());
        verify(userResource).update(userRep);
    }

    @Test
    void sendWelcomeEmail_Success() {
        mockKeycloakChain();
        UUID id = UUID.randomUUID();
        when(usersResource.get(id.toString())).thenReturn(userResource);


        keycloakService.sendWelcomeEmail(id);


        verify(userResource).executeActionsEmail(List.of("UPDATE_PASSWORD", "VERIFY_EMAIL"));
    }

    @Test
    void getRealmRoles_ReturnsListOfNames() {
        when(keycloak.realm(REALM_NAME)).thenReturn(realmResource);
        when(realmResource.roles()).thenReturn(rolesResource);


        RoleRepresentation role1 = new RoleRepresentation();
        role1.setName("admin");
        RoleRepresentation role2 = new RoleRepresentation();
        role2.setName("user");

        when(rolesResource.list()).thenReturn(List.of(role1, role2));


        List<String> result = keycloakService.getRealmRoles();


        assertEquals(2, result.size());
        assertTrue(result.contains("admin"));
        assertTrue(result.contains("user"));
    }

    @Test
    void realmRoleExists_ReturnsTrue_WhenExists() {
        when(keycloak.realm(REALM_NAME)).thenReturn(realmResource);
        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get("admin")).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(new RoleRepresentation());

        boolean exists = keycloakService.realmRoleExists("admin");

        assertTrue(exists);
    }

    @Test
    void findIdByUsername_ReturnsId() {
        mockKeycloakChain();
        String username = "search_user";
        UserRepresentation userRep = new UserRepresentation();
        userRep.setId("keycloak-id-123");

        when(usersResource.search(username, true)).thenReturn(List.of(userRep));

        String result = keycloakService.findIdByUsername(username);

        assertEquals("keycloak-id-123", result);
    }
}