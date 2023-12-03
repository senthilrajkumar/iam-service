package com.sogeti.iamservice.security;

import com.sogeti.iamservice.config.AccountListProperties;
import com.sogeti.iamservice.model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private AccountListProperties accountListProperties;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(accountListProperties);
        ReflectionTestUtils.setField(jwtTokenProvider, "secretString",
                "secretkeysecretkeysecretkeysecretkeysecretkeysecretkey");
        ReflectionTestUtils.setField(jwtTokenProvider, "expiration", 3600000); // 1 hour
        jwtTokenProvider.init();
    }

    @Test
    void testToGenerateValidToken() {
        String username = "testUser";
        String token = jwtTokenProvider.generateToken(username);
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(username, jwtTokenProvider.getUsernameFromToken(token));
    }

    @Test
    void testToReturnUsername() {
        String username = "testUser";
        String token = jwtTokenProvider.generateToken(username);
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);
        assertEquals(username, extractedUsername);
    }

    @Test
    void testToReturnRoles() {
        String username = "testUser";
        String role = "ROLE_USER";
        Account testAccount = Account.builder().username(username)
                .password("password").role(role).build();
        when(accountListProperties.getConfig()).thenReturn(Collections.singletonList(testAccount));
        String token = jwtTokenProvider.generateToken(username);
        List<String> roles = jwtTokenProvider.getRolesFromToken(token);
        assertNotNull(roles);
        assertTrue(roles.contains(role));
    }

    @Test
    void testValidTokenToReturnTrue() {
        String username = "testUser";
        String token = jwtTokenProvider.generateToken(username);
        boolean isValid = jwtTokenProvider.validateToken(token);
        assertTrue(isValid);
    }

    @Test
    void testValidTokenToReturnFalse() {
        String invalidToken = "invalidToken";
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);
        assertFalse(isValid);
    }

    @Test
    void testValidTokenToReturnFalseWithExpiredJwtException() {
        String username = "testUser";
        long expiration = -3600000; // 1 hour ago
        ReflectionTestUtils.setField(jwtTokenProvider, "expiration", expiration);
        String expiredToken = jwtTokenProvider.generateToken(username);
        assertFalse(jwtTokenProvider.validateToken(expiredToken));
    }

    @Test
    void testValidTokenToReturnFalseWithSignatureException() {
        String username = "testUser";
        String tamperedToken = jwtTokenProvider.generateToken(username) + "tampered";
        assertFalse(jwtTokenProvider.validateToken(tamperedToken));
    }

    @Test
    void testValidTokenToReturnFalseWithMalformedJwtException() {
        String malformedToken = "malformedToken";
        assertFalse(jwtTokenProvider.validateToken(malformedToken));
    }
}
