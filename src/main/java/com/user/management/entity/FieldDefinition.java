package com.user.management.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldDefinition {
    private String fieldName;
    private String displayName;
    private String dataType;
    private boolean required;
    private Object defaultValue;
    private boolean syncToKeycloak;
    private Map<String, Object> validation;
}