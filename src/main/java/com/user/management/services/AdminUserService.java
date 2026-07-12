package com.user.management.services;

import com.user.management.dto.request.AdminUserRequestDTO;
import com.user.management.dto.response.AdminUserResponseDTO;

public interface AdminUserService {
    AdminUserResponseDTO createUser(AdminUserRequestDTO request);
    AdminUserResponseDTO updateUser(Long id, AdminUserRequestDTO request);
    AdminUserResponseDTO activateUser(Long id);
    AdminUserResponseDTO deactivateUser(Long id);
    void deleteUser(Long id);
}
