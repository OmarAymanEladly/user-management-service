package com.user.management.services;

import com.user.management.dto.request.DelegationRequestDTO;
import com.user.management.dto.response.DelegationResponseDTO;

import java.util.UUID;

public interface DelegationService {

    DelegationResponseDTO createDelegation(DelegationRequestDTO request);
    DelegationResponseDTO revokeDelegation(UUID delegationId);

}
