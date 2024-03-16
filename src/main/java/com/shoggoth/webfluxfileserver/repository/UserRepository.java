package com.shoggoth.webfluxfileserver.repository;

import com.shoggoth.webfluxfileserver.entity.UserEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends R2dbcRepository<UserEntity, Long> {
    Mono<UserEntity> findUserEntityByEmail(String email);
}
