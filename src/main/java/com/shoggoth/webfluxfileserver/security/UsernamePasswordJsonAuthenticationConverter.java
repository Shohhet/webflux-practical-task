package com.shoggoth.webfluxfileserver.security;

import com.shoggoth.webfluxfileserver.dto.AuthRequestPayloadDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ResolvableType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
@RequiredArgsConstructor
public class UsernamePasswordJsonAuthenticationConverter implements ServerAuthenticationConverter {
    private final Jackson2JsonDecoder decoder;
    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return decoder.decodeToMono(
                        exchange.getRequest().getBody(),
                        ResolvableType.forClass(AuthRequestPayloadDto.class),
                        null,
                        null)
                .cast(AuthRequestPayloadDto.class)
                .map(authRequestPayloadDto ->
                        UsernamePasswordAuthenticationToken.unauthenticated(
                                authRequestPayloadDto.email(),
                                authRequestPayloadDto.password()));
    }
}
