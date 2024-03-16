package com.shoggoth.webfluxfileserver.repository;

import com.shoggoth.webfluxfileserver.entity.FileEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;

public interface FileRepository extends R2dbcRepository<FileEntity, Long> {
    @Query("""
            SELECT file.id, file.name, file.path, file.created, file.updated, file.status
            FROM file INNER JOIN event ON file.id = event.file_id
            WHERE event.user_id = :id;
            """)
    Flux<FileEntity> findFileEntitiesByUserId(@Param("id") Long userId);
}

