package com.upc.cuentix.entities;

import jakarta.persistence.*;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cuota")
public class Cuota {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_cuota", nullable = false)
  private Integer id;

  @Column(name = "fecha_vencimiento", nullable = false)
  private Date fechaVencimiento;

  @Column(name = "num", nullable = false)
  private Integer num;

  @Column(name = "intereses", nullable = false)
  private Double intereses;

  @Column(name = "moras", nullable = false)
  private Double moras;

  @Column(name = "total", nullable = false)
  private Double total;

  @Column(name = "cancelado", nullable = false)
  private Boolean cancelado = false;

  @Column(name = "fecha_pago")
  private java.time.LocalDate fechaPago;

  @ManyToOne
  @JoinColumn(name = "compra_id")
  private Compra compra;
}
