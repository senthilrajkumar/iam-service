package com.sogeti.iamservice.controller;

import com.sogeti.iamservice.security.JwtTokenFilter;
import com.sogeti.iamservice.security.JwtTokenProvider;
import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;

import java.net.URI;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationControllerTest {

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private Authentication authentication;

    @MockBean
    private SecurityContext securityContext;
    @MockBean
    private JwtTokenFilter jwtTokenFilter;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Value("${basicauth.config[0].password}")
    String password;

    @Test
    void testLoginSuccess() throws Exception {
        addHeaderInterceptors("broker", password);
        ResponseEntity<String> response = testRestTemplate.postForEntity(buildLogInURI(), null, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testLoginFailure() throws Exception {
        addHeaderInterceptors("InvalidUser", "password");
        ResponseEntity<String> response = testRestTemplate.postForEntity(buildLogInURI(), null, String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testTokenValidation() throws Exception {
        addHeaderInterceptorsForBearer("asdasdasdasdasdasdasdasdasd");
        ResponseEntity<String> response = testRestTemplate.postForEntity(buildTokenURI(), null, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    private URI buildLogInURI() {
        return createURIBuilder().host("localhost").port(port)
                .scheme("http").path("/api/accounts/login")
                .build();
    }

    private URI buildTokenURI() {
        return createURIBuilder().host("localhost").port(port)
                .scheme("http").path("/api/accounts/token-validation")
                .build();
    }

    public static UriBuilder createURIBuilder() {
        UriBuilderFactory factory = new DefaultUriBuilderFactory();
        return factory.builder();
    }

    private void addHeaderInterceptors(String username, String password) {
        testRestTemplate.getRestTemplate().setInterceptors(
                Collections.singletonList((request, body, execution) -> {
                    String auth = username + ":" + password;
                    String authHeader = "Basic " + Base64.encodeBase64String(auth.getBytes());
                    request.getHeaders().add("Authorization", authHeader);
                    return execution.execute(request, body);
                }));
    }

    private void addHeaderInterceptorsForBearer(String token) {
        testRestTemplate.getRestTemplate().setInterceptors(
                Collections.singletonList((request, body, execution) -> {
                    String authHeader = "Bearer " + token;
                    request.getHeaders().add("Authorization", authHeader);
                    return execution.execute(request, body);
                }));
    }
}