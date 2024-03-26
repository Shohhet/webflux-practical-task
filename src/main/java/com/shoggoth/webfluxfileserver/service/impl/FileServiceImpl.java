package com.shoggoth.webfluxfileserver.service.impl;

import com.shoggoth.webfluxfileserver.configuration.AwsS3Properties;
import com.shoggoth.webfluxfileserver.dto.FileDto;
import com.shoggoth.webfluxfileserver.entity.*;
import com.shoggoth.webfluxfileserver.exception.*;
import com.shoggoth.webfluxfileserver.mapper.FileMapper;
import com.shoggoth.webfluxfileserver.repository.EventRepository;
import com.shoggoth.webfluxfileserver.repository.FileRepository;
import com.shoggoth.webfluxfileserver.repository.UserRepository;
import com.shoggoth.webfluxfileserver.service.FileService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.SdkResponse;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private final S3AsyncClient s3AsyncClient;
    private final AwsS3Properties awsS3Properties;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final FileMapper fileMapper;


    @Override
    @Transactional
    public Mono<FileDto> addFile(FilePart filePart) {

        return uploadFile(filePart)
                .flatMap(fileRepository::save)
                .switchIfEmpty(Mono.error(new FileStorageException("Unable to upload file.", ErrorCode.FILE_SERVER_STORAGE_ERROR)))
                .zipWith(getCurrentUser(), ((fileEntity, userEntity) -> EventEntity.builder()
                        .file(fileEntity)
                        .user(userEntity)
                        .fileId(fileEntity.getId())
                        .userId(userEntity.getId())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .status(Status.ACTIVE)
                        .build()))
                .flatMap(eventRepository::save)
                .map(EventEntity::getFile)
                .map(fileMapper::map);
    }

    @Override
    public Mono<FileDto> getFileById(Long id) {
        return getCurrentUser()
                .flatMap(userEntity -> {
                    if (userEntity.getRole().equals(Role.USER)) {
                        return eventRepository.existsByFileIdAndUserId(id, userEntity.getId());
                    }
                    return Mono.just(Boolean.TRUE);
                })
                .flatMap(value -> {
                    if (value) {
                        return fileRepository.findById(id);
                    } else {
                        return Mono.error(
                                new AuthorizationException(
                                        "File not accessible for user",
                                        ErrorCode.FILE_SERVER_AUTHORIZATION_ERROR
                                )
                        );
                    }
                })
                .filter(fileEntity -> fileEntity.getStatus().equals(Status.ACTIVE))
                .switchIfEmpty(Mono.error(
                                new NotFoundException(
                                        String.format("File with id: %d not found.", id),
                                        ErrorCode.FILE_SERVER_DATABASE_ERROR
                                )
                        )
                )
                .map(fileMapper::map);

    }


    @Override
    public Flux<FileDto> getFilesForAuthenticatedUser() {

        return getCurrentUser()
                .flatMapMany(userEntity -> fileRepository.findFileEntitiesByUserId(userEntity.getId()))
                .map(fileMapper::map);
    }


    @Override
    public Mono<Void> deleteFile(Long id) {
        return fileRepository.findById(id)
                .filter(fileEntity -> fileEntity.getStatus().equals(Status.ACTIVE))
                .map(fileEntity -> {
                    fileEntity.setStatus(Status.DELETED);
                    fileEntity.setUpdatedAt(LocalDateTime.now());
                    return fileEntity;
                })
                .flatMap(fileRepository::save)
                .then();
    }

    private Mono<UserEntity> getCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getName)
                .flatMap(userRepository::findUserEntityByEmail)
                .switchIfEmpty(Mono.error(
                                new AuthenticationException("Authenticated user required", ErrorCode.FILE_SERVER_AUTHENTICATION_ERROR)
                        )
                );
    }

    private Mono<CreateMultipartUploadResponse> createMultipartUploadResponseMono(FilePart filePart, UserEntity userEntity) {
        String fileName = setFileNamePrefix(userEntity, filePart);
        MediaType mediaType = getContentType(filePart);
        Map<String, String> metadata = Map.of("filename", fileName);
        CompletableFuture<CreateMultipartUploadResponse> uploadRequest =
                s3AsyncClient.createMultipartUpload(
                        CreateMultipartUploadRequest.builder()
                                .contentType(mediaType.getType())
                                .metadata(metadata)
                                .bucket(awsS3Properties.getS3BucketName())
                                .key(fileName)
                                .build());
        return Mono.fromFuture(uploadRequest);
    }

    private String setFileNamePrefix(UserEntity userEntity, FilePart filePart) {
        return userEntity.getId() + "/" + filePart.filename();
    }

    private MediaType getContentType(FilePart filePart) {
        return ObjectUtils.defaultIfNull(filePart.headers().getContentType(), MediaType.APPLICATION_OCTET_STREAM);
    }

    private Mono<FileEntity> uploadFile(FilePart filePart) {

        UploadState uploadState = new UploadState();
        return getCurrentUser()
                .flatMap(userEntity -> {
                    uploadState.setFileName(setFileNamePrefix(userEntity, filePart));
                    uploadState.setContentType(getContentType(filePart).getType());
                    return createMultipartUploadResponseMono(filePart, userEntity);
                })
                .flatMapMany(response -> {
                    uploadResponseCheck(response);
                    uploadState.setUploadId(response.uploadId());
                    return filePart.content();
                })
                .bufferUntil(dataBuffer -> {
                    uploadState.addBuffered(dataBuffer.readableByteCount());
                    if (uploadState.getBuffered() >= awsS3Properties.getMultipartMinPartSize()) {
                        uploadState.setBuffered(0);
                        return true;
                    }
                    return false;
                })
                .map(this::concatenateBuffers)
                .flatMap(byteBuffer ->
                        uploadFilePart(uploadState, byteBuffer)
                )
                .onBackpressureBuffer()
                .reduce(uploadState, (state, completedPart) -> {
                    state.getCompletedParts().put(completedPart.partNumber(), completedPart);
                    return state;
                })
                .flatMap(this::completeUpload)
                .map(response -> {
                    uploadResponseCheck(response);
                    return FileEntity.builder()
                            .name(response.key())
                            .path(response.location())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .status(Status.ACTIVE)
                            .build();
                });
    }

    private Mono<CompletedPart> uploadFilePart(UploadState uploadState, ByteBuffer byteBuffer) {
        final int partNumber = uploadState.getIncPartCounter();
        CompletableFuture<UploadPartResponse> uploadPartResponseCompletableFuture =
                s3AsyncClient.uploadPart(
                        UploadPartRequest.builder()
                                .bucket(awsS3Properties.getS3BucketName())
                                .key(uploadState.getFileName())
                                .partNumber(partNumber)
                                .uploadId(uploadState.getUploadId())
                                .contentLength((long) byteBuffer.capacity())
                                .build(),
                        AsyncRequestBody.fromPublisher(Mono.just(byteBuffer))
                );

        return Mono.fromFuture(uploadPartResponseCompletableFuture)
                .map(response -> {
                            uploadResponseCheck(response);
                            return CompletedPart.builder()
                                    .eTag(response.eTag())
                                    .partNumber(partNumber)
                                    .build();
                        }
                );
    }

    private Mono<CompleteMultipartUploadResponse> completeUpload(UploadState state) {
        CompletedMultipartUpload multipartUpload = CompletedMultipartUpload.builder()
                .parts(state.getCompletedParts().values())
                .build();
        return Mono.fromFuture(
                s3AsyncClient.completeMultipartUpload(
                        CompleteMultipartUploadRequest.builder()
                                .bucket(awsS3Properties.getS3BucketName())
                                .uploadId(state.getUploadId())
                                .multipartUpload(multipartUpload)
                                .key(state.getFileName())
                                .build()));
    }

    private void uploadResponseCheck(SdkResponse response) {
        if (response.sdkHttpResponse() == null || !response.sdkHttpResponse().isSuccessful()) {
            throw new FileStorageException("File upload error.", ErrorCode.FILE_SERVER_STORAGE_ERROR);
        }
    }

    private ByteBuffer concatenateBuffers(List<DataBuffer> buffers) {
        int partSize = 0;
        for (DataBuffer buffer : buffers) {
            partSize += buffer.readableByteCount();
        }
        ByteBuffer partData = ByteBuffer.allocate(partSize);
        buffers.forEach((buffer) -> buffer.toByteBuffer(partData));
        partData.rewind();
        return partData;
    }

}
