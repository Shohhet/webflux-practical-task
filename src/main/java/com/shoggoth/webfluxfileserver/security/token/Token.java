package com.shoggoth.webfluxfileserver.security.token;

import org.springframework.security.core.GrantedAuthority;

import java.time.Instant;
import java.util.List;

public record Token(
        String subject,
        List<GrantedAuthority> authorities,
        Instant createdAt,
        Instant expiresAt
) {
}
