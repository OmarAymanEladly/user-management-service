package com.user.management.services.impl;

import com.user.management.dto.request.UserTypeRequestDTO;
import com.user.management.dto.response.UserTypeResponseDTO;
import com.user.management.entity.UserType;
import com.user.management.mapper.UserTypeMapper;
import com.user.management.repository.UserTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserTypeServiceImplTest {

    @Mock
    private UserTypeRepository repository;

    @Mock
    private UserTypeMapper mapper;

    @InjectMocks
    private UserTypeServiceImpl userTypeService;


    @Test
    void createUserType(){
        UUID testId = UUID.randomUUID();

        UserTypeRequestDTO request = new UserTypeRequestDTO();
        request.setType("ADMIN");
        request.setStatus("ACTIVE");
        request.setDescription(" New User Type");

        UserType userType =  UserType.builder()
                .type("ADMIN")
                .status("ACTIVE")
                .description("New User Type").build();

        UserTypeResponseDTO response = new UserTypeResponseDTO();
        response.setId(testId);
        response.setType("ADMIN");
        response.setStatus("ACTIVE");
        response.setDescription("New User Type");

        when(repository.findByType(request.getType().toLowerCase())).thenReturn(Optional.empty());
        when(mapper.toEntity(request)).thenReturn(userType);
        when(mapper.toResponse(userType)).thenReturn(response);
        when(repository.save(userType)).thenReturn(userType);

        UserTypeResponseDTO result = userTypeService.createType(request);

        assertNotNull(result);
        assertEquals("ADMIN",result.getType());
        verify(repository).save(userType);

    }

    @Test
    void getAllTypes(){
        UserType adminType = UserType.builder()
                .type("ADMIN")
                .status("ACTIVE")
                .description("New Admin Type").build();

        UserType userType = UserType.builder()
                .type("USER")
                .status("ACTIVE")
                .description("New User Type")
                .build();

        UserTypeResponseDTO adminDTO = new UserTypeResponseDTO();
        adminDTO.setType("ADMIN");
        adminDTO.setStatus("ACTIVE");
        adminDTO.setDescription("New Admin Type");

        UserTypeResponseDTO userDTO = new UserTypeResponseDTO();
        userDTO.setType("USER");
        userDTO.setStatus("ACTIVE");
        userDTO.setDescription("New User Type");

        when(repository.findAll()).thenReturn(List.of(adminType,userType));
        when(mapper.toResponse(adminType)).thenReturn(adminDTO);
        when(mapper.toResponse(userType)).thenReturn(userDTO);

        List<UserTypeResponseDTO> responses = userTypeService.getAllTypes();
        assertNotNull(responses);
        assertEquals(List.of(adminDTO,userDTO),responses);

        verify(repository).findAll();
        verify(mapper).toResponse(userType);
        verify(mapper).toResponse(adminType);


    }

    @Test
    void getTypeById(){
        UUID testId = UUID.randomUUID();

        UserType userType = UserType.builder()
                .id(testId)
                .type("ADMIN")
                .status("ACTIVE")
                .description("New User Type")
                .build();
        UserTypeResponseDTO response = new UserTypeResponseDTO();
        response.setId(testId);
        response.setType("ADMIN");
        response.setStatus("ACTIVE");
        response.setDescription("New User Type");

        when(repository.findById(testId)).thenReturn(Optional.of(userType));
        when(mapper.toResponse(userType)).thenReturn(response);

        UserTypeResponseDTO result = userTypeService.getTypeById(testId);

        assertNotNull(result);
        assertEquals(response,result);

        verify(repository).findById(testId);
        verify(mapper).toResponse(userType);
    }


    @Test
    void updateType(){
        UUID testId = UUID.randomUUID();


        UserType existingType = UserType.builder()
                .id(testId)
                .type("ADMIN")
                .description("New User Type")
                .build();

        UserType updatedType = UserType.builder()
                .id(testId)
                .type("UPDATED ADMIN")
                .description("UPDATED USER TYPE")
                .build();


        UserTypeRequestDTO request = new UserTypeRequestDTO();
        request.setType("UPDATED ADMIN");
        request.setDescription("UPDATED USER TYPE");

        UserTypeResponseDTO response = new UserTypeResponseDTO();
        response.setId(testId);
        response.setType("UPDATED ADMIN");
        response.setDescription("New User Type");

        when(repository.findById(testId)).thenReturn(Optional.of(existingType));
        when(mapper.toEntity(request)).thenReturn(updatedType);
        when(repository.save(existingType)).thenReturn(existingType);
        when(mapper.toResponse(updatedType)).thenReturn(response);

        UserTypeResponseDTO result = userTypeService.updateType(testId,request);

        assertNotNull(result);
        assertEquals(response,result);

        verify(repository).findById(testId);
        verify(mapper).toEntity(request);
        verify(mapper).toResponse(updatedType);
        verify(repository).save(existingType);

    }

    @Test
    void deleteType() {
        UUID testId = UUID.randomUUID();

        when(repository.existsById(testId)).thenReturn(true);
        userTypeService.deleteType(testId);

        verify(repository).existsById(testId);
        verify(repository).deleteById(testId);

    }

    @Test
    void deleteType_should_throw_Exception(){
        UUID testId = UUID.randomUUID();

        when(repository.existsById(testId)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                ()->{
            userTypeService.deleteType(testId);
                });

        assertEquals("User Type to delete doesn't exist", exception.getMessage());
        verify(repository,never()).deleteById(any());

    }



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
