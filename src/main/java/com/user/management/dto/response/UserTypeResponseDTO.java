package com.user.management.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;


@Data
@Builder
public class UserTypeResponseDTO {
    private Long id;
    private String type;
    private String description;
    private String status;
    private List<FieldDTO> fields;

    @Data
    @Builder
    public static class FieldDTO {
        private String fieldName;
        private String displayName;
        private String dataType;
        private boolean required;
        private Map<String, Object> validation;
    }
}
