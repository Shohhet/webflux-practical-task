package com.shoggoth.webfluxfileserver.mapper;

import com.shoggoth.webfluxfileserver.dto.FileDto;
import com.shoggoth.webfluxfileserver.entity.FileEntity;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FileMapper {
    FileDto map(FileEntity fileEntity);
    @InheritConfiguration
    FileEntity map(FileDto fileDto);
}
