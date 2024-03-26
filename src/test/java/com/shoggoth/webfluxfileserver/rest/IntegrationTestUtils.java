package com.shoggoth.webfluxfileserver.rest;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class IntegrationTestUtils {
    protected static final Long SIMPLE_USER_ID = 1L;
    public static final String SIMPLE_USER_FIRST_NAME = "Andrey";
    public static final String SIMPLE_USER_LAST_NAME = "Andreev";
    public static final String SIMPLE_USER_EMAIL = "aa@mail.com";

    public static final String MODERATOR_USER_FIRST_NAME = "Boris";
    public static final String MODERATOR_USER_LAST_NAME = "Borisov";
    public static final String MODERATOR_USER_EMAIL = "bb@mail.com";

    public static final String ADMIN_USER_FIRST_NAME = "Sergey";
    public static final String ADMIN_USER_LAST_NAME = "Sergeev";
    public static final String ADMIN_USER_EMAIL = "ss@mail.com";

    public static final String PASSWORD = "123";
    public static final LocalDateTime CREATED_AT = LocalDateTime.now();
    public static final LocalDateTime UPDATED_AT = CREATED_AT;


}
