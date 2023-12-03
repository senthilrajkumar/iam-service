package com.sogeti.iamservice.security;

import com.sogeti.iamservice.config.AccountListProperties;
import com.sogeti.iamservice.model.Account;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
@EnableConfigurationProperties(AccountListProperties.class)
public class JwtTokenProvider {

    public static final String ROLES = "roles";
    private SecretKey secretKey;

    @Value("${jwt.secret-string}")
    private String secretString;

    @Value("${jwt.expiration}")
    private long expiration;

    private AccountListProperties accountListProperties;

    @Autowired
    public JwtTokenProvider(AccountListProperties accountListProperties) {
        this.accountListProperties = accountListProperties;
    }

    @PostConstruct
    protected void init() {
        try {
            // Use the Keys class to generate a secure key
            secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretString));
            log.info("Secret key initialized successfully");
        } catch (Exception e) {
            log.error("Error initializing secret key: {}", e.getMessage());
        }
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .claim(ROLES, List.of(getRolesForUsername(username)))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey)
                .compact();
    }

    public String getRolesForUsername(String username) {
        return accountListProperties.getConfig().stream()
                .filter(account -> account.getUsername().equals(username))
                .findFirst()
                .map(Account::getRole)
                .orElse("");
    }

    public String getUsernameFromToken(String token) {
        Jws<Claims> claimsJws = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
        return claimsJws.getPayload().getSubject();
    }

    public List<String> getRolesFromToken(String token) {
        Jws<Claims> claimsJws = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
        return claimsJws.getPayload().get(ROLES, List.class);
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            Claims claims = claimsJws.getPayload();
            // Check for required roles
            List<String> userRoles = claims.get(ROLES, List.class);
            if (userRoles == null || userRoles.isEmpty() ||
                    Collections.disjoint(userRoles, List.of(getRolesForUsername(claims.getSubject())))) {
                log.error("User does not have the required roles");
                return false;
            }
            // Check for token expiration
            if (claims.getExpiration() != null && claims.getExpiration().before(new Date())) {
                log.error("Token has expired");
                return false;
            }
            return true;
        } catch (ExpiredJwtException ex) {
            log.error("Token has expired: {}", ex.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Error when validating token: {}", e.getMessage());
            return false;
        }
    }

}
