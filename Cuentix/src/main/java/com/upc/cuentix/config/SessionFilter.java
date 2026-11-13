package com.upc.cuentix.config;

import com.upc.cuentix.entities.Usuario;
import com.upc.cuentix.repositories.UsuarioRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class SessionFilter extends OncePerRequestFilter {
  private final UsuarioRepository usuarios;

  public SessionFilter(UsuarioRepository usuarios) {
    this.usuarios = usuarios;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    String p = req.getServletPath();
    boolean publicAuth =
        Set.of(
                    "/api/auth/login",
                    "/api/auth/register",
                    "/login",
                    "/registrarCliente",
                    "/registrarNegocio")
                .contains(p)
            && "POST".equals(req.getMethod());
    if (publicAuth) {
      chain.doFilter(req, res);
      return;
    }
    HttpSession session = req.getSession(false);
    if (session == null || !(session.getAttribute("usuarioId") instanceof Integer)) {
      fail(res, 401, "Debe iniciar sesión");
      return;
    }
    Usuario u = usuarios.findById((Integer) session.getAttribute("usuarioId")).orElse(null);
    if (u == null || !Boolean.TRUE.equals(u.getEstado()) || u.getRol() == null) {
      session.invalidate();
      fail(res, 401, "Sesión inválida");
      return;
    }
    int rol = u.getRol().getId();
    boolean own = p.startsWith("/api/cliente/");
    if ((own && rol != 2)
        || (!own
            && !p.equals("/api/auth/session")
            && !p.equals("/api/logout")
            && rol != 1
            && rol != 3)) {
      fail(res, 403, "Recurso no autorizado");
      return;
    }
    if (!Set.of("GET", "HEAD", "OPTIONS").contains(req.getMethod())) {
      String expected = (String) session.getAttribute("csrf"),
          provided = req.getHeader("X-Cuentix-CSRF");
      if (expected == null
          || provided == null
          || !MessageDigest.isEqual(
              expected.getBytes(StandardCharsets.UTF_8),
              provided.getBytes(StandardCharsets.UTF_8))) {
        fail(res, 403, "Solicitud no autorizada");
        return;
      }
    }
    chain.doFilter(req, res);
  }

  private void fail(HttpServletResponse res, int status, String message) throws IOException {
    res.setStatus(status);
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    String q = Character.toString(34);
    res.getWriter()
        .write(
            "{" + q + "status" + q + ":" + status + "," + q + "message" + q + ":" + q + message + q
                + "}");
  }
}
