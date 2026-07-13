package com.user.management.services.impl;

import com.user.management.dto.response.UserTypeResponseDTO;
import com.user.management.entity.UserType;
import com.user.management.mapper.UserTypeMapper;
import com.user.management.repository.UserTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserTypeServiceImplTest {

    @Mock
    private UserTypeRepository repository;

    @Mock
    private UserTypeMapper mapper;

    @InjectMocks
    private UserTypeServiceImpl userTypeService;

    @Test
    void deactivateTypeSetsStatusToInactive() {
        UUID id = UUID.randomUUID();
        UserType userType = UserType.builder()
                .id(id)
                .type("employee")
                .status("ACTIVE")
                .build();
        UserTypeResponseDTO response = UserTypeResponseDTO.builder()
                .id(id)
                .type("employee")
                .status("INACTIVE")
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(userType));
        when(repository.save(any(UserType.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toResponse(userType)).thenReturn(response);

        UserTypeResponseDTO result = userTypeService.deactivateType(id);

        assertEquals("INACTIVE", userType.getStatus());
        assertEquals("INACTIVE", result.getStatus());
        verify(repository).save(userType);
    }
}
