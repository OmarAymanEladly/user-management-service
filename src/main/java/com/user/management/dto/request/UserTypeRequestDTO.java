package com.user.management.dto.request;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class UserTypeRequestDTO {
    private String type;
    private String description;
    private String status;
    private List<FieldDTO> fields;

    @Data
    public static class FieldDTO {
        private String fieldName;
        private String displayName;
        private String dataType;
        private boolean required;
        private Map<String, Object> validation;
    }
}


