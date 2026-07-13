package com.user.management.services.impl;

import com.user.management.dto.request.UserTypeRequestDTO;
import com.user.management.dto.response.UserTypeResponseDTO;
import com.user.management.repository.UserTypeRepository;
import com.user.management.services.UserTypeService;
import com.user.management.entity.UserType;
import com.user.management.mapper.UserTypeMapper;
import lombok.*;
import org.springframework.stereotype.*;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserTypeServiceImpl implements UserTypeService {

    private final UserTypeRepository repository;
    private final UserTypeMapper mapper;

    @Override
    public UserTypeResponseDTO createType(UserTypeRequestDTO request){

        if(repository.findByType(request.getType().toLowerCase()).isPresent()){
            throw new RuntimeException("User Type: "+request.getType()+" already exist");
        }

        UserType entity = mapper.toEntity(request);

        UserType saved = repository.save(entity);

        return mapper.toResponse(saved);
    }


    @Override
    public List<UserTypeResponseDTO> getAllTypes(){
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserTypeResponseDTO getTypeById(UUID id){
        UserType entity = repository.findById(id).
                orElseThrow(()->new RuntimeException("User Type not found with ID: "+ id));

        return mapper.toResponse(entity);
    }

    @Override
    public UserTypeResponseDTO updateType(UUID id, UserTypeRequestDTO request){
        UserType existingEntity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User Type not found with id: " + id));
        UserType updatedData = mapper.toEntity(request);

        existingEntity.setDescription(updatedData.getDescription());
        existingEntity.setType(updatedData.getType());
        existingEntity.setFields(updatedData.getFields());
        existingEntity.setStatus(updatedData.getStatus());

        return  mapper.toResponse(repository.save(existingEntity));
    }

    @Override
    public void deleteType(UUID id){

        if(!repository.existsById(id)){
            throw new RuntimeException("User Type to delete doesn't exist");
        }
        repository.deleteById(id);
    }

}
