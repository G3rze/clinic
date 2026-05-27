package com.terraplanistas.clinic.domain.mapper;

import com.terraplanistas.clinic.domain.dto.request.RoleRequest;
import com.terraplanistas.clinic.domain.dto.response.RoleResponse;
import com.terraplanistas.clinic.domain.entities.Role;

public class RoleMapper {

    public static Role toEntity(RoleRequest request) {
        Role role = new Role();
        role.setCode(request.code());
        role.setName(request.name());
        return role;
    }

    public static Role toUpgrade(RoleRequest request, Role role) {
        role.setCode(request.code());
        role.setName(request.name());
        return role;
    }

    public static RoleResponse toResponse(Role role) {
        return new RoleResponse(
            role.getId(),
            role.getCode(),
            role.getName()
        );
    }
}