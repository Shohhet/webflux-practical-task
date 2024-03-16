package com.shoggoth.webfluxfileserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;

@SpringBootApplication
@EnableReactiveMethodSecurity
public class WebfluxFileServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebfluxFileServerApplication.class, args);
    }

}
