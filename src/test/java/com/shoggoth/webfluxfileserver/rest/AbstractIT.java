package com.shoggoth.webfluxfileserver.rest;

import com.shoggoth.webfluxfileserver.entity.Role;
import com.shoggoth.webfluxfileserver.entity.Status;
import com.shoggoth.webfluxfileserver.entity.UserEntity;
import com.shoggoth.webfluxfileserver.repository.UserRepository;
import com.shoggoth.webfluxfileserver.security.token.AccessTokenFactory;
import com.shoggoth.webfluxfileserver.security.token.Token;
import com.shoggoth.webfluxfileserver.security.token.TokenConverter;
import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.Resource;
import org.springframework.r2dbc.connection.init.ScriptUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;

import static com.shoggoth.webfluxfileserver.rest.IntegrationTestUtils.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIT {

    @Autowired
    protected WebTestClient webTestClient;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected PasswordEncoder passwordEncoder;
    @Autowired
    private AccessTokenFactory tokenFactory;
    @Autowired
    private TokenConverter tokenConverter;

    public UserEntity SIMPLE_USER;
    public UserEntity MODERATOR_USER;
    public UserEntity ADMIN_USER;


    @ServiceConnection
    static final MySQLContainer<?> container = new MySQLContainer<>(DockerImageName.parse("mysql:8.2"));


    @Autowired
    ConnectionFactory connectionFactory;

    static {
        container
                .withReuse(true)
                .start();
    }

    @BeforeEach
    void initUserEntities() {
        SIMPLE_USER = UserEntity.builder()
                .firstName(SIMPLE_USER_FIRST_NAME)
                .lastName(SIMPLE_USER_LAST_NAME)
                .email(SIMPLE_USER_EMAIL)
                .password(passwordEncoder.encode(PASSWORD))
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();

        MODERATOR_USER = UserEntity.builder()
                .firstName(MODERATOR_USER_FIRST_NAME)
                .lastName(MODERATOR_USER_LAST_NAME)
                .email(MODERATOR_USER_EMAIL)
                .password(passwordEncoder.encode(PASSWORD))
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .role(Role.MODERATOR)
                .status(Status.ACTIVE)
                .build();
        ADMIN_USER = UserEntity.builder()
                .firstName(ADMIN_USER_FIRST_NAME)
                .lastName(ADMIN_USER_LAST_NAME)
                .email(ADMIN_USER_EMAIL)
                .password(passwordEncoder.encode(PASSWORD))
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .role(Role.ADMIN)
                .status(Status.ACTIVE)
                .build();

    }

    @AfterEach
    void clearDatabase(@Value("classpath:/sql/truncate.sql") Resource script) {
        executeSqlScriptBlocking(script);
    }

    protected String getJwt(UserEntity user) {
        Authentication authentication = new PreAuthenticatedAuthenticationToken(user.getEmail(), user.getRole());
        Token token = tokenFactory.generate(authentication);
        return tokenConverter.convert(token);
    }

    private void executeSqlScriptBlocking(final Resource script) {
        Mono.from(connectionFactory.create())
                .flatMap(connection -> ScriptUtils.executeSqlScript(connection, script))
                .block();
    }


}

