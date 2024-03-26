package com.shoggoth.webfluxfileserver.rest;

import com.shoggoth.webfluxfileserver.dto.UserDto;
import io.jsonwebtoken.io.Encoders;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static com.shoggoth.webfluxfileserver.rest.IntegrationTestUtils.*;

@Disabled
public class UserRestControllerV1IT extends AbstractIT {

    @Test
    void registerUser_ThenUserDoesNotExist_WhenReturnRegisteredUserDto() {
        String body = """
                {
                  "firstName": "%s",
                  "lastName": "%s",
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(SIMPLE_USER_FIRST_NAME, SIMPLE_USER_LAST_NAME, SIMPLE_USER_EMAIL, PASSWORD);
        webTestClient
                .post()
                .uri("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("1")
                .jsonPath("$.firstName").isEqualTo(SIMPLE_USER_FIRST_NAME)
                .jsonPath("$.lastName").isEqualTo(SIMPLE_USER_LAST_NAME)
                .jsonPath("$.email").isEqualTo(SIMPLE_USER_EMAIL)
                .jsonPath("$.password").isEmpty();


    }

    @Test
    void loginBaseAuth_WhenUserExist_ThenReturnAccessToken() {

        String authorizationHeaderPayload = Encoders.BASE64
                .encode((SIMPLE_USER_EMAIL + ":" + PASSWORD).getBytes());
        userRepository.save(SIMPLE_USER).block();
        webTestClient
                .get()
                .uri("/api/v1/users/login")
                .header("Authorization", "Basic " + authorizationHeaderPayload)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody()
                .jsonPath("$.authToken").hasJsonPath()
                .jsonPath("$.authToken").isNotEmpty();

    }

    @Test
    void loginJsonAuth_WhenUserExist_ThenReturnAccessToken() {
        String body = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(SIMPLE_USER_EMAIL, PASSWORD);
        userRepository.save(SIMPLE_USER).block();
        webTestClient
                .post()
                .uri("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody()
                .jsonPath("$.authToken").hasJsonPath()
                .jsonPath("$.authToken").isNotEmpty();
    }

    @Test
    void getMySelf_WhenUserExistAndTokenAccepted_ThenReturnAuthenticatedUserDto() {
        userRepository.save(SIMPLE_USER).block();
        String authHeaderPayload = "Bearer " + super.getJwt(SIMPLE_USER);
        webTestClient
                .get()
                .uri("/api/v1/users/me")
                .header("Authorization", authHeaderPayload)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("1")
                .jsonPath("$.firstName").isEqualTo(SIMPLE_USER_FIRST_NAME)
                .jsonPath("$.lastName").isEqualTo(SIMPLE_USER_LAST_NAME)
                .jsonPath("$.email").isEqualTo(SIMPLE_USER_EMAIL)
                .jsonPath("$.password").isEmpty();

    }

    @Test
    void findAllUsers_WhenUserIsAdmin_ThenReturnUsersList() {
        userRepository.save(SIMPLE_USER).block();
        userRepository.save(MODERATOR_USER).block();
        userRepository.save(ADMIN_USER).block();
        String authHeaderPayload = "Bearer " + super.getJwt(ADMIN_USER);
        webTestClient
                .get()
                .uri("/api/v1/users/")
                .header("Authorization", authHeaderPayload)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserDto.class)
                .hasSize(3);
    }

    @Test
    void updateUser_WhenAuthenticatedAsAdminAndUpdatedUserExists_ThenReturnUpdatedUser() {
        String body = """
                {
                  "id": "%s",
                  "firstName": "%s",
                  "lastName": "%s",
                  "email": "%s",
                  "password": "%s"
                }
                                """.formatted(SIMPLE_USER_ID.toString(), "vasiliy", "terkin", "vt@mail.ru", PASSWORD);
        userRepository.save(MODERATOR_USER).block();
        userRepository.save(ADMIN_USER).block();
        String authHeaderPayload = "Bearer " + super.getJwt(ADMIN_USER);
        webTestClient
                .put()
                .uri("/api/v1/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", authHeaderPayload)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("1")
                .jsonPath("$.firstName").isEqualTo("vasiliy")
                .jsonPath("$.lastName").isEqualTo("terkin")
                .jsonPath("$.email").isEqualTo("vt@mail.ru")
                .jsonPath("$.password").isEmpty();
    }
    @Test
    void deleteUserById_WhenAuthenticatedAsAdminAndDeletingUserExists_ThenDeleteUser() {
        userRepository.save(MODERATOR_USER).block();
        userRepository.save(ADMIN_USER).block();
        String authHeaderPayload = "Bearer " + super.getJwt(ADMIN_USER);
        webTestClient
                .delete()
                .uri("/api/v1/users/%s".formatted(SIMPLE_USER_ID))
                .header("Authorization", authHeaderPayload)
                .exchange()
                .expectStatus().isOk();
    }

}
