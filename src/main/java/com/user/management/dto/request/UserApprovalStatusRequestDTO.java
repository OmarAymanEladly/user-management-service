package com.user.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserApprovalStatusRequestDTO {
    @NotBlank(message = "Status is required")
    private String status;

    private String rejectReason;
}
