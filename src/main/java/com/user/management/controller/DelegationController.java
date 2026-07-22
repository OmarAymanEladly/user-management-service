package com.user.management.controller;

import com.user.management.dto.request.DelegationRequestDTO;
import com.user.management.dto.response.DelegationResponseDTO;
import com.user.management.services.DelegationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/delegations")
@RequiredArgsConstructor
public class DelegationController {
    private final DelegationService delegationService;

    @PostMapping
    public DelegationResponseDTO create(@Valid @RequestBody DelegationRequestDTO request){
       return delegationService.createDelegation(request);
    }
}
