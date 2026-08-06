package com.user.management.controller;

import com.user.management.dto.request.AdminUserRequestDTO;
import com.user.management.dto.request.UserApprovalStatusRequestDTO;
import com.user.management.dto.response.AdminUserResponseDTO;
import com.user.management.services.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


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

    @GetMapping
    public List<AdminUserResponseDTO> getAllUsers(@RequestParam(required = false) String signupApprovalStatus) {

        return adminUserService.getAllUsers(signupApprovalStatus);
    }

    @GetMapping("/{id}")
    public AdminUserResponseDTO listUserById(@PathVariable UUID id) {

        return adminUserService.getUserById(id);
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

    @PatchMapping("/{id}/approve-signup")
    public AdminUserResponseDTO approveSignup(@PathVariable UUID id) {
        return adminUserService.approveSignup(id);
    }

    @PatchMapping("/{id}/reject-signup")
    public AdminUserResponseDTO rejectSignup(@PathVariable UUID id) {
        return adminUserService.rejectSignup(id);
    }

    @PatchMapping("/{id}/approval-status")
    public AdminUserResponseDTO updateApprovalStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UserApprovalStatusRequestDTO request) {
        return adminUserService.updateApprovalStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID id) {
        adminUserService.deleteUser(id);
    }



}
