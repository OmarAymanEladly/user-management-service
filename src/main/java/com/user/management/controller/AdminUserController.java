package com.user.management.controller;

import com.user.management.dto.request.AdminUserRequestDTO;
import com.user.management.dto.response.AdminUserResponseDTO;
import com.user.management.services.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
    public AdminUserResponseDTO updateUser(@PathVariable Long id, @Valid @RequestBody AdminUserRequestDTO request) {
        return adminUserService.updateUser(id, request);
    }

    @PatchMapping("/{id}/activate")
    public AdminUserResponseDTO activateUser(@PathVariable Long id) {
        return adminUserService.activateUser(id);
    }

    @PatchMapping("/{id}/deactivate")
    public AdminUserResponseDTO deactivateUser(@PathVariable Long id) {
        return adminUserService.deactivateUser(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        adminUserService.deleteUser(id);
    }
}
