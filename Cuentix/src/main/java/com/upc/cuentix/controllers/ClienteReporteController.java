package com.upc.cuentix.controllers;

import com.upc.cuentix.entities.*;
import com.upc.cuentix.repositories.*;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/cliente/mis-compras")
@Transactional(readOnly = true)
public class ClienteReporteController {
  private final com.upc.cuentix.services.BusinessApplicationService financial;
  private final ClienteRepository clientes;
  private final CompraRepository compras;
  private final ItemRepository items;
  private final CuotaRepository cuotas;

  public ClienteReporteController(
      ClienteRepository clientes,
      CompraRepository compras,
      ItemRepository items,
      CuotaRepository cuotas,
      com.upc.cuentix.services.BusinessApplicationService financial) {
    this.financial = financial;
    this.clientes = clientes;
    this.compras = compras;
    this.items = items;
    this.cuotas = cuotas;
  }

  public record CompraResumen(
      Integer id,
      Integer cuentaId,
      String tienda,
      String moneda,
      LocalDate fecha,
      Double total,
      Integer meses) {}

  public record ProductoComprado(Integer id, String nombre, Integer cantidad, String unidad) {}

  public record CuotaCliente(
      Integer id,
      Integer numero,
      Date fechaVencimiento,
      Double interes,
      Double amortizacion,
      Double mora,
      Double total,
      Boolean cancelado) {}

  public record DetalleCompra(
      Integer id,
      Integer cuentaId,
      String tienda,
      String moneda,
      LocalDate fecha,
      Double total,
      Integer meses,
      List<ProductoComprado> productos,
      List<CuotaCliente> cuotas) {}

  private Cliente actual(HttpSession session) {
    Cliente c = clientes.findByUsuario_Id((Integer) session.getAttribute("usuarioId"));
    if (c == null || !Boolean.TRUE.equals(c.getEstado()))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cliente no autorizado");
    return c;
  }

  private CompraResumen resumen(Compra c) {
    CuentaCredito a = c.getCuentaCredito();
    return new CompraResumen(
        c.getId(),
        a.getId(),
        a.getNegocio().getRazonSocial(),
        a.getMoneda().getSimbolo(),
        c.getFecha(),
        c.getTotalFinal(),
        c.getNumMeses());
  }

  @GetMapping
  public List<CompraResumen> listar(HttpSession session) {
    return compras.findAllByCuentaCredito_Cliente_Id(actual(session).getId()).stream()
        .map(this::resumen)
        .toList();
  }

  @GetMapping("/{id}")
  public DetalleCompra detalle(@PathVariable Integer id, HttpSession session) {
    Integer cliente = actual(session).getId();
    Compra c =
        compras
            .findById(id)
            .filter(
                v ->
                    v.getCuentaCredito() != null
                        && v.getCuentaCredito().getCliente().getId().equals(cliente))
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compra no encontrada"));
    CompraResumen r = resumen(c);
    return new DetalleCompra(
        r.id(),
        r.cuentaId(),
        r.tienda(),
        r.moneda(),
        r.fecha(),
        r.total(),
        r.meses(),
        items.findByCompra_Id(id).stream()
            .map(
                i ->
                    new ProductoComprado(
                        i.getId(),
                        i.getProducto().getNombre(),
                        i.getCantidad(),
                        i.getProducto().getUnidadMedida().getNombre()))
            .toList(),
        cuotas.findByCompra_Id(id).stream()
            .map(
                q ->
                    new CuotaCliente(
                        q.getId(),
                        q.getNum(),
                        q.getFechaVencimiento(),
                        q.getIntereses(),
                        financial.quoteForReport(q).amortization(),
                        financial.quoteForReport(q).lateAmount(),
                        com.upc.cuentix.services.FinancialMath.money(
                                q.getTotal() + financial.quoteForReport(q).lateAmount())
                            .doubleValue(),
                        q.getCancelado()))
            .toList());
  }
}
