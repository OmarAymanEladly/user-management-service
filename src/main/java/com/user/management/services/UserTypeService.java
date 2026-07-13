package com.user.management.services;

import com.user.management.dto.request.UserTypeRequestDTO;
import com.user.management.dto.response.UserTypeResponseDTO;

import java.util.List;
import java.util.UUID;

public interface UserTypeService {
    UserTypeResponseDTO createType(UserTypeRequestDTO request);

    List<UserTypeResponseDTO> getAllTypes();

    UserTypeResponseDTO getTypeById(UUID id);

    UserTypeResponseDTO updateType(UUID id, UserTypeRequestDTO request);

    void deleteType(UUID id);
    UserTypeResponseDTO deactivateType(UUID id);
}
