package com.user.management.controller;


import com.user.management.dto.request.UserTypeRequestDTO;
import com.user.management.dto.response.UserTypeResponseDTO;
<<<<<<< HEAD
import com.user.management.model.entity.UserType;
import com.user.management.repository.UserTypeRepository;
=======
>>>>>>> 9082cad52326dd5df1a5ed89e2b8aec65a0d54c2
import com.user.management.services.UserTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/userTypes")
@RequiredArgsConstructor
public class UserTypeController {

    private final UserTypeService userTypeService;
<<<<<<< HEAD
    private final UserTypeRepository userTypeRepository;
=======
>>>>>>> 9082cad52326dd5df1a5ed89e2b8aec65a0d54c2

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserTypeResponseDTO create(@Valid @RequestBody UserTypeRequestDTO request){
        return userTypeService.createType(request);
    }

    @GetMapping
    public List<UserTypeResponseDTO> getAllTypes(){

        return userTypeService.getAllTypes();
    }

    @GetMapping("/roles")
    public List<String> getAvailableRoles(){

        return userTypeService.getAvailableRoles();
    }

    @GetMapping("/{id}")
    public UserTypeResponseDTO getTypeById(@PathVariable UUID id){

        return userTypeService.getTypeById(id);
    }

    @PutMapping("/{id}")
    public UserTypeResponseDTO updateType(@PathVariable UUID id, @Valid @RequestBody UserTypeRequestDTO request){
        return userTypeService.updateType(id,request);
    }

    @PatchMapping("/{id}/deactivate")
    public UserTypeResponseDTO deactivateType(@PathVariable UUID id){
        return userTypeService.deactivateType(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteType(@PathVariable UUID id){

        userTypeService.deleteType(id);
    }

<<<<<<< HEAD
    @GetMapping("/public/user-types")
    @CrossOrigin(origins = "http://localhost:8081")
    public List<String> getPublicUserTypes() {
        return userTypeRepository.findAll().stream()
                .map(UserType::getType)
                .toList();
    }

=======
>>>>>>> 9082cad52326dd5df1a5ed89e2b8aec65a0d54c2
}
