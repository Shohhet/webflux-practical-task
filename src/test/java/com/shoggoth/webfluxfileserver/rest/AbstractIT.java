package com.shoggoth.webfluxfileserver.rest;

import com.shoggoth.webfluxfileserver.WebfluxFileServerApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = WebfluxFileServerApplication.class)
public abstract class AbstractIT {
    @Autowired
    ApplicationContext applicationContext;
    WebTestClient webTestClient;

    @ServiceConnection
    static final MySQLContainer<?> container = new MySQLContainer<>(DockerImageName.parse("mysql:8.2"));

    static {
        container
                .withReuse(true)
                .start();
    }

    @BeforeEach
    void init() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext)
                //.apply(springSecurity())
                .configureClient()
                .build();
    }

}

