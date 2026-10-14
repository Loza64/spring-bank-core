package com.server.app.common.security;

import org.springframework.security.core.Authentication;

public final class AuthUtils {

  private AuthUtils() {
  }

  public static boolean hasAuthority(Authentication authentication, String authority) {
    return authentication != null && authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals(authority));
  }
}
