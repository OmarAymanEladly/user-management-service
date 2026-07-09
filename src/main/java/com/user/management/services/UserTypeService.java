package com.user.management.services;

import com.user.management.dto.request.UserTypeRequestDTO;
import com.user.management.dto.response.UserTypeResponseDTO;
import java.util.List;

public interface UserTypeService {
    UserTypeResponseDTO createType(UserTypeRequestDTO request);
    List<UserTypeResponseDTO> getAllTypes();
    UserTypeResponseDTO getTypeById(Long id);
    UserTypeResponseDTO updateType(Long id, UserTypeRequestDTO request);
    void deleteType(Long id);
}