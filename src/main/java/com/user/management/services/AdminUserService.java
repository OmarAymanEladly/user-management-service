package com.user.management.services;

import com.user.management.dto.request.AdminUserRequestDTO;
import com.user.management.dto.response.AdminUserResponseDTO;

import java.util.List;
import java.util.UUID;

public interface AdminUserService {
    AdminUserResponseDTO createUser(AdminUserRequestDTO request);
    AdminUserResponseDTO updateUser(UUID id, AdminUserRequestDTO request);
    AdminUserResponseDTO activateUser(UUID id);
    AdminUserResponseDTO deactivateUser(UUID id);
    void deleteUser(UUID id);
    List<AdminUserResponseDTO> getAllUsers();
    AdminUserResponseDTO getUserById(UUID id);
}
