package com.upc.cuentix.config;

import com.upc.cuentix.entities.*;
import com.upc.cuentix.repositories.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ResourceAccess {
  private final HttpServletRequest request;
  private final UsuarioRepository usuarios;
  private final NegocioRepository negocios;
  private final CuentaCreditoRepository cuentas;
  private final CompraRepository compras;
  private final ItemRepository items;
  private final ProductoRepository productos;

  public ResourceAccess(
      HttpServletRequest request,
      UsuarioRepository usuarios,
      NegocioRepository negocios,
      CuentaCreditoRepository cuentas,
      CompraRepository compras,
      ItemRepository items,
      ProductoRepository productos) {
    this.request = request;
    this.usuarios = usuarios;
    this.negocios = negocios;
    this.cuentas = cuentas;
    this.compras = compras;
    this.items = items;
    this.productos = productos;
  }

  private Usuario actual() {
    var s = request.getSession(false);
    if (s == null || !(s.getAttribute("usuarioId") instanceof Integer))
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    return usuarios
        .findById((Integer) s.getAttribute("usuarioId"))
        .filter(u -> Boolean.TRUE.equals(u.getEstado()))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
  }

  public boolean admin() {
    return actual().getRol() != null && Integer.valueOf(1).equals(actual().getRol().getId());
  }

  public void requireAdmin() {
    if (!admin()) deny();
  }

  public boolean ownsBusiness(Integer id) {
    if (admin()) return true;
    if (id == null) return false;
    return negocios
        .findById(id)
        .filter(
            n ->
                Boolean.TRUE.equals(n.getEstado())
                    && n.getUsuario() != null
                    && n.getUsuario().getId().equals(actual().getId()))
        .isPresent();
  }

  public void business(Integer id) {
    if (!ownsBusiness(id)) deny();
  }

  public void account(Integer id) {
    if (id == null) throw new IllegalArgumentException("Indique cuenta");
    var c =
        cuentas.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    business(c.getNegocio().getId());
  }

  public void purchase(Integer id) {
    var c =
        compras.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    account(c.getCuentaCredito().getId());
  }

  public void item(Integer id) {
    var i = items.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    purchase(i.getCompra().getId());
  }

  public void product(Integer id) {
    var p =
        productos.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    business(p.getNegocio().getId());
  }

  public void client(Integer id) {
    if (admin()) return;
    boolean linked =
        cuentas.findAll().stream()
            .anyMatch(
                c -> c.getCliente().getId().equals(id) && ownsBusiness(c.getNegocio().getId()));
    if (!linked) deny();
  }

  private void deny() {
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Recurso no autorizado");
  }
}
