package com.user.management.mapper;


import com.user.management.dto.request.AdminUserRequestDTO;

import com.user.management.model.entity.ManagedUser;
import com.user.management.model.event.UserProvisioningEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserProvisioningMapper {

    public ManagedUser toEntity(UserProvisioningEvent event) {
        return ManagedUser.builder()
                .id(UUID.fromString(event.keycloakId()))
                .username(event.username())
                .firstName(event.firstName())
                .lastName(event.lastName())
                .email(event.email())
                .phoneNumber(event.phoneNumber())
                .isNewUser(true)
                .build();
    }


    public AdminUserRequestDTO toAdminRequest(ManagedUser user) {
        AdminUserRequestDTO dto = new AdminUserRequestDTO();

        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setEnabled(user.getEnabled());

        dto.setUserTypeId(user.getUserType().getId());
        dto.setAttributes(user.getAttributes());

        return dto;
    }


    public void update(ManagedUser user, UserProvisioningEvent event) {
        user.setUsername(event.username());
        user.setFirstName(event.firstName());
        user.setLastName(event.lastName());
        user.setEmail(event.email());
        user.setPhoneNumber(event.phoneNumber());
    }
}