package com.user.management.services.impl;

import com.user.management.dto.request.AdminUserRequestDTO;
import com.user.management.dto.response.AdminUserResponseDTO;
import com.user.management.entity.FieldDefinition;
import com.user.management.entity.ManagedUser;
import com.user.management.entity.UserType;
import com.user.management.repository.ManagedUserRepository;
import com.user.management.repository.UserTypeRepository;
import com.user.management.services.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final ManagedUserRepository managedUserRepository;
    private final UserTypeRepository userTypeRepository;

    @Override
    public AdminUserResponseDTO createUser(AdminUserRequestDTO request) {
        UserType userType = getUserType(request.getUserTypeId());
        validateAttributes(userType, request.getAttributes());

        ManagedUser user = managedUserRepository.findByUsername(request.getUsername())
                .orElseGet(ManagedUser::new);

        applyRequest(user, request, userType);
        return toResponse(managedUserRepository.save(user));
    }

    @Override
    public AdminUserResponseDTO updateUser(Long id, AdminUserRequestDTO request) {
        ManagedUser user = getUser(id);
        UserType userType = getUserType(request.getUserTypeId());
        validateAttributes(userType, request.getAttributes());

        applyRequest(user, request, userType);
        return toResponse(managedUserRepository.save(user));
    }

    @Override
    public AdminUserResponseDTO activateUser(Long id) {
        ManagedUser user = getUser(id);
        user.setEnabled(true);
        return toResponse(managedUserRepository.save(user));
    }

    @Override
    public AdminUserResponseDTO deactivateUser(Long id) {
        ManagedUser user = getUser(id);
        user.setEnabled(false);
        return toResponse(managedUserRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        if (!managedUserRepository.existsById(id)) {
            throw new RuntimeException("User to delete doesn't exist");
        }
        managedUserRepository.deleteById(id);
    }

    private ManagedUser getUser(Long id) {
        return managedUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    private UserType getUserType(Long id) {
        return userTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User Type not found with id: " + id));
    }

    private void validateAttributes(UserType userType, Map<String, Object> attributes) {
        List<FieldDefinition> fields = userType.getFields();
        if (fields == null || fields.isEmpty()) {
            return;
        }

        for (FieldDefinition field : fields) {
            if (field.isRequired() && (attributes == null || !attributes.containsKey(field.getFieldName())
                    || attributes.get(field.getFieldName()) == null)) {
                throw new RuntimeException("Required attribute is missing: " + field.getFieldName());
            }
        }
    }

    private void applyRequest(ManagedUser user, AdminUserRequestDTO request, UserType userType) {
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setUserType(userType);
        user.setEnabled(request.getEnabled() == null || request.getEnabled());
        user.setAttributes(request.getAttributes());
    }

    private AdminUserResponseDTO toResponse(ManagedUser user) {
        return new AdminUserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getUserType().getId(),
                user.getEnabled(),
                user.getAttributes()
        );
    }
}
