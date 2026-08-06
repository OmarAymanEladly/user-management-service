package com.user.management.services;

import com.user.management.dto.request.AdminUserRequestDTO;
import com.user.management.dto.request.UserApprovalStatusRequestDTO;
import com.user.management.dto.response.AdminUserResponseDTO;

import java.util.List;
import java.util.UUID;

public interface AdminUserService {
    AdminUserResponseDTO createUser(AdminUserRequestDTO request);
    List<AdminUserResponseDTO> getAllUsers(String signupApprovalStatus);
    AdminUserResponseDTO updateUser(UUID id, AdminUserRequestDTO request);
    AdminUserResponseDTO activateUser(UUID id);
    AdminUserResponseDTO deactivateUser(UUID id);
    AdminUserResponseDTO approveSignup(UUID id);
    AdminUserResponseDTO rejectSignup(UUID id);
    AdminUserResponseDTO updateApprovalStatus(UUID id, UserApprovalStatusRequestDTO request);
    void deleteUser(UUID id);
    AdminUserResponseDTO getUserById(UUID id);
}
