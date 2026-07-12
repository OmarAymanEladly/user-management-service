package com.user.management.services;

import com.user.management.dto.request.AdminUserRequestDTO;
import com.user.management.dto.response.AdminUserResponseDTO;

import java.util.List;

public interface AdminUserService {
    AdminUserResponseDTO createUser(AdminUserRequestDTO request);
    List<AdminUserResponseDTO> getAllUsers();
    AdminUserResponseDTO getUserById(Long id);
    AdminUserResponseDTO updateUser(Long id, AdminUserRequestDTO request);
    AdminUserResponseDTO activateUser(Long id);
    AdminUserResponseDTO deactivateUser(Long id);
    void deleteUser(Long id);
}
