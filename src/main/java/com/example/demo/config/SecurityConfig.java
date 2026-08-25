package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.demo.filter.AuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationFilter authenticationFilter;

    public SecurityConfig(AuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .csrf(AbstractHttpConfigurer::disable)

            // Use CORS configuration from CorsConfig.java
            .cors(cors -> {})

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(auth -> auth

                // Public endpoints
                .requestMatchers(
                    "/api/users/register",
                    "/api/auth/login",
                    "/api/products/**",
                    "/api/categories/**"
                ).permitAll()

                // All other endpoints require authentication
                .anyRequest().authenticated()
            )

            .addFilterBefore(
                authenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}