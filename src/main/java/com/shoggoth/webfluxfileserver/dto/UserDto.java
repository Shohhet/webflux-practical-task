package com.shoggoth.webfluxfileserver.dto;

import com.shoggoth.webfluxfileserver.entity.Role;

public record UserDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String password,
        Role role
) {
}
