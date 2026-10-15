package com.server.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Credenciales del cliente SOAP (security.soap.* en application.properties). */
@ConfigurationProperties(prefix = "security.soap")
public record SoapProperties(String username, String password) {
}
