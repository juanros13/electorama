package com.electorama.msvc.users.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Valida que el header {@code X-Auth-Id} (la identidad propia del llamador, usada por casi todos
 * los endpoints) coincida con el UUID del token JWT validado ({@code uid}). Sin esto, un cliente
 * podría enviar su propio token pero un X-Auth-Id forjado y actuar como otro usuario (p. ej.
 * cambiar su contraseña). Se registra dentro de la cadena de seguridad, después de la autenticación
 * del bearer token.
 */
@Slf4j
public class AuthIdHeaderValidationFilter extends OncePerRequestFilter {

  private final boolean enforce;

  public AuthIdHeaderValidationFilter(boolean enforce) {
    this.enforce = enforce;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String headerAuthId = request.getHeader("X-Auth-Id");
    if (headerAuthId != null && !headerAuthId.isBlank()) {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication instanceof JwtAuthenticationToken jwtAuth) {
        Object tokenUid = jwtAuth.getToken().getClaim("uid");
        if (tokenUid == null || !headerAuthId.equals(tokenUid.toString())) {
          log.warn(
              "X-Auth-Id ({}) no coincide con la identidad del token ({})", headerAuthId, tokenUid);
          if (enforce) {
            response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "X-Auth-Id no coincide con la identidad autenticada");
            return;
          }
        }
      }
    }
    filterChain.doFilter(request, response);
  }
}
