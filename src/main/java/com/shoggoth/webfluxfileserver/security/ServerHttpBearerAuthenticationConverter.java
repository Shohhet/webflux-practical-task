package com.shoggoth.webfluxfileserver.security;


import com.shoggoth.webfluxfileserver.security.token.TokenConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ServerHttpBearerAuthenticationConverter implements ServerAuthenticationConverter {
    private static final String BEARER_AUTH_PREFIX = "Bearer ";
    private final TokenConverter tokenConverter;
    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        var request = exchange.getRequest();
        String authorizationHeaderPayload = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorizationHeaderPayload != null) {
            if (StringUtils.startsWithIgnoreCase(authorizationHeaderPayload, BEARER_AUTH_PREFIX) &&
                authorizationHeaderPayload.length() > BEARER_AUTH_PREFIX.length()) {
                var stringToken = authorizationHeaderPayload.substring(BEARER_AUTH_PREFIX.length());
                var token = tokenConverter.convert(stringToken);
                if (token != null) {
                    var principal = new TokenPrincipal(token);
                    Authentication authentication = new PreAuthenticatedAuthenticationToken(principal, null, token.authorities());
                    return Mono.just(authentication);
                }
            }
        }
        return Mono.empty();
    }
}
