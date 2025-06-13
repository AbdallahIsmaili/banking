package com.securitybanking.transaction.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Bean;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors() // Active le support CORS
                .and()
                .csrf().disable() // désactive CSRF pour les appels API
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().permitAll());

        return http.build();
    }
}
