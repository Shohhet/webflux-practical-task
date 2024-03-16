package com.shoggoth.webfluxfileserver.mapper;

import com.shoggoth.webfluxfileserver.dto.UserDto;
import com.shoggoth.webfluxfileserver.entity.UserEntity;
import io.jsonwebtoken.lang.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;

public abstract class UserMapperDecorator implements UserMapper {
    @Autowired
    @Qualifier("delegate")
    private UserMapper delegate;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserEntity map(UserDto userDto) {
        UserEntity user = delegate.map(userDto);
        user.setPassword(passwordEncoder.encode(userDto.password()));
        return user;
    }
    @Override
    public UserDto map(UserEntity userEntity) {
        userEntity.setPassword(Strings.EMPTY);
        return delegate.map(userEntity);
    }
}
