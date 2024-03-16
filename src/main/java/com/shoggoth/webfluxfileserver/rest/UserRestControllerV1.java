package com.shoggoth.webfluxfileserver.rest;

import com.shoggoth.webfluxfileserver.dto.EventDto;
import com.shoggoth.webfluxfileserver.dto.FileDto;
import com.shoggoth.webfluxfileserver.dto.UserDto;
import com.shoggoth.webfluxfileserver.service.EventService;
import com.shoggoth.webfluxfileserver.service.FileService;
import com.shoggoth.webfluxfileserver.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserRestControllerV1 {
    private final UserService userService;
    private final FileService fileService;
    private final EventService eventService;
    @PostMapping("/register")
    public Mono<UserDto> registerUser(@RequestBody UserDto userDto) {
        return userService.createUser(userDto);
    }

    @GetMapping("/{id}")
    public Mono<UserDto> getUserById(@PathVariable Long id) {
        return userService.findUserById(id);
    }

    @GetMapping("/")
    public Flux<UserDto> getAllUsers() {
        return userService.findAllUsers();
    }
    @GetMapping("/me")
    public Mono<UserDto> getMyself() {
        return userService.getMyself();
    }

    @GetMapping("/me/files")
    public Flux<FileDto> getAllFilesByAuthenticatedUser() {
        return fileService.getFilesForAuthenticatedUser();
    }

    @GetMapping("/{id}/events")
    public Flux<EventDto> getAllEventsByUserId(@PathVariable Long id) {
        return eventService.findEventsByUserId(id);
    }
    @PutMapping("/")
    public Mono<UserDto> updateUser(@RequestBody UserDto userDto) {
       return userService.updateUser(userDto);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteUser(@PathVariable Long id) {
        return userService.deleteUserById(id);
    }
}
