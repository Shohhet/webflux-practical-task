package com.shoggoth.webfluxfileserver.repository;

import com.shoggoth.webfluxfileserver.entity.EventEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EventRepository extends R2dbcRepository<EventEntity, Long> {
    Mono<Boolean> existsByFileIdAndUserId(Long fileId, Long UserId);
    Flux<EventEntity> findAllByUserId(Long userId);
}
