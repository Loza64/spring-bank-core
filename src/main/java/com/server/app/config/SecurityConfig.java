package com.server.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.server.app.filter.JwtAuthenticationFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    @Order(2)
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Con el servlet SOAP (/ws/*) registrado hay más de un servlet, así que los
        // patrones String
        // son ambiguos para Spring Security; se usan matchers de path explícitos.
        PathPatternRequestMatcher.Builder matchers = PathPatternRequestMatcher.withDefaults();

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                matchers.matcher("/api/auth/login"),
                                matchers.matcher("/api/auth/signup"),
                                matchers.matcher("/api/auth/refresh"),
                                matchers.matcher("/api/auth/logout"),
                                matchers.matcher("/v3/api-docs"),
                                matchers.matcher("/v3/api-docs/**"),
                                matchers.matcher("/swagger-ui.html"),
                                matchers.matcher("/swagger-ui/**"))
                        .permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}