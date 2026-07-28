package com.user.management.mapper;

import com.user.management.dto.request.UserTypeRequestDTO;
import com.user.management.dto.response.UserTypeResponseDTO;
import com.user.management.entity.FieldDefinition;
import com.user.management.entity.UserType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserTypeMapper {

    // 1. Convert Entity -> Response DTO
    public UserTypeResponseDTO toResponse(UserType entity) {
        if (entity == null) return null;

        return UserTypeResponseDTO.builder()
                .id(entity.getId())
                .type(entity.getType())
                .description(entity.getDescription())
                .roleName(entity.getRoleName())
                .status(entity.getStatus())
                .fields(mapFieldsToResponseDto(entity.getFields()))
                .build();
    }

    // 2. Convert Request DTO -> Entity
    public UserType toEntity(UserTypeRequestDTO request) {
        if (request == null) return null;

        return UserType.builder()
                .type(request.getType())
                .description(request.getDescription())
                .roleName(request.getRoleName())
                .status(request.getStatus())
                .fields(mapFieldsToEntity(request.getFields()))
                .build();
    }

    // --- Helper Methods for the internal List of Fields ---

    private List<UserTypeResponseDTO.UserTypeResponseFieldDTO> mapFieldsToResponseDto(List<FieldDefinition> fieldEntities) {
        if (fieldEntities == null) return new ArrayList<>();
        return fieldEntities.stream().map(field ->
                UserTypeResponseDTO.UserTypeResponseFieldDTO.builder()
                        .fieldName(field.getFieldName())
                        .displayName(field.getDisplayName())
                        .dataType(field.getDataType())
                        .required(field.isRequired())
                        .syncToKeycloak(field.isSyncToKeycloak())
                        .validation(field.getValidation())
                        .build()
        ).collect(Collectors.toList());
    }

    private List<FieldDefinition> mapFieldsToEntity(List<UserTypeRequestDTO.UserTypeRequestFieldDTO> fieldDtos) {
        if (fieldDtos == null) return new ArrayList<>();
        return fieldDtos.stream().map(dto -> {
            FieldDefinition field = new FieldDefinition();
            field.setFieldName(dto.getFieldName());
            field.setDisplayName(dto.getDisplayName());
            field.setDataType(dto.getDataType());
            field.setRequired(dto.isRequired());
            field.setSyncToKeycloak(dto.isSyncToKeycloak());
            field.setValidation(dto.getValidation());
            return field;
        }).collect(Collectors.toList());
    }
}
