package com.shoggoth.webfluxfileserver.service.impl;

import com.shoggoth.webfluxfileserver.dto.UserDto;
import com.shoggoth.webfluxfileserver.entity.Role;
import com.shoggoth.webfluxfileserver.entity.Status;
import com.shoggoth.webfluxfileserver.exception.AlreadyExistException;
import com.shoggoth.webfluxfileserver.exception.ErrorCode;
import com.shoggoth.webfluxfileserver.exception.NotFoundException;
import com.shoggoth.webfluxfileserver.mapper.UserMapper;
import com.shoggoth.webfluxfileserver.repository.UserRepository;
import com.shoggoth.webfluxfileserver.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public Mono<UserDto> createUser(UserDto userDto) {
        return userRepository.findUserEntityByEmail(userDto.email())
                .flatMap(userEntity -> {
                    if (userEntity.getStatus().equals(Status.ACTIVE)) {
                        log.warn("IN createUser - user with email: {} already exists", userEntity.getEmail());
                        return Mono.error(
                                new AlreadyExistException(
                                        String.format("User with email %s already exists.", userEntity.getEmail()),
                                        ErrorCode.FILE_SERVER_DATABASE_ERROR
                                )
                        );
                    } else {
                        var newUserEntity = userMapper.map(userDto);
                        newUserEntity.setId(userEntity.getId());
                        newUserEntity.setUpdatedAt(LocalDateTime.now());
                        return userRepository.save(newUserEntity)
                                .doOnSuccess(userEntity1 -> log.info("IN createUser - user: {} created with existing id.", userEntity1));
                    }
                })
                .switchIfEmpty(Mono.just(userMapper.map(userDto)))
                .flatMap(userEntity -> {
                    userEntity.setRole(Role.USER);
                    userEntity.setStatus(Status.ACTIVE);
                    userEntity.setCreatedAt(LocalDateTime.now());
                    userEntity.setUpdatedAt(LocalDateTime.now());
                    return userRepository.save(userEntity);
                })
                .doOnSuccess(userEntity -> log.info("IN createUser - user: {} created with new id.", userEntity))
                .map(userMapper::map);

    }

    @Override
    public Mono<UserDto> findUserById(Long id) {
        return userRepository.findById(id)
                .filter(userEntity -> userEntity.getStatus().equals(Status.ACTIVE))
                .map(userMapper::map)
                .switchIfEmpty(Mono.error(
                                new NotFoundException(
                                        String.format("User with id: %d not found.", id),
                                        ErrorCode.FILE_SERVER_DATABASE_ERROR
                                )
                        )
                )
                .doOnError((throwable) -> log.warn("IN findUserById - user with id: {} - does not exist.", id));
    }

    @Override
    public Mono<UserDto> getMyself() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getName)
                .flatMap(userRepository::findUserEntityByEmail)
                .map(userMapper::map);
    }

    @Override
    public Flux<UserDto> findAllUsers() {
        return userRepository.findAll()
                .filter(userEntity -> userEntity.getStatus().equals(Status.ACTIVE))
                .map(userMapper::map);
    }

    @Override
    public Mono<UserDto> updateUser(UserDto userDto) {
        return userRepository.findById(userDto.id())
                .filter(userEntity -> userEntity.getStatus().equals(Status.ACTIVE))
                .switchIfEmpty(Mono.error(
                                new NotFoundException(
                                        String.format("User with id: %d not found.", userDto.id()),
                                        ErrorCode.FILE_SERVER_DATABASE_ERROR
                                )
                        )
                )
                .doOnError((throwable) -> log.warn("IN findUserById - user with id: {} - does not exist.", userDto.id()))
                .map(userEntity -> {
                    userEntity.setEmail(userDto.email());
                    userEntity.setPassword(userDto.password());
                    userEntity.setFirstName(userDto.firstName());
                    userEntity.setLastName(userDto.lastName());
                    userEntity.setUpdatedAt(LocalDateTime.now());
                    return userEntity;
                })
                .flatMap(userRepository::save)
                .map(userMapper::map);
    }

    @Override
    public Mono<Void> deleteUserById(Long id) {
        return userRepository.findById(id)
                .filter(userEntity -> userEntity.getStatus().equals(Status.ACTIVE))
                .map(userEntity -> {
                    userEntity.setStatus(Status.DELETED);
                    userEntity.setUpdatedAt(LocalDateTime.now());
                    return userEntity;
                })
                .flatMap(userRepository::save)
                .then();

    }
}
