package com.shoggoth.webfluxfileserver.rest;

import com.shoggoth.webfluxfileserver.dto.AuthTokenResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;


public class UserRestControllerV1IT extends AbstractIT {

    @Test
    void registerUser_ThenUserDoesNotExist_WhenReturnRegisteredUserJwt() {
        String body = """
                {
                  "firstName": "Alex",
                  "lastName": "Alexeev",
                  "email": "aa@gmail.com",
                  "password": "5"
                }
                """;
        webTestClient
                .post()
                .uri("/api/v1/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthTokenResponseDto.class);

    }
}
