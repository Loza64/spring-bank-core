package com.server.app.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.server.app.domain.dto.auth.ProfileResponseDto;
import com.server.app.domain.model.enums.Channel;
import com.server.app.service.AuditService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Audita las operaciones que modifican estado en /api/** (POST/PUT/PATCH/DELETE).
 * Se registra como filtro de servlet normal, por lo que corre DESPUÉS de la
 * cadena de Spring Security y ya puede ver al usuario autenticado.
 * Los endpoints /api/auth/** se omiten para no registrar credenciales.
 */
@Component
@RequiredArgsConstructor
public class RestAuditFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(RestAuditFilter.class);
  private static final int CACHE_LIMIT = 10_240;

  private final AuditService auditService;

  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    String uri = request.getRequestURI();
    String method = request.getMethod();
    boolean mutating = !(method.equals("GET") || method.equals("HEAD") || method.equals("OPTIONS"));
    return !mutating || !uri.startsWith("/api/") || uri.startsWith("/api/auth/");
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain chain) throws ServletException, IOException {

    ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request, CACHE_LIMIT);
    ContentCachingResponseWrapper res = new ContentCachingResponseWrapper(response);

    try {
      chain.doFilter(req, res);
    } finally {
      try {
        auditService.record(
            Channel.REST,
            request.getMethod() + " " + request.getRequestURI(),
            new String(req.getContentAsByteArray(), StandardCharsets.UTF_8),
            new String(res.getContentAsByteArray(), StandardCharsets.UTF_8),
            request.getRemoteAddr(),
            res.getStatus(),
            currentUserId());
      } catch (Exception e) {
        // La auditoría nunca debe romper la respuesta al cliente
        log.error("No se pudo registrar la auditoría REST", e);
      }
      res.copyBodyToResponse();
    }
  }

  private Long currentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return auth != null && auth.getPrincipal() instanceof ProfileResponseDto p ? p.id() : null;
  }
}
