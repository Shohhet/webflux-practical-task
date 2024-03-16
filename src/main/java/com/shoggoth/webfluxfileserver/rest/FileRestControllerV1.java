package com.shoggoth.webfluxfileserver.rest;

import com.shoggoth.webfluxfileserver.dto.FileDto;
import com.shoggoth.webfluxfileserver.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/files")
@RequiredArgsConstructor
public class FileRestControllerV1 {
    private final FileService fileService;

    @PostMapping("/")
    public Mono<FileDto> uploadFile(@RequestPart("file-data") Mono<FilePart> filePart) {
        return filePart
                .flatMap(fileService::addFile);
    }

    @GetMapping("/{id}")
    public Mono<FileDto> getFileById(@PathVariable Long id) {
        return fileService.getFileById(id);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteFileById(@PathVariable Long id) {
        return fileService.deleteFile(id);
    }
}
