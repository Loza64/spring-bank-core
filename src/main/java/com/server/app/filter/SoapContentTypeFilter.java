package com.server.app.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Algunos clientes (Postman, Apidog) mandan el SOAP como application/xml.
 * Spring-WS espera text/xml, así que se lo cambiamos solo en esos casos.
 * SOAP 1.2 (application/soap+xml) no se toca.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SoapContentTypeFilter extends OncePerRequestFilter {

  private static final String SOAP_CT = "text/xml; charset=utf-8";

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String uri = request.getRequestURI();
    boolean isSoapPath = "/ws".equals(uri) || uri.startsWith("/ws/");

    String contentType = request.getContentType();
    boolean isPlainXml = contentType != null
        && contentType.toLowerCase(Locale.ROOT).startsWith("application/xml");

    return !(isSoapPath && isPlainXml);
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    chain.doFilter(new HttpServletRequestWrapper(request) {
      @Override
      public String getContentType() {
        return SOAP_CT;
      }

      @Override
      public String getHeader(String name) {
        return "Content-Type".equalsIgnoreCase(name) ? SOAP_CT : super.getHeader(name);
      }

      @Override
      public Enumeration<String> getHeaders(String name) {
        return "Content-Type".equalsIgnoreCase(name)
            ? Collections.enumeration(List.of(SOAP_CT))
            : super.getHeaders(name);
      }
    }, response);
  }
}
