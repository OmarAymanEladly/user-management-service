package com.user.management.services;

import com.user.management.model.entity.UserType;
import com.user.management.model.event.UserProvisioningEvent;

import java.util.Optional;

public interface UserTypeMappingService {
    public UserType mapUserType(UserProvisioningEvent event);
}
