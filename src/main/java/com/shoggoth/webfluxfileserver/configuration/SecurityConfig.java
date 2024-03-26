package com.shoggoth.webfluxfileserver.configuration;

import com.shoggoth.webfluxfileserver.entity.Status;
import com.shoggoth.webfluxfileserver.exception.AuthenticationException;
import com.shoggoth.webfluxfileserver.exception.ErrorCode;
import com.shoggoth.webfluxfileserver.repository.UserRepository;
import com.shoggoth.webfluxfileserver.security.UserLoginSuccessHandler;
import com.shoggoth.webfluxfileserver.security.ServerHttpBearerAuthenticationConverter;
import com.shoggoth.webfluxfileserver.security.UsernamePasswordJsonAuthenticationConverter;
import com.shoggoth.webfluxfileserver.security.token.AccessTokenFactory;
import com.shoggoth.webfluxfileserver.security.token.TokenConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.config.web.server.ServerHttpSecurity.FormLoginSpec;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ReactivePreAuthenticatedAuthenticationManager;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity()
@RequiredArgsConstructor
public class SecurityConfig {
    private static final String LOGIN_PATH = "/api/v1/users/login";
    private static final String REGISTRATION_PATH = "api/v1/users/register";

    private final UserRepository userRepository;
    private final AccessTokenFactory tokenFactory;
    private final TokenConverter tokenConverter;

    @Bean
    public SecurityWebFilterChain webFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(authorizeExchangeSpec -> {
                            authorizeExchangeSpec.pathMatchers(HttpMethod.POST, REGISTRATION_PATH).permitAll();
                            authorizeExchangeSpec.anyExchange().authenticated();
                        }
                )
                .httpBasic(Customizer.withDefaults())
                .csrf(CsrfSpec::disable)
                .formLogin(FormLoginSpec::disable)
                .addFilterAt(basicAuthenticationFilter(tokenFactory, tokenConverter, userRepository), SecurityWebFiltersOrder.HTTP_BASIC)
                .addFilterAfter(usernamePasswordJsonAuthenticationFilter(tokenFactory, tokenConverter, userRepository), SecurityWebFiltersOrder.HTTP_BASIC)
                .addFilterAt(tokenAuthenticationFilter(tokenConverter), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ReactiveUserDetailsService reactiveUserDetailsService(UserRepository userRepository) {
        return username -> userRepository.findUserEntityByEmail(username)
                .filter(userEntity -> userEntity.getStatus().equals(Status.ACTIVE))
                .map(userEntity -> User.builder()
                        .username(userEntity.getEmail())
                        .password(userEntity.getPassword())
                        .authorities(userEntity.getRole().name())
                        .build()
                )
                .switchIfEmpty(Mono.error(new AuthenticationException("User with email: %s was deleted or does not exist".formatted(username), ErrorCode.FILE_SERVER_AUTHENTICATION_ERROR)));
    }

    @Bean
    public AuthenticationWebFilter basicAuthenticationFilter(AccessTokenFactory tokenFactory,
                                                             TokenConverter tokenConverter,
                                                             UserRepository userRepository) {

        var authManager = new UserDetailsRepositoryReactiveAuthenticationManager(reactiveUserDetailsService(userRepository));
        authManager.setPasswordEncoder(passwordEncoder());
        var successHandler = new UserLoginSuccessHandler(tokenFactory, tokenConverter);
        ServerWebExchangeMatcher matcher = ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, LOGIN_PATH);

        var basicAuthenticationFilter = new AuthenticationWebFilter(authManager);
        basicAuthenticationFilter.setRequiresAuthenticationMatcher(matcher);
        basicAuthenticationFilter.setAuthenticationSuccessHandler(successHandler);

        return basicAuthenticationFilter;
    }

    @Bean
    public AuthenticationWebFilter usernamePasswordJsonAuthenticationFilter(AccessTokenFactory tokenFactory,
                                                                            TokenConverter tokenConverter,
                                                                            UserRepository userRepository) {

        var authManager = new UserDetailsRepositoryReactiveAuthenticationManager(reactiveUserDetailsService(userRepository));
        authManager.setPasswordEncoder(passwordEncoder());
        var successHandler = new UserLoginSuccessHandler(tokenFactory, tokenConverter);
        ServerWebExchangeMatcher matcher = ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, LOGIN_PATH);
        var authConverter = new UsernamePasswordJsonAuthenticationConverter(new Jackson2JsonDecoder());
        var usernamePasswordJsonAuthenticationFilter = new AuthenticationWebFilter(authManager);
        usernamePasswordJsonAuthenticationFilter.setServerAuthenticationConverter(authConverter);
        usernamePasswordJsonAuthenticationFilter.setRequiresAuthenticationMatcher(matcher);
        usernamePasswordJsonAuthenticationFilter.setAuthenticationSuccessHandler(successHandler);

        return usernamePasswordJsonAuthenticationFilter;

    }

    @Bean
    public AuthenticationWebFilter tokenAuthenticationFilter(TokenConverter tokenConverter) {

        var authManager = new ReactivePreAuthenticatedAuthenticationManager(reactiveUserDetailsService(userRepository));
        var tokenAuthenticationFilter = new AuthenticationWebFilter(authManager);
        var authConverter = new ServerHttpBearerAuthenticationConverter(tokenConverter);
        tokenAuthenticationFilter.setServerAuthenticationConverter(authConverter);
        tokenAuthenticationFilter.setSecurityContextRepository(new WebSessionServerSecurityContextRepository());
        return tokenAuthenticationFilter;
    }

    @Bean
    static RoleHierarchy roleHierarchy() {
        String roleHierarchyStringRepresentation = """
                ADMIN > MODERATOR
                MODERATOR > USER
                """;
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy(roleHierarchyStringRepresentation);
        return roleHierarchy;
    }

    @Bean
    @Primary
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        return handler;
    }
}
