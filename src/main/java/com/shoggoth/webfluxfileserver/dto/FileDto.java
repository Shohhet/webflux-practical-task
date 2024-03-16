package com.shoggoth.webfluxfileserver.dto;

public record FileDto(
        Long id,
        String name,
        String path
) {
}
