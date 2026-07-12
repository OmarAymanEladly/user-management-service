package com.user.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserTypeResponseDTO {
    private UUID id;
    private String type;
    private String description;
    private String status;
    private List<UserTypeResponseFieldDTO> fields;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserTypeResponseFieldDTO {
        private String fieldName;
        private String displayName;
        private String dataType;
        private boolean required;
        private Map<String, Object> validation;
    }
}
