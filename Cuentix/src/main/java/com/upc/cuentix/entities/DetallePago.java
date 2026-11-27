package com.upc.cuentix.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "detalle_pago")
public class DetallePago {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "recibo_id", nullable = false)
  private ReciboPago recibo;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "cuota_id", nullable = false, unique = true)
  private Cuota cuota;

  @Column(nullable = false, precision = 38, scale = 2)
  private BigDecimal mora;

  @Column(nullable = false, precision = 38, scale = 2)
  private BigDecimal interes;

  @Column(nullable = false, precision = 38, scale = 2)
  private BigDecimal capital;

  @Column(name = "interes_gracia", nullable = false, precision = 38, scale = 2)
  private BigDecimal interesGracia;
}
