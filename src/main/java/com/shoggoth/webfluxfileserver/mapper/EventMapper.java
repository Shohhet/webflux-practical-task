package com.shoggoth.webfluxfileserver.mapper;

import com.shoggoth.webfluxfileserver.dto.EventDto;
import com.shoggoth.webfluxfileserver.entity.EventEntity;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventMapper {
    EventDto map(EventEntity eventEntity);
    @InheritConfiguration
    EventEntity map(EventDto eventDto);
}
