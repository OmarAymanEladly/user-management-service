package com.user.management.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTypeRequestDTO {
    private String type;
    private String description;
    private String status;
    private List<UserTypeRequestFieldDTO> fields;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserTypeRequestFieldDTO {
        private String fieldName;
        private String displayName;
        private String dataType;
        private boolean required;
        private Map<String, Object> validation;
    }
}


