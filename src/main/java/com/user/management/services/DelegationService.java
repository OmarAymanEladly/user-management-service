package com.user.management.services;

import com.user.management.dto.request.DelegationRequestDTO;
import com.user.management.dto.response.DelegationResponseDTO;

public interface DelegationService {

    DelegationResponseDTO createDelegation(DelegationRequestDTO request);

}
