package com.user.management.services;

<<<<<<< HEAD
import com.user.management.model.event.KeycloakEvent;
=======
>>>>>>> 9082cad52326dd5df1a5ed89e2b8aec65a0d54c2
import com.user.management.model.event.UserProvisioningEvent;

public interface UserProvisioningService {

    void handleUserCreated(UserProvisioningEvent event);

    void handleUserUpdated(UserProvisioningEvent event);

    void handleUserDeleted(UserProvisioningEvent event);
<<<<<<< HEAD

    void handleKeycloakEvent(KeycloakEvent event);
=======
>>>>>>> 9082cad52326dd5df1a5ed89e2b8aec65a0d54c2
}
