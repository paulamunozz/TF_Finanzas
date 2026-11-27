package com.upc.cuentix.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "recibo_pago", uniqueConstraints = @UniqueConstraint(columnNames = {"cuenta_id", "clave"}))
public class ReciboPago {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "cuenta_id", nullable = false)
  private CuentaCredito cuenta;

  @Column(nullable = false)
  private UUID clave;

  @Column(nullable = false)
  private LocalDate vencimiento;

  @Column(name = "fecha_pago", nullable = false)
  private LocalDate fechaPago;

  @Column(nullable = false)
  private String moneda;

  @OneToMany(mappedBy = "recibo", cascade = CascadeType.PERSIST)
  @OrderBy("id ASC")
  private List<DetallePago> detalles = new ArrayList<>();
}
