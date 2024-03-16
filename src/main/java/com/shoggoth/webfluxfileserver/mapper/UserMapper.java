package com.shoggoth.webfluxfileserver.mapper;

import com.shoggoth.webfluxfileserver.dto.UserDto;
import com.shoggoth.webfluxfileserver.entity.UserEntity;
import org.mapstruct.DecoratedWith;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
@DecoratedWith(UserMapperDecorator.class)
public interface UserMapper {
    UserDto map(UserEntity userEntity);

    @InheritConfiguration
    UserEntity map(UserDto userDto);
}
