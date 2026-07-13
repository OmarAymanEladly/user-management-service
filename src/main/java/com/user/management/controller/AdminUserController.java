package com.user.management.controller;

import com.user.management.dto.request.AdminUserRequestDTO;
import com.user.management.dto.response.AdminUserResponseDTO;
import com.user.management.services.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminUserResponseDTO createUser(@Valid @RequestBody AdminUserRequestDTO request) {
        return adminUserService.createUser(request);
    }

    @PutMapping("/{id}")
    public AdminUserResponseDTO updateUser(@PathVariable UUID id, @Valid @RequestBody AdminUserRequestDTO request) {
        return adminUserService.updateUser(id, request);
    }

    @PatchMapping("/{id}/activate")
    public AdminUserResponseDTO activateUser(@PathVariable UUID id) {
        return adminUserService.activateUser(id);
    }

    @PatchMapping("/{id}/deactivate")
    public AdminUserResponseDTO deactivateUser(@PathVariable UUID id) {
        return adminUserService.deactivateUser(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID id) {
        adminUserService.deleteUser(id);
    }

    @GetMapping
    public List<AdminUserResponseDTO> getUsers(){
        return adminUserService.getAllUsers();
    }

    @GetMapping("/{id}")
    public AdminUserResponseDTO getUserById(@PathVariable UUID id){
        return adminUserService.getUserById(id);
    }
}
