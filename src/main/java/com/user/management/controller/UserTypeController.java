package com.user.management.controller;


import com.user.management.dto.request.UserTypeRequestDTO;
import com.user.management.dto.response.UserTypeResponseDTO;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserTypeResponseDTO create(@Valid @RequestBody UserTypeRequestDTO request){
        return userTypeService.createType(request);
    }

    @GetMapping
    public List<UserTypeResponseDTO> getAllTypes(){
        return userTypeService.getAllTypes();
    }

    @GetMapping("/{id}")
    public UserTypeResponseDTO getTypeById(@PathVariable UUID id){
        return userTypeService.getTypeById(id);
    }

    @PutMapping("/{id}")
    public UserTypeResponseDTO updateType(@PathVariable UUID id, @Valid @RequestBody UserTypeRequestDTO request){
        return userTypeService.updateType(id,request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteType(@PathVariable UUID id){
       userTypeService.deleteType(id);
    }

}
