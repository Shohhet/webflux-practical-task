package com.shoggoth.webfluxfileserver.service;

import com.shoggoth.webfluxfileserver.dto.UserDto;
import org.springframework.security.access.prepost.PreAuthorize;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<UserDto> createUser(UserDto userDto);

    @PreAuthorize("hasAuthority('MODERATOR')")
    Mono<UserDto> findUserById(Long id);

    @PreAuthorize("hasAuthority('USER')")
    Mono<UserDto> getMyself();

    @PreAuthorize("hasAuthority('MODERATOR')")
    Flux<UserDto> findAllUsers();

    @PreAuthorize("hasAuthority('ADMIN')")
    Mono<UserDto> updateUser(UserDto userDto);

    @PreAuthorize("hasAuthority('ADMIN')")
    Mono<Void> deleteUserById(Long id);
}
