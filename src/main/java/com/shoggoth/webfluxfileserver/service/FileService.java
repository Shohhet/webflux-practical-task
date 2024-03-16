package com.shoggoth.webfluxfileserver.service;

import com.shoggoth.webfluxfileserver.dto.FileDto;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FileService {
    @PreAuthorize("hasAuthority('USER')")
    Mono<FileDto> addFile(FilePart filePart);

    @PreAuthorize("hasAuthority('USER')")
    Mono<FileDto> getFileById(Long id);

    @PreAuthorize("hasAuthority('USER')")
    Flux<FileDto> getFilesForAuthenticatedUser();

    Mono<FileDto> updateFile(FileDto fileDto);

    @PreAuthorize("hasAuthority('MODERATOR')")
    Mono<Void> deleteFile(Long id);


}
