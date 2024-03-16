package com.shoggoth.webfluxfileserver.rest;

import com.shoggoth.webfluxfileserver.dto.EventDto;
import com.shoggoth.webfluxfileserver.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventRestControllerV1 {
    private final EventService eventService;
    @GetMapping("/{id}")
    public Mono<EventDto> getEventById(@PathVariable Long id) {
        return eventService.findEventById(id);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteEventById(@PathVariable Long id) {
        return eventService.deleteEventById(id);
    }


}
