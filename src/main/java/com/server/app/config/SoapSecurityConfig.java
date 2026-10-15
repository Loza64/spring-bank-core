package com.server.app.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * Cadena de seguridad solo para /ws/**. Los socios no usan JWT: entran como
 * cliente máquina con HTTP Basic (siempre sobre TLS en prod).
 * Si algún día piden WS-Security con certificados, este es el lugar a cambiar.
 */
@Configuration
@EnableConfigurationProperties(SoapProperties.class)
public class SoapSecurityConfig {

  public static final String SOAP_CLIENT = "SOAP_CLIENT";

  @Bean
  @Order(1)
  SecurityFilterChain soapSecurityFilterChain(
      HttpSecurity http,
      PasswordEncoder encoder,
      SoapProperties properties) throws Exception {

    UserDetails client = User.withUsername(properties.username())
        .password(encoder.encode(properties.password()))
        .authorities(SOAP_CLIENT)
        .build();

    PathPatternRequestMatcher.Builder matchers = PathPatternRequestMatcher.withDefaults();

    http
        .securityMatcher(matchers.matcher("/ws/**"))
        .csrf(csrf -> csrf.disable()) // no hay cookies ni sesión, csrf no aplica
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .userDetailsService(new InMemoryUserDetailsManager(client))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(matchers.matcher(HttpMethod.GET, "/ws/*.wsdl")).permitAll()
            .anyRequest().hasAuthority(SOAP_CLIENT))
        .httpBasic(Customizer.withDefaults());

    return http.build();
  }
}
