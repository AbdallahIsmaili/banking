package com.banque.accountservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers("/actuator/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/accounts/client/**").hasAnyAuthority(SecurityConstants.ROLE_CLIENT, SecurityConstants.ROLE_BANKER, SecurityConstants.ROLE_ADMIN)
                                .requestMatchers(HttpMethod.GET, "/api/accounts/**").hasAnyAuthority(SecurityConstants.ROLE_BANKER, SecurityConstants.ROLE_ADMIN)
                                .requestMatchers(HttpMethod.POST, "/api/accounts").hasAnyAuthority(SecurityConstants.ROLE_BANKER, SecurityConstants.ROLE_ADMIN)
                                .requestMatchers(HttpMethod.PUT, "/api/accounts/**").hasAnyAuthority(SecurityConstants.ROLE_BANKER, SecurityConstants.ROLE_ADMIN)
                                .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        /*
        @Bean
public JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withPublicKey(rsaPublicKey).build();
}

@Bean
public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
    grantedAuthoritiesConverter.setAuthorityPrefix("");

    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
    return jwtAuthenticationConverter;
}
         */

        return http.build();
    }
}