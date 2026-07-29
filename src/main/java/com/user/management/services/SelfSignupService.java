package com.user.management.services;

import com.user.management.dto.request.SelfSignupRequestDTO;
import com.user.management.dto.response.AdminUserResponseDTO;

public interface SelfSignupService {
    AdminUserResponseDTO signup(SelfSignupRequestDTO request);
    AdminUserResponseDTO signupWithApproval(SelfSignupRequestDTO request);
}
