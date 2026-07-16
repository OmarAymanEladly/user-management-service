package com.user.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.management.dto.request.UserTypeRequestDTO;
import com.user.management.dto.response.UserTypeResponseDTO;
import com.user.management.services.UserTypeService;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserTypeControllerTest {

    @Mock
    private UserTypeService userTypeService;


    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @InjectMocks
    UserTypeController userTypeController;

    @BeforeEach
    void setup(){
        mockMvc = MockMvcBuilders.standaloneSetup(userTypeController).build();
    }


    @Test
    void create() throws Exception {
        UserTypeRequestDTO request = new UserTypeRequestDTO();

        request.setType("ADMIN");

        UserTypeResponseDTO response = new UserTypeResponseDTO();
        response.setType("ADMIN");

        when(userTypeService.createType(any(UserTypeRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/userTypes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void getAllTypes() throws Exception {

        UserTypeResponseDTO response = new UserTypeResponseDTO();
        response.setType("ADMIN");

        when(userTypeService.getAllTypes()).thenReturn(List.of(response));
        mockMvc.perform(get("/api/userTypes")).andExpect(status().isOk());
    }

    @Test
    void getTypeById() throws Exception {
        UUID testId = UUID.randomUUID();
        UserTypeResponseDTO response = new UserTypeResponseDTO();
        response.setId(testId);
        when(userTypeService.getTypeById(testId)).thenReturn(response);
        mockMvc.perform(get("/api/userTypes/{id}",testId))
                .andExpect(status().isOk());
    }

    @Test
    void updateType() throws Exception {
        UUID testId = UUID.randomUUID();

        UserTypeRequestDTO request = new UserTypeRequestDTO();
        request.setType("ADMIN");
        request.setDescription("New userType");
        request.setStatus("ACTIVE");

        UserTypeResponseDTO response = new UserTypeResponseDTO();
        response.setId(testId);
        response.setType("ADMIN");
        response.setDescription("New userType");
        response.setStatus("ACTIVE");

        when(userTypeService.updateType(eq(testId),any(UserTypeRequestDTO.class))).thenReturn(response);
        mockMvc.perform(put("/api/userTypes/{id}",testId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());


    }

    @Test
    void deactivateType() throws Exception {
        UUID testId = UUID.randomUUID();
        UserTypeResponseDTO response = new UserTypeResponseDTO();
        response.setId(testId);
        response.setStatus("INACTIVE");

        when(userTypeService.deactivateType(testId)).thenReturn(response);
        mockMvc.perform(patch("/api/userTypes/{id}/deactivate",testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));

    }

    @Test
    void deleteType() throws Exception{
        UUID testId = UUID.randomUUID();
        mockMvc.perform(delete("/api/userTypes/{id}",testId))
                .andExpect(status().isNoContent());

    }
}