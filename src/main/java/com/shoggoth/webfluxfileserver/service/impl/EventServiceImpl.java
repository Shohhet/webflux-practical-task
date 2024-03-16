package com.shoggoth.webfluxfileserver.service.impl;

import com.shoggoth.webfluxfileserver.dto.EventDto;
import com.shoggoth.webfluxfileserver.entity.Status;
import com.shoggoth.webfluxfileserver.exception.ErrorCode;
import com.shoggoth.webfluxfileserver.exception.NotFoundException;
import com.shoggoth.webfluxfileserver.mapper.EventMapper;
import com.shoggoth.webfluxfileserver.repository.EventRepository;
import com.shoggoth.webfluxfileserver.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Override
    public Mono<EventDto> findEventById(Long id) {
        return eventRepository.findById(id)
                .filter(eventEntity -> eventEntity.getStatus().equals(Status.ACTIVE))
                .switchIfEmpty(Mono.error(
                                new NotFoundException(
                                        String.format("Event with id:%d not found", id),
                                        ErrorCode.FILE_SERVER_DATABASE_ERROR
                                )
                        )
                )
                .doOnError((throwable) -> log.warn("IN findEventById - event with id: {} - does not exist.", id))
                .map(eventMapper::map);
    }

    @Override
    public Flux<EventDto> findEventsByUserId(Long userId) {
        return eventRepository.findAllByUserId(userId)
                .filter(eventEntity -> eventEntity.getStatus().equals(Status.ACTIVE))
                .map(eventMapper::map);
    }

    @Override
    public Mono<Void> deleteEventById(Long id) {
        return eventRepository.findById(id)
                .filter(eventEntity -> eventEntity.getStatus().equals(Status.ACTIVE))
                .switchIfEmpty(Mono.error(
                                new NotFoundException(
                                        String.format("Event with id:%d not found", id),
                                        ErrorCode.FILE_SERVER_DATABASE_ERROR
                                )
                        )
                )
                .doOnError((throwable) -> log.warn("IN findEventById - event with id: {} - does not exist.", id))
                .map(eventEntity -> {
                    eventEntity.setStatus(Status.DELETED);
                    eventEntity.setUpdatedAt(LocalDateTime.now());
                    return eventEntity;
                })
                .flatMap(eventRepository::save)
                .then();
    }
}
