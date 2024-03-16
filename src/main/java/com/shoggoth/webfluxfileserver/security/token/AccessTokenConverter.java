package com.shoggoth.webfluxfileserver.security.token;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccessTokenConverter implements TokenConverter {
    private static final String AUTHORITIES_CLAIM_KEY = "auth";

    @Value("${spring.security.token.secret-key}")
    private String secretKeyBase64Encoded;

    @Override
    public String convert(Token token) {
        var header = Jwts.header()
                .type("JWS")
                .build();

        var claims = Jwts.claims()
                .subject(token.subject())
                .add(AUTHORITIES_CLAIM_KEY, token.authorities())
                .issuedAt(Date.from(token.createdAt()))
                .expiration(Date.from(token.expiresAt()))
                .build();

        var secretKey = decodeSecretKey(secretKeyBase64Encoded);

        return Jwts.builder()
                .header().add(header).and()
                .claims().add(claims).and()
                .signWith(secretKey)
                .compact();
    }

    @Override
    public Token convert(String stringToken) {
        var secretKey = decodeSecretKey(secretKeyBase64Encoded);

        var jwsPayload = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(stringToken)
                .getPayload();

        Object authoritiesClaim = jwsPayload.get(AUTHORITIES_CLAIM_KEY);
        List<GrantedAuthority> authorities = authoritiesClaim == null ?
                AuthorityUtils.NO_AUTHORITIES :
                AuthorityUtils.commaSeparatedStringToAuthorityList(authoritiesClaim.toString());

        return new Token(
                jwsPayload.getSubject(),
                authorities,
                jwsPayload.getIssuedAt().toInstant(),
                jwsPayload.getExpiration().toInstant()
        );

    }

    private SecretKey decodeSecretKey(String secretKeyBase64Encoded) {
        var byteArrayJwsSecretKey = Decoders.BASE64.decode(secretKeyBase64Encoded);
        return Keys.hmacShaKeyFor(byteArrayJwsSecretKey);
    }
}
