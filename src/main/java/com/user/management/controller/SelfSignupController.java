package com.user.management.controller;

import com.user.management.dto.request.SelfSignupRequestDTO;
import com.user.management.dto.response.AdminUserResponseDTO;
import com.user.management.services.SelfSignupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/signup")
@RequiredArgsConstructor
public class SelfSignupController {

    private final SelfSignupService selfSignupService;

    @PostMapping("/open")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminUserResponseDTO signup(@Valid @RequestBody SelfSignupRequestDTO request) {
        return selfSignupService.signup(request);
    }

    @PostMapping("/approval-required")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminUserResponseDTO signupWithApproval(@Valid @RequestBody SelfSignupRequestDTO request) {
        return selfSignupService.signupWithApproval(request);
    }
}
