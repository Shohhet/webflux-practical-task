package com.shoggoth.webfluxfileserver.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoggoth.webfluxfileserver.dto.AuthTokenResponseDto;
import com.shoggoth.webfluxfileserver.security.token.Token;
import com.shoggoth.webfluxfileserver.security.token.TokenConverter;
import com.shoggoth.webfluxfileserver.security.token.TokenFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import reactor.core.publisher.Mono;


@RequiredArgsConstructor
public class BasicAuthReturnAccessTokenSuccessHandler implements ServerAuthenticationSuccessHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TokenFactory<Token, Authentication> tokenFactory;
    private final TokenConverter tokenConverter;

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        var token = tokenFactory.generate(authentication);
        var tokenDto = new AuthTokenResponseDto(tokenConverter.convert(token));

        var serverWebExchange = webFilterExchange.getExchange();
        var response = serverWebExchange.getResponse();

        response.setStatusCode(HttpStatus.ACCEPTED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return setAuthTokenResponseBody(response, tokenDto)
                .then(webFilterExchange.getChain().filter(serverWebExchange));

    }


    private Mono<Void> setAuthTokenResponseBody(ServerHttpResponse response, AuthTokenResponseDto dto) {
        try {
            byte[] responseBodyPayload = objectMapper.writeValueAsBytes(dto);
            DataBuffer dataBuffer = response.bufferFactory().wrap(responseBodyPayload);
            return response.writeWith(Mono.just(dataBuffer));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }


}
