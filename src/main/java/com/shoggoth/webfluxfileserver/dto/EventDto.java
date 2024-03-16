package com.shoggoth.webfluxfileserver.dto;

public record EventDto(
        Long id,
        UserDto user,
        FileDto file
) {
}
