package com.sogeti.iamservice.config;

import com.sogeti.iamservice.model.Account;
import com.sogeti.iamservice.security.JwtTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(AccountListProperties.class)
public class SecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;
    private final AccountListProperties accountListProperties;

    @Autowired
    public SecurityConfig(JwtTokenFilter jwtTokenFilter,
                    final AccountListProperties accountListProperties, AuthenticationManagerBuilder managerBuilder) {
        this.jwtTokenFilter = jwtTokenFilter;
        this.accountListProperties = accountListProperties;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain loginFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .securityMatcher("/api/accounts/login")
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated()
                )
                .httpBasic(withDefaults());
        return http.build();
    }

    @Bean
    public SecurityFilterChain tokenFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .securityMatcher("/api/accounts/token-validation")
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated()
                )
                .apply(new JwtConfigurer(jwtTokenFilter));
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(BCryptPasswordEncoder bCryptPasswordEncoder) {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        for (Account account : accountListProperties.getConfig()) {
            manager.createUser(User.withUsername(account.getUsername())
                    .password(bCryptPasswordEncoder.encode(account.getPassword()))
                    .roles(account.getRole()).build());
        }
        return manager;
    }
}
