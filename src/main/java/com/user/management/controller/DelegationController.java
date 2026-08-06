package com.user.management.controller;

import com.user.management.dto.request.DelegationRequestDTO;
import com.user.management.dto.response.DelegationResponseDTO;
import com.user.management.services.DelegationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/delegations")
@RequiredArgsConstructor
public class DelegationController {
    private final DelegationService delegationService;

    @PostMapping
    public DelegationResponseDTO create(@Valid @RequestBody DelegationRequestDTO request){
       return delegationService.createDelegation(request);
    }

    @PatchMapping("/{id}/revoke")
    public DelegationResponseDTO revoke(@PathVariable UUID id) {
        return delegationService.revokeDelegation(id);
    }
}
