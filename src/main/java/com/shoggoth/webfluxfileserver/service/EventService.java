package com.shoggoth.webfluxfileserver.service;

import com.shoggoth.webfluxfileserver.dto.EventDto;
import org.springframework.security.access.prepost.PreAuthorize;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EventService {
    @PreAuthorize("hasAuthority('MODERATOR')")
    Mono<EventDto> findEventById(Long id);
    @PreAuthorize("hasAuthority('MODERATOR')")
    Flux<EventDto> findEventsByUserId(Long userId);
    @PreAuthorize("hasAuthority('ADMIN')")
    Mono<Void> deleteEventById(Long id);
}
