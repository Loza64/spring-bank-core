package com.server.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * Cadena de seguridad exclusiva para /ws/**. Los socios/sistemas externos no
 * usan JWT: se autentican como cliente máquina con HTTP Basic (sobre TLS).
 * Para WS-Security con certificados (WSS4J) este es el punto a reemplazar.
 */
@Configuration
public class SoapSecurityConfig {

  public static final String SOAP_CLIENT = "SOAP_CLIENT";

  @Bean
  @Order(1)
  SecurityFilterChain soapSecurityFilterChain(
      HttpSecurity http,
      PasswordEncoder encoder,
      @Value("${security.soap.username}") String username,
      @Value("${security.soap.password}") String password) throws Exception {

    UserDetails client = org.springframework.security.core.userdetails.User
        .withUsername(username)
        .password(encoder.encode(password))
        .authorities(SOAP_CLIENT)
        .build();

    PathPatternRequestMatcher.Builder matchers = PathPatternRequestMatcher.withDefaults();

    http
        .securityMatcher(matchers.matcher("/ws/**"))
        .csrf(csrf -> csrf.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .userDetailsService(new InMemoryUserDetailsManager(client))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(matchers.matcher(HttpMethod.GET, "/ws/*.wsdl")).permitAll()
            .anyRequest().hasAuthority(SOAP_CLIENT))
        .httpBasic(Customizer.withDefaults());

    return http.build();
  }
}
