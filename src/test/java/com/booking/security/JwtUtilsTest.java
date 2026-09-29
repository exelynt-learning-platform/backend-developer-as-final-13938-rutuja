package com.booking.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 3600000L);
    }

    @Test
    void testGenerateTokenAndExtractUsername() {
        UserDetails userDetails = new User("admin@booking.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

        String token = jwtUtils.generateToken(userDetails);
        assertNotNull(token);

        String username = jwtUtils.getUserNameFromJwtToken(token);
        assertEquals("admin@booking.com", username);
    }

    @Test
    void testValidateJwtTokenSuccess() {
        UserDetails userDetails = new User("admin@booking.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

        String token = jwtUtils.generateToken(userDetails);
        assertTrue(jwtUtils.validateJwtToken(token));
    }

    @Test
    void testValidateJwtTokenInvalid() {
        assertFalse(jwtUtils.validateJwtToken("invalid.jwt.token"));
        assertFalse(jwtUtils.validateJwtToken(""));
    }

    @Test
    void testValidateJwtTokenExpired() {
        JwtUtils expiredJwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(expiredJwtUtils, "jwtSecret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(expiredJwtUtils, "jwtExpirationMs", -1000L); // expired

        UserDetails userDetails = new User("admin@booking.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

        String expiredToken = expiredJwtUtils.generateToken(userDetails);
        assertFalse(jwtUtils.validateJwtToken(expiredToken));
    }
}
