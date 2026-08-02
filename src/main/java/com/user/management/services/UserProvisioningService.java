package com.user.management.services;

import com.user.management.model.event.UserProvisioningEvent;

public interface UserProvisioningService {

    void handleUserCreated(UserProvisioningEvent event);

    void handleUserUpdated(UserProvisioningEvent event);

    void handleUserDeleted(UserProvisioningEvent event);
}
