package com.shoggoth.webfluxfileserver.service.impl;

import com.shoggoth.webfluxfileserver.dto.UserDto;
import com.shoggoth.webfluxfileserver.entity.Role;
import com.shoggoth.webfluxfileserver.entity.Status;
import com.shoggoth.webfluxfileserver.entity.UserEntity;
import com.shoggoth.webfluxfileserver.exception.AlreadyExistException;
import com.shoggoth.webfluxfileserver.exception.NotFoundException;
import com.shoggoth.webfluxfileserver.mapper.UserMapper;
import com.shoggoth.webfluxfileserver.mapper.UserMapperImpl_;
import com.shoggoth.webfluxfileserver.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class UserServiceTest {
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;
    @Spy
    private UserMapper userMapper = new UserMapperImpl_();
    private static final Long USER_ID = 1L;
    private static final String FIRST_NAME = "Ivan";
    private static final String LAST_NAME = "Ivanov";
    private static final String EMAIL = "aa@mail.com";
    private static final String PASSWORD = "123";
    private static final LocalDateTime CREATED_AT = LocalDateTime.now();
    private static final LocalDateTime UPDATED_AT = CREATED_AT;

    private static UserEntity activeUser;
    private static UserEntity deletedUser;
    private static UserDto createUserDto;
    private static UserDto getUserDto;

    @BeforeEach
    public void init() {
        activeUser = UserEntity.builder()
                .id(USER_ID)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .password(PASSWORD)
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();

        deletedUser = UserEntity.builder()
                .id(USER_ID)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .password(PASSWORD)
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .role(Role.USER)
                .status(Status.DELETED)
                .build();


        createUserDto = new UserDto(
                null,
                FIRST_NAME,
                LAST_NAME,
                EMAIL,
                PASSWORD,
                null
        );
        getUserDto = new UserDto(
                USER_ID,
                FIRST_NAME,
                LAST_NAME,
                EMAIL,
                PASSWORD,
                Role.USER
        );
    }

    @Test
    public void WhenCreatedUserDoesNotExist_ThenCreateNewUser() {
//        Given
        when(userRepository.save(any(UserEntity.class))).thenReturn(Mono.just(activeUser));
        when(userRepository.findUserEntityByEmail(EMAIL)).thenReturn(Mono.empty());
//        When
        StepVerifier
                .create(userService.createUser(createUserDto))
                .consumeNextWith(userDto -> {
//        Then
                    assertEquals(userDto, getUserDto);
                })
                .verifyComplete();
    }

    @Test
    public void WhenCreatedUserExistsAndStatusActive_ThenThrowAlreadyExistException() {
//          Given
        when(userRepository.findUserEntityByEmail(EMAIL)).thenReturn(Mono.just(activeUser));
//          When
        StepVerifier
                .create(userService.createUser(createUserDto))
//          Then
                .expectError(AlreadyExistException.class)
                .verify();
    }

    @Test
    public void WhenCreatedUserExistsAndStatusDeleted_ThenUpdateExisting() {
//          Given
        when(userRepository.save(any(UserEntity.class))).thenReturn(Mono.just(activeUser));
        when(userRepository.findUserEntityByEmail(EMAIL)).thenReturn(Mono.just(deletedUser));
//          When
        StepVerifier
                .create(userService.createUser(createUserDto))
                .consumeNextWith(userDto -> {
//          Then
                    assertEquals(userDto, getUserDto);
                })
                .verifyComplete();

    }

    @Test
    public void WhenSearchingUserExistsAndActive_ThenReturnUser() {
//          Given
        when(userRepository.findById(USER_ID)).thenReturn(Mono.just(activeUser));
//          When
        StepVerifier
                .create(userService.findUserById(USER_ID))
                .consumeNextWith(userDto -> {
//          Then
                    assertEquals(userDto, getUserDto);
                })
                .verifyComplete();


    }

    @Test
    public void WhenSearchingUserExistsAndDeleted_ThenThrowNotFoundException() {
//          Given
        when(userRepository.findById(USER_ID)).thenReturn(Mono.just(deletedUser));
//          When
        StepVerifier
                .create(userService.findUserById(USER_ID))
//          Then
                .expectError(NotFoundException.class)
                .verify();


    }

    @Test
    public void WhenSearchingUserDoesNotExist_ThenThrowNotFoundException() {
//          Given
        when(userRepository.findById(USER_ID)).thenReturn(Mono.empty());
//          When
        StepVerifier
                .create(userService.findUserById(USER_ID))
//          Then
                .expectError(NotFoundException.class)
                .verify();


    }

}