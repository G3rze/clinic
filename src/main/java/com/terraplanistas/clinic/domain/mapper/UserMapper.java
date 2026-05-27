package com.terraplanistas.clinic.domain.mapper;

import com.terraplanistas.clinic.domain.dto.request.UserRequest;
import com.terraplanistas.clinic.domain.dto.response.UserResponse;
import com.terraplanistas.clinic.domain.entities.User;
import com.terraplanistas.clinic.domain.entities.Role;

public class UserMapper {

    public static User toEntity(UserRequest request, Role role) {
        User user = new User();
        user.setEmail(request.email());
        user.setUsername(request.username());
        user.setRole(role);
        return user;
    }

    public static User toUpgrade(UserRequest request, User user) {
        user.setEmail(request.email());
        user.setUsername(request.username());
        return user;
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getUsername(),
            user.getRole() != null ? user.getRole().getId() : null
        );
    }
}